package br.com.gunbound.emulator.gameserver.packets.readers.room;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.gameserver.room.GameRoom;
import br.com.gunbound.emulator.gameserver.room.RoomManager;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.model.entities.game.PlayerSessionManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class RoomKickPlayerReader {
	
	private static final int OPCODE_REQUEST = 0x3150;
	private static final Integer OPCODE_CONFIRMATION = 0x3FFF;
	
	static PlayerSessionManager psm = PlayerSessionManager.getInstance();
	static RoomManager roomMngr = RoomManager.getInstance();
	
	
	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_KICK (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		GameRoom room = player.getCurrentRoom();

		if (player == null || player.getCurrentRoom() == null)
			return;
		
		ByteBuf request = Unpooled.wrappedBuffer(payload);
		int slot = request.readByte();
		
		PlayerSession kicked = room.getPlayersBySlot().get(slot);
		
		if(kicked == null)
			return;
		
		if(room.getRoomMaster().equals(player) && kicked.getAuthority() < 99) {

		// kicka o player mencionado
		removePlayerFromRoom(room,kicked);
		
		}
	}

	public static void removePlayerFromRoom(GameRoom room, PlayerSession kickedPlayer) {
		roomMngr.handlePlayerLeave(kickedPlayer);

		ByteBuf confirmationPacket = PacketUtils.generatePacket(kickedPlayer, OPCODE_CONFIRMATION,
				Unpooled.EMPTY_BUFFER, false);

		kickedPlayer.getPlayerCtxChannel().writeAndFlush(confirmationPacket)
				.addListener((ChannelFutureListener) future -> {
					if (!future.isSuccess()) {
						System.err.println("Falha ao fechar sala para: " + kickedPlayer.getNickName());
						future.cause().printStackTrace();
						// Caso o jogador nao esteja impossibilitado de receber pacotes.
						kickedPlayer.getPlayerCtxChannel().close();
					} else {
						System.out.println("RoomID: " + (room.getRoomId() + 1) + ", Command Sent: '0x"
								+ Integer.toHexString(OPCODE_CONFIRMATION) + "'");
					}
				});
	}
}