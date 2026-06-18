package br.com.gunbound.emulator.gameserver.packets.readers.shop;

import java.util.Optional;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerAvatar;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.ShopService;
import br.com.gunbound.emulator.services.impl.ShopServiceImpl;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class AvatarPlayerGiftSendReader {
	
	// Instancia o serviço que contém a lógica de negócio
	private static final ShopService shopService = new ShopServiceImpl();
	private static final int OPCODE_REQUEST = 0x6028;
	private static final int OPCODE_CONFIRMATION = 0x6029;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ITEM_GIFT (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
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
			int avatarIdx = request.readIntLE(); // Os 4 primeiros bytes é o avatar na chest (IDX)
			String targetIdPlayer = Utils.stringDecode(request.readBytes(16));
			String targetNickname = Utils.stringDecode(request.readBytes(12));
			
			int msgSize = request.readByte(); // tamanho da msg
			String msgText = Utils.stringDecode(request.readBytes(msgSize));
			
			 // Buscando o PlayerAvatar pelo idx
	        Optional<PlayerAvatar> hasAvatar = player.getPlayerAvatars().stream()
	                                                     .filter(p -> p.getIdx() == avatarIdx)
	                                                     .findFirst();

	        //Atualiza na tabela o ownerdId do avatar em questao
	        shopService.giftItem(player, avatarIdx,targetIdPlayer,targetNickname,Utils.getStringCharacters(msgText,50));

			
			System.out.println(avatarIdx);
			System.out.println(targetIdPlayer);
			System.out.println(targetNickname);
			System.out.println(msgText);
			
			
			ByteBuf response = Unpooled.buffer(4);
			response.writeIntLE(avatarIdx);
			
			ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION, Unpooled.EMPTY_BUFFER,true);
			player.getPlayerCtx().writeAndFlush(finalPacket);

		} catch (Exception e) {
			System.err.println("Erro ao processar gift de avatar");
			ctx.close();
			e.printStackTrace();
		} finally {
			request.release();
		}

		
	}
		
		//packet para erro no shop
		// 09 00 A0 6C 21 60 04 00 00 

}