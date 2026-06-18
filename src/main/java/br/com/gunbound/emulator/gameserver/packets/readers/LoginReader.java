package br.com.gunbound.emulator.gameserver.packets.readers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.gunbound.emulator.ServerConfig;
import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.gameserver.packets.readers.lobby.LobbyJoin;
import br.com.gunbound.emulator.gameserver.packets.writers.PacketWriter;
import br.com.gunbound.emulator.gameserver.packets.writers.PlayerStatsWriter;
import br.com.gunbound.emulator.gameserver.room.model.enums.GameError;
import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.model.entities.game.PlayerSessionManager;
import br.com.gunbound.emulator.services.AuthenticationService;
import br.com.gunbound.emulator.services.excepion.AuthenticationException;
import br.com.gunbound.emulator.services.excepion.AuthorityException;
import br.com.gunbound.emulator.services.excepion.VersionException;
import br.com.gunbound.emulator.services.impl.AuthenticationServiceImpl;
import br.com.gunbound.emulator.utils.FunctionRestrict;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class LoginReader {

	private static final int LOGIN_REQUEST = 0x1010;
	private static final int LOGIN_RESPONSE = 0x1012;

	// configuracoes globais
	ServerConfig serverConfig = ServerConfig.getInstance();

	// A camada de Reader agora depende do Serviço, que encapsula a lógica de
	// negócio.
	private static final AuthenticationService authService = new AuthenticationServiceImpl();

	/**
	 * ExecutorService dedicado para operações bloqueantes (JDBC/banco de dados).
	 * Usa Virtual Threads do Java 21 para que cada chamada bloqueante rode em sua
	 * própria thread virtual leve, sem jamais bloquear as threads de I/O do Netty.
	 *
	 * Virtual Threads são extremamente baratas (~1KB de stack inicial vs ~1MB de
	 * platform threads), então este executor pode lidar com milhares de tarefas
	 * simultâneas sem problemas.
	 */
	private static final ExecutorService DB_EXECUTOR = Executors
			.newThreadPerTaskExecutor(Thread.ofVirtual().name("db-vthread-", 0).factory());

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_LOGIN/ADMIN (0x" + Integer.toHexString(LOGIN_REQUEST) + ")");

		String userId = "N/A";
		try {
			// 1. Decodificar o nome de usuário (operação CPU-bound, rápida — OK no
			// EventLoop)
			byte[] usernameEncryptedBlock = Arrays.copyOfRange(payload, 0, 0x10);
			byte[] usernameDecryptedBytes = GunBoundCipher.gunboundStaticDecrypt(usernameEncryptedBlock);
			userId = new String(usernameDecryptedBytes, StandardCharsets.ISO_8859_1).trim();

			byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();
			if (authToken == null) {
				// Usar IllegalStateException para erros de programação/estado inesperado
				throw new IllegalStateException("Token de autenticação não encontrado na sessão.");
			}

			// A senha está nos blocos dinâmicos que começam no offset 0x20
			byte[] passwordEncryptedBlocks = Arrays.copyOfRange(payload, 0x20, payload.length);

			// -------------------------------------------------------------------
			// REFATORAÇÃO: Offloading da chamada bloqueante para Virtual Thread
			// -------------------------------------------------------------------
			// Captura variáveis finais para uso dentro do lambda
			final String finalUserId = userId;
			final byte[] finalPayload = payload;
			final byte[] finalAuthToken = authToken;
			final byte[] finalPasswordBlocks = passwordEncryptedBlocks;

			// 2. Executa a autenticação (JDBC bloqueante) em uma Virtual Thread
			CompletableFuture.supplyAsync(() -> {
				try {
					return authService.loginWithToken(finalUserId, finalPasswordBlocks, finalAuthToken, LOGIN_REQUEST);
				} catch (AuthenticationException e) {
					throw new java.util.concurrent.CompletionException(e);
				}
			}, DB_EXECUTOR).whenCompleteAsync((authenticatedUser, throwable) -> {
				// 3. Este callback executa na thread do EventLoop do Netty (ctx.executor()),
				// garantindo que toda manipulação de Channel/ByteBuf seja thread-safe.
				if (throwable != null) {
					// Desempacota a CompletionException para pegar a causa real
					Throwable cause = throwable;
					if (cause instanceof java.util.concurrent.CompletionException && cause.getCause() != null) {
						cause = cause.getCause();
					}

					handleLoginError(ctx, finalUserId, cause);
				} else {
					// Autenticação bem-sucedida — processar no EventLoop do Netty
					processLoginSuccess(ctx, finalPayload, authenticatedUser, finalAuthToken);
				}
			}, ctx.executor()); // <-- IMPORTANTE: volta para o EventLoop do Netty

		} catch (Exception e) {
			// Captura erros que ocorrem ANTES do async (descriptografia estática, estado
			// inválido)
			System.err.println("Erro crítico ao processar login para '" + userId + "': " + e.getMessage());
			e.printStackTrace();
			ctx.close();
		}
	}

	/**
	 * Trata erros de login de forma centralizada.
	 * Este método é chamado no EventLoop do Netty, então é seguro manipular o
	 * canal.
	 */
	private static void handleLoginError(ChannelHandlerContext ctx, String userId, Throwable cause) {
		if (cause instanceof AuthorityException) {
			System.err.println(cause.getMessage());
			int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();
			PacketWriter.sendPlayerAnError(ctx, currentTxSum, LOGIN_RESPONSE, GameError.LOGIN_PROIBIDO.getId());

		} else if (cause instanceof AuthenticationException) {
			System.err.println("Falha na autenticação para '" + userId + "': " + cause.getMessage());
			int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();
			PacketWriter.sendPlayerAnError(ctx, currentTxSum, LOGIN_RESPONSE, GameError.LOGIN_PROIBIDO.getId());

		} else {
			System.err.println("Erro crítico ao processar login para '" + userId + "': " + cause.getMessage());
			cause.printStackTrace();
			ctx.close();
		}
	}

	/**
	 * Processa as etapas após um login bem-sucedido.
	 * IMPORTANTE: Este método DEVE ser chamado no EventLoop do Netty
	 * (ctx.executor()).
	 */
	private static void processLoginSuccess(ChannelHandlerContext ctx, byte[] originalPayload, UserDTO user,
			byte[] authToken) {
		// Cria e configura a sessão do jogador
		PlayerSession session = new PlayerSession(user, ctx);

		try {
			// Extrai a versão do cliente do payload original
			byte[] versionEncryptedBlocks = Arrays.copyOfRange(originalPayload, 0x20, originalPayload.length);

			int clientVersion = authService.checkVersion(user.getUserId(), user.getPassword(), versionEncryptedBlocks,
					authToken, LOGIN_REQUEST);

			ctx.channel().attr(GameAttributes.CLIENT_VERSION).set(clientVersion);

			PlayerSessionManager psm = PlayerSessionManager.getInstance();

			// verifica se esse player tem outra conexão em aberto para fecha-la
			PlayerSession psAlreadyOn = psm.getSessionPlayerByNickname(user.getNickname());
			if (psAlreadyOn != null) {
				psm.removePlayer(psAlreadyOn.getPlayerCtxChannel());
				psAlreadyOn.getPlayerCtx().close();
			}
			// ----------------------------------------------------------

			// Adiciona o player ao manager de sessoes
			psm.addPlayer(session);
			ctx.channel().attr(GameAttributes.USER_SESSION).set(session);

			// Envia pacote de sucesso e agenda a próxima etapa
			ByteBuf loginPayload = writeLoginSuccess(session, authToken);
			ByteBuf finalPacket = PacketUtils.generatePacket(session, LOGIN_RESPONSE, loginPayload, false);

			ctx.writeAndFlush(finalPacket).addListener(future -> {
				if (future.isSuccess()) {
					System.out.println("Pacote de login (0x1012) enviado. Agendando entrada no canal em 150ms.");
					ctx.channel().eventLoop().schedule(() -> {
						LobbyJoin.read(ctx, null); // Nao tem payload
					}, 150, java.util.concurrent.TimeUnit.MILLISECONDS);
				} else {
					System.err.println("Falha ao enviar o pacote de login (0x1012): " + future.cause().getMessage());
					ctx.close();
				}
			});

		} catch (VersionException e) {
			// Se o serviço lançou nossa exceção personalizada, o login falhou.
			System.err.println("Falha na autenticação para '" + session.getUserNameId() + "': " + e.getMessage());
			int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();
			PacketWriter.sendPlayerAnError(ctx, currentTxSum, LOGIN_RESPONSE, GameError.WRONG_VERSION.getId());
		} catch (Exception e) {
			// Captura qualquer outro erro (descriptografia estática, estado inválido, etc.)
			System.err.println(
					"Erro crítico no processLoginSuccess para '" + session.getUserNameId() + "': " + e.getMessage());
			e.printStackTrace();
			ctx.close();
		}

		// Escreve os Stats do player aqui
		ctx.channel().eventLoop().schedule(() -> {
			requestPlayerStats(session);
		}, 150, java.util.concurrent.TimeUnit.MILLISECONDS);
	}

	private static ByteBuf writeLoginSuccess(PlayerSession session, byte[] authToken) {

		// carrega as configurações
		ServerConfig serverConfig = ServerConfig.getInstance();

		// Para ativar Avatares, Thor e um Evento
		int enabledFunctionsMultiple = FunctionRestrict.getFunctionValue(FunctionRestrict.EFFECT_FORCE,
				FunctionRestrict.EFFECT_TORNADO, FunctionRestrict.EFFECT_LIGHTNING, FunctionRestrict.EFFECT_WIND,
				FunctionRestrict.EFFECT_THOR, FunctionRestrict.EFFECT_MOON, FunctionRestrict.EFFECT_ECLIPSE);

		ByteBuf buffer = Unpooled.buffer();

		// --- Campos do Pacote ---
		buffer.writeBytes(new byte[] { 0x00, 0x00 }); // ????
		buffer.writeBytes(session.getPlayerCtxChannel().attr(GameAttributes.SESSION_UNIQUE).get()); // session_unique
		// (4 bytes)
		buffer.writeBytes(Utils.resizeBytes(session.getNickName().getBytes(StandardCharsets.ISO_8859_1), 0xC)); // Username
		buffer.writeByte(session.getGender());
		buffer.writeBytes(Utils.resizeBytes(session.getGuild().getBytes(StandardCharsets.UTF_8), 8)); // Guild (8
																										// bytes)

		buffer.writeShortLE(session.getRankCurrent());
		buffer.writeShortLE(session.getRankSeason());

		// Valores padrão para campos não implementados
		buffer.writeShortLE(session.getMemberGuildCount()); // MemberCount Guild
		buffer.writeIntLE(session.getTotalRank()); // TotalRank
		buffer.writeIntLE(session.getSeasonRank()); // SeasonRank
		buffer.writeIntLE(session.getGuildRank()); // GuildRank

		buffer.writeBytes(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 });
		int avgHit = Math.abs(session.getAccumDamage() / Math.max(session.getAccumShot(), 1));
		buffer.writeIntLE(avgHit);

		buffer.writeIntLE(session.getTotalScore()); // TotalScore (GP)
		buffer.writeIntLE(session.getSeasonScore()); // SeasonScore (GP da Temporada)
		buffer.writeIntLE(session.getGold());
		// Scores de Evento (zerados)
		buffer.writeIntLE(0); // EventScore0
		buffer.writeIntLE(0); // EventScore1
		buffer.writeIntLE(0); // EventScore2
		buffer.writeIntLE(0); // EventScore3

		// --- Parte 2: Dados a Serem Criptografados ---

		// Cria um buffer temporário para os dados que serão criptografados
		ByteBuf dataToEncrypt = Unpooled.buffer();
		Integer func = serverConfig.getGameServerFuncRestrict();
		dataToEncrypt.writeIntLE(func != null ? func : 1040384);
		dataToEncrypt.writeIntLE(serverConfig.getGameServerScoreFactor()); // ScoreFactor
		dataToEncrypt.writeIntLE(serverConfig.getGameServerGoldFactor()); // GoldFactor

		try {
			// Converte o buffer temporário para um array de bytes
			byte[] plainBytes = new byte[dataToEncrypt.readableBytes()];
			dataToEncrypt.readBytes(plainBytes);

			// Criptografa o array de bytes
			byte[] encryptedBytes = GunBoundCipher.gunboundDynamicEncrypt(plainBytes, session.getUserNameId(),
					session.getPassword(), authToken, 0x1012 // Opcode 4114 em decimal
			);

			// Adiciona os dados criptografados ao buffer principal
			buffer.writeBytes(encryptedBytes);
		} catch (Exception e) {
			System.err.println("Falha ao criptografar o bloco de FuncRestrict no login.");
			e.printStackTrace();
		} finally {
			// Libera o buffer temporário
			dataToEncrypt.release();
		}

		return buffer;
	}

	public static void requestPlayerStats(PlayerSession ps) {
		System.err.println("Enviando stats");
		if (ps == null) {
			return;
		}

		try {
			// 1. Constrói o corpo do pacote de resposta usando o Writer
			ByteBuf responsePayload = PlayerStatsWriter.build(ps.getStats());

			// 2. Gera o pacote final com cabeçalho, etc.
			ByteBuf finalPacket = PacketUtils.generatePacket(ps, PlayerStatsWriter.OPCODE_RESPONSE, responsePayload,
					false);

			// 3. Envia para o cliente
			ps.getPlayerCtx().writeAndFlush(finalPacket);

			System.out.println("Enviado pacote de estatísticas para: " + ps.getNickName());

		} catch (Exception e) {
			System.err.println("Erro ao processar a requisição de estatísticas: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
