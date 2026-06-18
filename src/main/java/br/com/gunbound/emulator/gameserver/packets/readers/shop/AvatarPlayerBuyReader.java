package br.com.gunbound.emulator.gameserver.packets.readers.shop;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.gameserver.packets.writers.PacketWriter;
import br.com.gunbound.emulator.gameserver.room.model.enums.GameError;
import br.com.gunbound.emulator.model.entities.DTO.ChestDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.ShopService;
import br.com.gunbound.emulator.services.excepion.ShopException;
import br.com.gunbound.emulator.services.impl.ShopServiceImpl;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class AvatarPlayerBuyReader {

	private static final int OPCODE_REQUEST = 0x6020;
	private static final int OPCODE_CONFIRMATION = 0x6021;

	// Instancia o serviço que contém a lógica de negócio
	private static final ShopService chestService = new ShopServiceImpl();

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PROP_BUY (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();

		try {
			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, player.getUserNameId(),
					player.getPassword(), authToken, OPCODE_REQUEST);

			ByteBuf request = Unpooled.wrappedBuffer(decryptedPayload);

			int avatarCode = request.readIntLE();
			int goldOrCash = request.readByte(); // 0 para Gold, 1 para Cash

			// Chama o serviço para executar toda a lógica de negócio
			try {
				ChestDTO newAvatar = chestService.buyAvatar(player, avatarCode, goldOrCash);

				// Se o serviço executou sem erros, a compra foi bem-sucedida.
				// Enviamos a confirmação para o cliente.
				writeNewAvatarConfirmation(ctx, newAvatar);

			} catch (ShopException e) {
				// Se o serviço lançou uma exceção, a compra falhou por um motivo de negócio.
				System.err.println("Falha na compra para " + player.getNickName() + ": " + e.getMessage());
				PacketWriter.sendPlayerAnError(player, OPCODE_CONFIRMATION, GameError.SHOP_ERROR.getId(), false,true);
			}

		} catch (Exception e) {
			// Captura qualquer outro erro inesperado (ex: descriptografia, erro de pacote)
			System.err.println("Erro crítico ao processar compra de avatar: " + e.getMessage());
			e.printStackTrace();
			PacketWriter.sendPlayerAnError(player, OPCODE_CONFIRMATION, GameError.SHOP_ERROR.getId(), false,true);
		}
	}

	/**
	 * Constrói e envia o pacote de confirmação da compra bem-sucedida para o
	 * cliente.
	 * 
	 * @param ctx       O contexto do canal do jogador.
	 * @param newAvatar O item recém-adicionado ao inventario.
	 */
	private static void writeNewAvatarConfirmation(ChannelHandlerContext ctx, ChestDTO newAvatar) {
		System.out.println("SEND> SVC_ITEM_CONFIRMATION (0x" + Integer.toHexString(OPCODE_CONFIRMATION) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		ByteBuf avatarData = Unpooled.buffer();
		avatarData.writeShort(0);
		avatarData.writeByte(1); // Número de itens comprados
		avatarData.writeByte(0);

		avatarData.writeIntLE(newAvatar.getIdx());
		avatarData.writeIntLE(newAvatar.getItem());

		ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION, avatarData, false);
		player.getPlayerCtxChannel().writeAndFlush(finalPacket);

	}

	// packet para erro no shop
	// 09 00 A0 6C 21 60 04 00 00

}