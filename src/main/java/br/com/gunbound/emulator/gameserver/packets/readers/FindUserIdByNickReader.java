package br.com.gunbound.emulator.gameserver.packets.readers;

import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.gameserver.packets.writers.PacketWriter;
import br.com.gunbound.emulator.gameserver.room.model.enums.GameError;
import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.UserService;
import br.com.gunbound.emulator.services.impl.UserServiceImpl;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class FindUserIdByNickReader {

	private static final int OPCODE_REQUEST = 0x1020;
	private static final int OPCODE_RESPONSE = 0x1022;

	// A camada de Reader agora depende do Serviço, que encapsula a lógica de
	// negócio.
	private static final UserService userService = new UserServiceImpl();

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_USER_ID (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");

		PlayerSession requesterSession = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (requesterSession == null) {
			ctx.close();
			return;
		}

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {

			// Pulamos esses bytes para chegar ao nome de usuário.
			request.skipBytes(2);

			// 2. Lemos os 12 bytes que contêm o nome de usuário diretamente do payload.
			String targetNickname = Utils.stringDecode(request.readBytes(12));
			System.out.println(requesterSession.getNickName() + " está procurando por: " + targetNickname);

			// 3. Busca a SESSÃO do jogador ALVO.
			UserDTO targetPlayer = userService.findUserByNickName(targetNickname);

			if (targetPlayer != null) {

				// 4. Constrói o payload da resposta.
				ByteBuf responsePayload = writeUserIdResponse(targetPlayer);

				ByteBuf finalPacket = PacketUtils.generatePacket(requesterSession, OPCODE_RESPONSE, responsePayload,
						false);
				ctx.writeAndFlush(finalPacket);
				System.out.println("Enviada resposta de ID de usuário (0x1022) para " + requesterSession.getNickName());
			} else {
				PacketWriter.sendPlayerAnError(requesterSession, OPCODE_RESPONSE, GameError.USER_NOT_FOUND.getId(),
						false,false);
			}
		} catch (Exception e) {
			System.err.println("Erro ao processar SVC_USER_ID: " + e.getMessage());

			e.printStackTrace();
			// ctx.close();
		} finally {
			request.release();
		}
	}

	public static ByteBuf writeUserIdResponse(UserDTO targetSession) {
		ByteBuf buffer = Unpooled.buffer();

		// Se a sessão alvo não for encontrada, retorna um buffer com dados "vazios".
		if (targetSession == null) {
			buffer.writeBytes(new byte[28]); // 16+12 (ID+NICK)
		}

		// Escreve o nome de usuário duas vezes, como na referência
		buffer.writeShortLE(1);
		buffer.writeBytes(Utils.resizeBytes(targetSession.getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
		buffer.writeBytes(Utils.resizeBytes(targetSession.getNickname().getBytes(StandardCharsets.ISO_8859_1), 12));

		return buffer;
	}

}