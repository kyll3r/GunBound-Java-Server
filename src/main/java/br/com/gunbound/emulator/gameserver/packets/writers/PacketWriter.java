package br.com.gunbound.emulator.gameserver.packets.writers;

import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class PacketWriter {

	public static ByteBuf writeJoinNotification(PlayerSession player) {
		ByteBuf buffer = Unpooled.buffer();

		System.out.println("DEBUG : ChannelPosition no writeJoinNotification: " + player.getChannelPosition());

		// Posição (slot) do jogador no canal.
		buffer.writeByte(player.getChannelPosition());

		System.out.println(
				"DEBUG : ChannelPosition no writeJoinNotification (byte) : " + (byte) player.getChannelPosition());

		// Nickname (13 bytes)
		buffer.writeBytes(Utils.resizeBytes(player.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12));
		buffer.writeByte(player.getGender());
		// Guild (8 bytes)
		buffer.writeBytes(Utils.resizeBytes(player.getGuild().getBytes(StandardCharsets.ISO_8859_1), 8));

		// Avatar Equipado (a string hexadecimal é convertida para bytes)
		// O código de referência usa 8 bytes para o avatar aqui.
		// byte[] avatarBytes = Utils.hexStringToByteArray(player.getAvatarEquipped());
		// buffer.writeBytes(Utils.resizeBytes(avatarBytes, 8));

		// Ranks (2 bytes cada)
		buffer.writeShortLE(player.getRankCurrent());
		buffer.writeShortLE(player.getRankSeason());

		// PPNNNNNNNNNNNNNNNNNNNNNNNNAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGGRCRCRSRS
		// buffer.writeBytes(Utils.hexStringToByteArray(("014142434445464748494A4B4C|14|4E4F50515253544E|565857595A|4142434445464748494A4B4C4D|4E4F505152535455|565857595A").replace("|",
		// "")));
		// buffer.writeBytes(Utils.hexStringToByteArray(("014142434445464748494A4B4C|00|4E4F50515253544E|565857595A|4142434445464748494A4B4C4D|4E4F505152535455|565857595A").replace("|",
		// "")));

		return buffer;
	}

	public static void sendPlayerAnError(PlayerSession player, int opcode, int error, boolean rtc, boolean close) {
		// Senha incorreta, rejeita o cliente.
		System.out.println("Enviando erro ao cliente: (0x" + Integer.toHexString(error) + ")");
		// 10 00 é para login incorreto eu particularmente acho errado avisar se é o
		// login ou senha incorreto
		// 60 versao incorreta
		// 30 login proibido (prefiro essa porque fecha o cliente)

		ByteBuf errorBuf = Unpooled.buffer(2); // 2 bytes para o código de erro
		errorBuf.writeShortLE(error); // writeShort escreve em little endian por padrão

		ByteBuf finalPacket = PacketUtils.generatePacket(player, opcode, errorBuf, rtc);
		// Envia o pacote com erro.
		player.getPlayerCtx().writeAndFlush(finalPacket);

		// sendPacket(ctx, 0x1012, hexStringToBytes("1100")); // Erro: Senha ou user
		// incorretos
		if (close) {
			player.getPlayerCtx().close();
		}
	}

	public static void sendPlayerAnError(ChannelHandlerContext ctx, int currentTxSum, int opcode, int error) {
		System.out.println("Enviando erro ao cliente: (0x" + Integer.toHexString(error) + ")");

		ByteBuf errorBuf = Unpooled.buffer(2); // 2 bytes para o código de erro
		errorBuf.writeShortLE(error);

		ByteBuf finalPacket = PacketUtils.generatePacket(currentTxSum, opcode, errorBuf);
		// Envia o pacote com erro.
		ctx.writeAndFlush(finalPacket);
		ctx.close();
	}

}