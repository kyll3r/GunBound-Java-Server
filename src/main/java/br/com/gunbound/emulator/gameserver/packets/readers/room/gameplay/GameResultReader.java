package br.com.gunbound.emulator.gameserver.packets.readers.room.gameplay;

import java.util.Map;

import org.mariadb.jdbc.plugin.authentication.standard.ed25519.Utils;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.gameserver.room.GameRoom;
import br.com.gunbound.emulator.model.entities.game.PlayerGameResult;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.PlayLogService;
import br.com.gunbound.emulator.services.UserService;
import br.com.gunbound.emulator.services.impl.PlayLogServiceImpl;
import br.com.gunbound.emulator.services.impl.UserServiceImpl;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class GameResultReader {

	private static final int OPCODE_REQUEST = 0x4412;
	private static final int OPCODE_CONFIRMATION = 0x4413;

	// Instancias de serviços
	private static final PlayLogService playlogService = new PlayLogServiceImpl();
	private static final UserService userService = new UserServiceImpl();

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PLAY_RESULT (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession ps = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (ps == null)
			return;

		GameRoom room = ps.getCurrentRoom();
		if (room == null)
			return;

		try {
			// 1. Descriptografa o payload do chat
			byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();
			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, ps.getUserNameId(),
					ps.getPassword(), authToken, OPCODE_REQUEST);

			System.out.println("ResultGame decrypted >> " + Utils.bytesToHex(decryptedPayload));

			room.getRoomEventLoop().schedule(() -> {
				processEndGame(room, decryptedPayload);
			}, 500, java.util.concurrent.TimeUnit.MILLISECONDS);

			// processEndGame(room, decryptedPayload);

		} catch (Exception e) {
			System.err.println("Erro ao processar pacote ResultGame: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public static void processEndGame(GameRoom room, byte[] payload) {

		ByteBuf rcvPayload = Unpooled.wrappedBuffer(payload);

		try {
			int qtdPlayers = rcvPayload.readByte();

			for (int i = 0; i < qtdPlayers; i++) {
				rcvPayload.skipBytes(1);

				int slot = rcvPayload.readByte();
				int gold = rcvPayload.readShortLE();
				int bonusGold = rcvPayload.readShortLE();
				int eventPoints = rcvPayload.readShortLE();
				int gp = rcvPayload.readShortLE();
				int hits = rcvPayload.readShortLE(); //acertos
				rcvPayload.readShortLE();
				int damage = rcvPayload.readShortLE(); // dano causado
				rcvPayload.readShortLE();

				PlayerGameResult pResult = new PlayerGameResult(gold, bonusGold, eventPoints, gp, hits,damage);

				System.out.println("pResult >>> SLOT: " + slot + " Valores: " + pResult);

				//Esta aqui pois é preciso usar para salvar na playlog
				room.setResultGameBySlot(slot, pResult);

				// --- USA O UserService PARA PERSISTIR OS DADOS ---
				PlayerSession playerToUpdate = room.getPlayersBySlot().get(slot);
				if (playerToUpdate != null) {
					// A chamada para o serviço é limpa e direta.
					// O serviço, por sua vez, cuida da execução assíncrona.
					userService.applyGameResults(playerToUpdate, pResult);
				}
			}

			playlogService.recordMatchResults(room); // salva nossa playlog

			// skypa o restante dos bytes precisa ser analisado
			if (rcvPayload.readableBytes() > 0) {
				System.out.println("Aviso: " + rcvPayload.readableBytes()
						+ " bytes restantes no final do pacote de resultado. Ignorando-os.");
				rcvPayload.skipBytes(rcvPayload.readableBytes());
			}

			ByteBuf buffer = Unpooled.EMPTY_BUFFER;

			for (Map.Entry<Integer, PlayerSession> entry : room.getPlayersBySlot().entrySet()) {
				PlayerSession player = entry.getValue();
				ByteBuf confirmationPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION, buffer, false);
				player.getPlayerCtxChannel().writeAndFlush(confirmationPacket);
			}

		} catch (Exception e) {
			System.err.println("Erro ao processar Resultado da partida");
			e.printStackTrace();
		} finally {
			rcvPayload.release();
			room.isGameStarted(false);// sala deixa de estar em estado playing
		}

	}

}