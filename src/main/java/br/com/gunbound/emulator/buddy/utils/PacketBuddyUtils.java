package br.com.gunbound.emulator.buddy.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class PacketBuddyUtils {
	public static void sendPacket(ChannelHandlerContext ctx, int opcode, ByteBuf payload) {
		int payloadSize = payload.readableBytes();
		int totalSize = 2 + 2 + payloadSize; // Tamanho + Opcode + Payload

		ByteBuf finalPacket = Unpooled.buffer(totalSize);
		finalPacket.writeShortLE(totalSize);
		finalPacket.writeShortLE(opcode);
		finalPacket.writeBytes(payload);

		//insere na fila para ser executado
		ctx.channel().eventLoop().schedule(() -> {
			ctx.writeAndFlush(finalPacket);
		}, 100, java.util.concurrent.TimeUnit.MILLISECONDS);
		
		// Libera o buffer do payload, já que seus dados foram copiados para o pacote final
		payload.release();
	}
}
