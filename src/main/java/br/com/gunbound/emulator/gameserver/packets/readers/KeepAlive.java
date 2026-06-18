package br.com.gunbound.emulator.gameserver.packets.readers;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import io.netty.channel.ChannelHandlerContext;

public class KeepAlive {
	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		PlayerSession ps = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		
		System.err.println("[KEEPALIVE] de: " + ps.getNickName());
	}
}
