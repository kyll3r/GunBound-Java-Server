package br.com.gunbound.emulator.buddy.packet.readers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import br.com.gunbound.emulator.GlobalConfig;
import br.com.gunbound.emulator.buddy.config.enums.BuddyOpcodes;
import br.com.gunbound.emulator.buddy.config.enums.BuddySessionState;
import br.com.gunbound.emulator.buddy.db.exceptions.BuddyAuthException;
import br.com.gunbound.emulator.buddy.db.services.BuddyAuthService;
import br.com.gunbound.emulator.buddy.db.services.BuddyListService;
import br.com.gunbound.emulator.buddy.db.services.BuddyUserService;
import br.com.gunbound.emulator.buddy.db.services.impl.BuddyAuthServiceImpl;
import br.com.gunbound.emulator.buddy.db.services.impl.BuddyListServiceImpl;
import br.com.gunbound.emulator.buddy.db.services.impl.BuddyUserServiceImpl;
import br.com.gunbound.emulator.buddy.entities.BuddyList;
import br.com.gunbound.emulator.buddy.entities.BuddyPlayerSession;
import br.com.gunbound.emulator.buddy.entities.BuddySessionManager;
import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;
import br.com.gunbound.emulator.buddy.packet.writers.BuddyMail;
import br.com.gunbound.emulator.buddy.utils.BuddyCipher;
import br.com.gunbound.emulator.buddy.utils.BuddyUtils;
import br.com.gunbound.emulator.buddy.utils.PacketBuddyUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class LoginPacketReader {

	private static final String SS = GlobalConfig.getProperty("GBBK");
	//private static final byte[] FIXED_AES_KEY = Utils.hexStringToByteArray("2c45926cf3396642b670d006a1fa8182");
	private static final byte[] FIXED_AES_KEY = Utils.hexStringToByteArray(SS);
	private static final BuddySessionManager buddySessionManager = BuddySessionManager.getInstance();
	private static final int REQUEST = BuddyOpcodes.BUDDY_SESSION_AUTHENTICATION.getId();

	// A camada de Reader agora depende do Serviço, que encapsula a lógica de
	// negócio.
	private static final BuddyAuthService authService = new BuddyAuthServiceImpl();
	private static final BuddyListService blService = new BuddyListServiceImpl();
	private static final BuddyUserService userService = new BuddyUserServiceImpl();

	private static final ExecutorService workerPool = Executors.newCachedThreadPool();

	public static void read(BuddyPlayerSession session, ByteBuf payload) {
		System.out.println("RECV> SVC_LOGIN (0x" + Integer.toHexString(REQUEST) + ")");

		System.out
				.println("LoginPacketReader: Payload recebido para processar >> " + payload.readableBytes() + " bytes");

		if (session == null) {
			System.err.println("Erro crítico: Sessão do jogador é nula no LoginPacketReader.");
			// Não podemos fechar o canal aqui sem o contexto, o handler deve fazer isso.
			return;
		}

		// --- VALIDAÇÃO INICIAL ---
		if (payload.readableBytes() < 80) {
			System.err.println("Pacote 0x1010 com tamanho de payload incorreto. Esperado: 80, Recebido: "
					+ payload.readableBytes());
			session.ctx().close();
			return;
		}

		// --- 1. DIVIDIR O PAYLOAD DIRETAMENTE DO BYTEBUF ---
		// Lemos diretamente do ByteBuf para evitar cópias de memória desnecessárias
		byte[] part1_encrypted = new byte[32];
		payload.readBytes(part1_encrypted);

		byte[] part2_encrypted = new byte[48];
		payload.readBytes(part2_encrypted);

		String user = "N/A";

		try {
			// --- 2. PROCESSAR A PARTE 1 (CHAVE FIXA) ---
			byte[] part1_decrypted = BuddyCipher.aesDecryptBlock(part1_encrypted, FIXED_AES_KEY);

			// Ler os dados descriptografados
			ByteBuf part1Buffer = Unpooled.wrappedBuffer(part1_decrypted);

			// Criando o array de bytes para userId e authDword
			byte[] userIdBytes = new byte[16];
			part1Buffer.readBytes(userIdBytes); // Lê os 16 primeiros bytes

			byte[] authDword = new byte[4];
			part1Buffer.readBytes(authDword); // Lê os próximos 4 bytes

			System.out.println("Parte 1 OK. UserID (hex): " + Utils.bytesToHex(userIdBytes) + ", AuthDword: "
					+ Utils.bytesToHex(authDword));

			user = new String(userIdBytes, StandardCharsets.ISO_8859_1).trim();

			BuddyUserDTO authenticatedUser = authService.loginWithToken(user, part2_encrypted, authDword);
			session.setUser(authenticatedUser);
			session.setAuthToken(authDword);
			
			processLogin(session, part2_encrypted);

		} catch (BuddyAuthException e) {
			System.err.println("Falha na autenticação para '" + user + "': " + e.getMessage());
			e.printStackTrace();
			session.ctx().close();
		} catch (Exception e) {
			System.err.println("Falha crítica ao descriptografar ou processar a requisição de login:");
			e.printStackTrace();
			session.ctx().close();
		}

	}

	private static void processLogin(BuddyPlayerSession session, byte[] part2_encrypted) throws Exception {

		// --- 3. PROCESSAR A PARTE 2 (CHAVE DINÂMICA) ---
		byte[] part2_decrypted = BuddyCipher.gunboundDynamicDecrypt(part2_encrypted, session.getUser().getUserId(),
				session.getUser().getPassword(), session.getAuthToken(), 0x0);

		System.out.println("Parte 2 OK. Payload descriptografado (36 bytes): " + Utils.bytesToHex(part2_decrypted));

		// --- 4. LER OS DADOS FINAIS DA PARTE 2 ---
		ByteBuf finalPayload = Unpooled.wrappedBuffer(part2_decrypted);

		// Agora, use os métodos LE (Little Endian) diretamente
		int playerState = finalPayload.readIntLE(); // Lê um inteiro em Little Endian
		byte[] passwordContainer = new byte[20];
		finalPayload.readBytes(passwordContainer); // Lê 20 bytes

		int udpIp = finalPayload.readIntLE(); // Lê o IP em Little Endian
		short udpPortSigned = finalPayload.readShortLE(); // Lê o short da porta em Little Endian
		int udpPort = udpPortSigned & 0xFFFF; // Converte para unsigned

		// Criando os bytes da porta (2 bytes)
		byte[] udpPortBytes = new byte[2];
		finalPayload.getBytes(finalPayload.readerIndex(), udpPortBytes); // Lê 2 bytes diretamente

		System.out.println("Dados da Sessão: Estado=" + playerState + ", IP UDP=" + BuddyUtils.intToIpLE(udpIp)
				+ ", Porta UDP=" + (int) (udpPort & 0xFFFF));
		System.out.println("Sessão identificador>>" + Utils.bytesToHex(session.getSessionUniqueId()));

		// --- 5. ATUALIZAR A SESSÃO DO JOGADOR ---
		session.setState(BuddySessionState.AUTHENTICATED);
		// session.setAuthToken(authDword);
		session.setUdpPortListen(udpPort);
		session.setUdpPortBytes(udpPortBytes);
		// session.setUdpAddress(udpIp, udpPort); // Atualize a sessão com os dados P2P

		System.out.println("Sessão do jogador " + session.getUser().getNickname() + " autenticada com sucesso no BuddyServer.");

		String pwd = new String(passwordContainer, StandardCharsets.UTF_8);
		System.out.println("UserId: " + session.getUser().getUserId() + " password:" + pwd);

		// so vai setar o usuario se o login senha e auth token baterem
		// session.setUser(queriedUser);

		// --- Parte6: Finalizar configuração da sessão ---
		buddySessionManager.addPlayer(session);

		svcLoginOK(session);

		// Verificar mensagens e convites enviados offline
		workerPool.submit(() -> {
			session.ctx().channel().eventLoop().schedule(() -> {
				BuddyMail.mailCheck(session);
			}, 300, TimeUnit.MILLISECONDS);
		});

	}

	private static void svcLoginOK(BuddyPlayerSession session) {

		List<BuddyList> buddyList = blService.findBuddiesByUserId(session.getUser().getUserId());

		// melhorar isso depois add os amigos vinculados ao player
		session.setFriendList(buddyList);

		int sizeBuddyList = buddyList.size();

		List<BuddyUserDTO> userList = new ArrayList<BuddyUserDTO>();

		ByteBuf response = session.ctx().alloc().buffer();

		response.writeShortLE(0);
		response.writeBytes(session.getSessionUniqueId());
		response.writeBytes(
				Utils.resizeBytes(session.getUser().getNickname().getBytes(StandardCharsets.ISO_8859_1), 0xC));
		response.writeBytes(Utils.resizeBytes(session.getUser().getGuild().getBytes(StandardCharsets.ISO_8859_1), 8)); // Guild
																														// 8b
		response.writeShortLE(session.getUser().getTotalGrade());
		response.writeShortLE(sizeBuddyList);

		buddyList.forEach(player -> {
			BuddyUserDTO queriedUser = userService.findUserByUserId(player.getBuddyId());// pegar dados do amigo
			userList.add(queriedUser);
			response.writeBytes(Utils.resizeBytes(queriedUser.getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
			response.writeBytes(Utils.resizeBytes(queriedUser.getNickname().getBytes(StandardCharsets.ISO_8859_1), 13)); // nick
																															// 13b
			response.writeBytes(Utils.resizeBytes(queriedUser.getGuild().getBytes(StandardCharsets.ISO_8859_1), 8)); // Guild
																														// 8b
			response.writeShortLE(queriedUser.getTotalGrade());
		});

		PacketBuddyUtils.sendPacket(session.ctx(), BuddyOpcodes.BUDDY_SESSION_AUTHENTICATION_REPLY.getId(), response);

		sendBuddiesStatus(session, userList);

	}

	public static void sendBuddiesStatus(BuddyPlayerSession session, List<BuddyUserDTO> userList) {

		ByteBuf response = session.ctx().alloc().buffer();
		response.writeShortLE(userList.size());

		userList.forEach(player -> {

			BuddyPlayerSession playerSession = buddySessionManager.getSessionPlayerById(player.getUserId());

			response.writeBytes(Utils.resizeBytes(player.getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
			if (playerSession == null) {
				// response.writeBytes(new byte[] {(byte)0x35,(byte)0x04});

				response.writeByte(0);
			} else {

				// response.writeBytes(new byte[] {(byte)0x5F,(byte)0x08});
				// response.writeBytes(new byte[] {(byte)0x35,(byte)0x04});
				response.writeByte(1);
				response.writeBytes(playerSession.getIpBytes());
				response.writeBytes(playerSession.getUdpPortBytes());
				response.writeBytes(playerSession.getIpBytes());
				response.writeBytes(playerSession.getUdpPortBytes());
			}
		});

		// resp opcode 0x3FFF
		PacketBuddyUtils.sendPacket(session.ctx(), BuddyOpcodes.BUDDY_STATUS_SYNC.getId(), response);

		// Notificando amigos sobre o status do player ao entrar no game.
		notifyBuddiesStatus(session, userList);

	}

	private static void notifyBuddiesStatus(BuddyPlayerSession session, List<BuddyUserDTO> userList) {

		userList.forEach(player -> {

			BuddyPlayerSession playerSession = buddySessionManager.getSessionPlayerById(player.getUserId());
			if (playerSession != null) {
				ByteBuf response = playerSession.ctx().alloc().buffer();
				response.writeShortLE(1);
				response.writeBytes(
						Utils.resizeBytes(session.getUser().getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
				response.writeBytes(session.getIpBytes());
				response.writeBytes(session.getUdpPortBytes());
				response.writeBytes(session.getIpBytes());
				response.writeBytes(session.getUdpPortBytes());

				// resp opcode 0x3FFF
				PacketBuddyUtils.sendPacket(playerSession.ctx(), BuddyOpcodes.BUDDY_STATUS_SYNC.getId(), response);
			}

		});

	}
}