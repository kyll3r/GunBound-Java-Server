package br.com.gunbound.emulator.gameserver.packets.readers.shop;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.gameserver.packets.writers.PacketWriter;
import br.com.gunbound.emulator.gameserver.room.model.enums.GameError;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.ShopService;
import br.com.gunbound.emulator.services.impl.ShopServiceImpl;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class AvatarPlayerSellReader {

	private static final int OPCODE_REQUEST = 0x6024;
	private static final int OPCODE_CONFIRMATION = 0x6025;
	
	// Instancia o serviço que contém a lógica de negócio
	private static final ShopService shopService = new ShopServiceImpl();

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ITEM_CONSUME (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();

		if (player == null)
			return;

		ByteBuf request = Unpooled.buffer();
		try {

			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, player.getUserNameId(),
					player.getPassword(), authToken, OPCODE_REQUEST);

			request = Unpooled.wrappedBuffer(decryptedPayload);
			
			System.out.println(Utils.bytesToHex(decryptedPayload));

			// 1. Lê o cabeçalho do payload
			int avatarCode = request.readIntLE(); // O primeiro byte é a quantidade
			// request.skipBytes(1); // Pula o byte 0x00 de padding
			int goldOrCash = request.readByte(); // é Gold ou é cash?

			int value = shopService.sellItem(player, avatarCode);
			
			
			ByteBuf response = Unpooled.buffer(4);
			//response.writeIntLE(avatarCode);
			//escreve aqui o valor a ser creditado na UI
			response.writeIntLE(value);
			
			//PacketWriter.sendPlayerAnError(player, OPCODE_CONFIRMATION, GameError.SHOP_ERROR.getId(), false);

			ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION, response,true);
			player.getPlayerCtx().writeAndFlush(finalPacket);

		} catch (Exception e) {
			PacketWriter.sendPlayerAnError(player, OPCODE_CONFIRMATION, GameError.SHOP_ERROR.getId(), false,false);
			System.err.println("Erro ao processar Venda de avatar");
			ctx.close();
			e.printStackTrace();
		} finally {
			request.release();
		}

		
	}
		
		//packet para erro no shop
		// 09 00 A0 6C 21 60 04 00 00 

}