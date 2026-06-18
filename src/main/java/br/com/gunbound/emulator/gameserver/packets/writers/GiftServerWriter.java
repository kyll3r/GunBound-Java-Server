package br.com.gunbound.emulator.gameserver.packets.writers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptGiftDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.ReceiptService;
import br.com.gunbound.emulator.services.impl.ReceiptServiceImpl;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GiftServerWriter {
	private static final ReceiptService receiptService = new ReceiptServiceImpl();

	/**
	 * Estrutura do pacote 0x6005 - Mensagem de Gift
	 * 
	 * Este pacote é enviado quando um jogador envia uma mensagem no chat ou realiza
	 * uma ação relacionada a itens (gifts) no jogo.
	 * 
	 * @size Tamanho variável: 27 bytes (fixo) + tamanho da mensagem
	 * 
	 *       Campos: - header: 25 00 DB 83 05 60 (6 bytes) - item_id: 01 00 00 00
	 *       (ID do item: 0x01000000) - nick: "PROZITO" (12 bytes com padding null)
	 *       - timestamp: A0 DE 52 69 (0x6952DEA0) - message_length: 05 (5 bytes) -
	 *       message: "teste"
	 * 
	 * @note O nick sempre ocupa 12 bytes, preenchido com null se menor
	 * @note A mensagem tem tamanho variável definido por message_length
	 */

	private static void sendGiftBoxMsg(PlayerSession player, ReceiptGiftDTO rGift) {

		byte[] messageBytes = rGift.getText().getBytes(StandardCharsets.ISO_8859_1);

		ByteBuf buffer = Unpooled.buffer();

		buffer.writeIntLE(rGift.getIdxChest());
		buffer.writeBytes(Utils.resizeBytes(rGift.getSenderNick().getBytes(StandardCharsets.ISO_8859_1), 12));
		buffer.writeIntLE(Utils.convertTimestampToInt(rGift.getGiftTime()));
		buffer.writeByte(messageBytes.length);
		buffer.writeBytes(messageBytes);

		// --- Campos do Pacote ---
		// buffer.writeBytes(new byte[] {
		// 0x03,0x00,0x00,0x00,0x74,0x65,0x73,0x74,0x74,0x65,0x73,0x74,0x74,0x65,0x73,0x74,(byte)
		// 0xA0, (byte) 0xDE, 0x52, 0x69 ,0x05,0x41,0x41,0x41,0x41,0x75,}); // ????

		ByteBuf finalPacket = PacketUtils.generatePacket(player, 0x6005, buffer, false);

		player.getPlayerCtxChannel().eventLoop().execute(() -> {
			player.getPlayerCtx().writeAndFlush(finalPacket);
		});

	}

	private static void getPlayerReceviedAnyGift(PlayerSession player) {
		List<ReceiptGiftDTO> receiptGift = receiptService.getPlayerReceiptGiftItems(player);
		//limpa lista
		player.getPlayerGiftReceived().clear();
		//popula novamente
		player.setPlayerGiftReceived(receiptGift);
	}

	public static void checkPlayerReceivedAnyGift(PlayerSession player) {
		getPlayerReceviedAnyGift(player);

		for (ReceiptGiftDTO gifts : player.getPlayerGiftReceived()) {
			sendGiftBoxMsg(player, gifts);
		}

	}

}
