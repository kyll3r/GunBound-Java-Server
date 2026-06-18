package br.com.gunbound.emulator.gameserver.packets.readers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.model.entities.game.PlayerSessionManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class MessageBcmReader {

	private static final int OPCODE_MSG_BCM_REQUEST = 0x5010; //
	private static final int OPCODE_MSG_BCM_RESPONSE = 0x5101; //

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_BCM_MSG (0x" + Integer.toHexString(OPCODE_MSG_BCM_REQUEST) + ")");
		PlayerSession session = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (session == null || session.getCurrentRoom() == null) {
			return; // Jogador não está logado ou não está em uma sala (Possivel Hack)
		}

		try {
			// 1. Descriptografa o payload do chat
			byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();
			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, session.getUserNameId(),
					session.getPassword(), authToken, OPCODE_MSG_BCM_REQUEST);

			// 2. Decodifica a mensagem (formato: 1 byte de tamanho + N bytes de mensagem)
			int messageLength = decryptedPayload[1] & 0xFF;
			String chatMessage = new String(decryptedPayload, 2, messageLength, StandardCharsets.ISO_8859_1);

			System.out.println("[BCM] " + session.getNickName() + ": " + chatMessage);

			// logs para depuração
			System.out.println("[DEBUG] Iniciando broadcast de: " + session.getNickName());

			// 3. Pede ao canal atual do jogador para fazer o broadcast da mensagem
			broadcastSendMessage(chatMessage);

		} catch (Exception e) {
			System.err.println("Erro ao processar pacote de chat: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void broadcastSendMessage(String message) {
		// Itera sobre todos os jogadores no lobby
		// snapshot para evitar concorrência
		Collection<PlayerSession> recipients = new ArrayList<>(PlayerSessionManager.getInstance().getAllPlayers());
		for (PlayerSession recipient : recipients) {
			try {

				printMsgToPlayer(recipient, message);

			} catch (Exception e) {
				System.err.println("Falha ao enviar bcm para " + recipient.getNickName() + ": " + e.getMessage());
			}
		}
	}

	public static void bznSendMessage(String message) {
		// Itera sobre todos os jogadores no lobby
		// snapshot para evitar concorrência
		Collection<PlayerSession> recipients = new ArrayList<>(PlayerSessionManager.getInstance().getAllPlayers());
		for (PlayerSession player : recipients) {
			try {

				printMsgToPlayer(player, message, 0x9999);

			} catch (Exception e) {
				System.err.println("Falha ao enviar bzn para " + player.getNickName() + ": " + e.getMessage());
			}
		}
	}

	/**
	 * Gera uma mensagem para que determinado player visualize (Sobrescrita)
	 * 
	 * @param player
	 * @param msg
	 */
	public static void printMsgToPlayer(PlayerSession player, String msg) {

		printMsgToPlayer(player, msg, OPCODE_MSG_BCM_RESPONSE);

	}

	/**
	 * Gera uma mensagem para que determinado player visualize (Usado no BCM)
	 * 
	 * @param player
	 * @param msg
	 * @param opcode
	 */
	public static void printMsgToPlayer(PlayerSession player, String msg, int opcode) {

		byte[] messageBytes = Utils.getStringCharacters(msg,60).getBytes(StandardCharsets.ISO_8859_1);
		ByteBuf buffer = Unpooled.wrappedBuffer(messageBytes);

		ByteBuf confirmationPacket = PacketUtils.generatePacket(player, opcode, buffer, false);

		// Envia o pacote individualmente.
		player.getPlayerCtxChannel().eventLoop().schedule(() -> {
			player.getPlayerCtxChannel().writeAndFlush(confirmationPacket);
		}, 100, TimeUnit.MILLISECONDS);

	}

}