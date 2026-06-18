package br.com.gunbound.emulator.gameserver.packets.readers.room.gameplay;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.gameserver.room.GameRoom;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class GamePlayerDeadReader {

	private static final int OPCODE_REQUEST = 0x4100;
	private static final int OPCODE_CONFIRMATION = 0x4101;
	private static final int OPCODE_BROADCAST_DEAD = 0x4102;
	private static final int OPCODE_BROADCAST_WINNER = 0x4410;

	// Removido o 'synchronized'. A concorrência deve ser gerenciada pelo estado da
	// sala.
	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PLAY_USER_DEAD (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession deadPlayer = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (deadPlayer == null || deadPlayer.getCurrentRoom() == null) {
			return;
		}

		GameRoom room = deadPlayer.getCurrentRoom();
		
		ByteBuf rcvPayload = Unpooled.wrappedBuffer(payload);
		int deadCause = (int) rcvPayload.readByte();
		
		

		// 1. Envia a confirmação 0x4101 de volta para o jogador que morreu.
		ByteBuf confirmationPacket = PacketUtils.generatePacket(deadPlayer, OPCODE_CONFIRMATION, Unpooled.EMPTY_BUFFER,
				false);
		ctx.writeAndFlush(confirmationPacket);

		System.out.println(
				"[DEBUG] Morte do Player: " + deadPlayer.getNickName() + " NO SLOT: " + room.getSlotPlayer(deadPlayer));

		// 2. Atualiza o estado do jogo de forma segura.
		// Usar o executor da sala garante que todas as mudanças de estado aconteçam em
		// ordem.
		room.submitAction(() -> {
			deadPlayer.setIsAlive(0);
			deadPlayer.setDeadCause(deadCause);
			
			//Setar o horario da morte para a playlog
			deadPlayer.setDeadTime(LocalDateTime.now()); 

			if (room.isAscoreRoom()) {
				room.setScoreTeam(deadPlayer.getRoomTeam());
				System.out.println("[DEBUG] Score atualizado para o time " + deadPlayer.getRoomTeam() + ": "
						+ (deadPlayer.getRoomTeam() == 0 ? room.getScoreTeamA() : room.getScoreTeamB()));
			}

			// 3. Anuncia a morte a todos e, ao completar, verifica o fim do jogo.
			
			/*Comentado em 05/11/2025 pode ser o causador do race condicition
			announceDeadPlayer(room, deadPlayer, () -> {
				int winnerTeam = room.checkGameEndAndGetWinner();
				if (winnerTeam != -1 && room.tryTriggerEndGame()) {
					// Agendamos o anúncio do vencedor com um pequeno delay.
					room.getRoomEventLoop().schedule(() -> {
						announceFinalScore(room, winnerTeam);
					}, 300, TimeUnit.MILLISECONDS);
				}
			});*/
			
			int winnerTeam = room.checkGameEndAndGetWinner();

			// Se já terminou, agenda o fim imediatamente.
			if (winnerTeam != -1 && room.tryTriggerEndGame()) {
			    room.getRoomEventLoop().schedule(() -> {
			        announceFinalScore(room, winnerTeam);
			    }, 300, TimeUnit.MILLISECONDS);
			} else {
			    // Caso contrário, apenas anuncia a morte.
			    announceDeadPlayer(room, deadPlayer, null);
			}
		});
		//}, ctx); teste sem inserir contexto do player
	}

	/**
	 * Anuncia a morte de um jogador a todos os outros na sala. Quando todos os
	 * pacotes forem enviados, o callback onComplete é executado.
	 */
	private static void announceDeadPlayer(GameRoom room, PlayerSession deadPlayer, Runnable onComplete) {
		int totalPlayers = room.getPlayersBySlot().size();
		if (totalPlayers == 0) {
			if (onComplete != null)
				onComplete.run();
			return;
		}

		AtomicInteger completionCounter = new AtomicInteger(totalPlayers);
		byte[] payload = buildDeadPlayerPayload(room, deadPlayer);

		for (PlayerSession recipient : room.getPlayersBySlot().values()) {
			try {
				byte[] encryptedPayload = GunBoundCipher.gunboundDynamicEncrypt(payload, recipient.getUserNameId(),
						recipient.getPassword(), recipient.getAuthToken(), OPCODE_BROADCAST_DEAD);

				ByteBuf finalPacket = PacketUtils.generatePacket(recipient, OPCODE_BROADCAST_DEAD,
						Unpooled.wrappedBuffer(encryptedPayload), false);

				// Adiciona um listener que será chamado quando a escrita for concluída (ou
				// falhar).
				recipient.getPlayerCtxChannel().writeAndFlush(finalPacket)
						.addListener((ChannelFutureListener) future -> {
							if (completionCounter.decrementAndGet() == 0 && onComplete != null) {
								onComplete.run();
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
				// Garante que o contador seja decrementado mesmo em caso de erro.
				if (completionCounter.decrementAndGet() == 0 && onComplete != null) {
					onComplete.run();
				}
			}
		}
	}

	private static byte[] buildDeadPlayerPayload(GameRoom room, PlayerSession deadPlayer) {
		byte[] fixedData = Utils.hexStringToByteArray("13000000000000443447"); // Dados fixos do pacote
		byte[] resultBytes = new byte[2 + fixedData.length];
		resultBytes[0] = (byte) room.getSlotPlayer(deadPlayer);
		resultBytes[resultBytes.length - 1] = (byte) deadPlayer.getRoomTeam();
		System.arraycopy(fixedData, 0, resultBytes, 1, fixedData.length);
		return resultBytes;
	}

	/**
	 * Anuncia o time vencedor a todos os jogadores.
	 */
	public static void announceFinalScore(GameRoom room, int winnerTeam) {
		byte[] payload = buildFinalScorePayload(winnerTeam);

		for (PlayerSession player : room.getPlayersBySlot().values()) {
			try {
				byte[] encryptedPayload = GunBoundCipher.gunboundDynamicEncrypt(payload, player.getUserNameId(),
						player.getPassword(), player.getAuthToken(), OPCODE_BROADCAST_WINNER);

				ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_BROADCAST_WINNER,
						Unpooled.wrappedBuffer(encryptedPayload), false);
				room.submitAction(() -> {
					//player.getPlayerCtxChannel().eventLoop().schedule(() -> {
				player.getPlayerCtxChannel().writeAndFlush(finalPacket);
					//}, 300, TimeUnit.MILLISECONDS);
				}, player.getPlayerCtx());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static byte[] buildFinalScorePayload(int winnerTeam) {
		byte[] fixedData = Utils.hexStringToByteArray("0000000000000000000000"); // Dados fixos do pacote
		byte[] resultBytes = new byte[1 + fixedData.length];
		resultBytes[0] = (byte) winnerTeam;
		System.arraycopy(fixedData, 0, resultBytes, 1, fixedData.length);
		return resultBytes;
	}
}
