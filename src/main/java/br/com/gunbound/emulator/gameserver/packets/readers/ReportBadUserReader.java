package br.com.gunbound.emulator.gameserver.packets.readers;

import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class ReportBadUserReader {

	private static final int OPCODE_REQUEST = 0x5000;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_REPORT_USER (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");

		PlayerSession requesterSession = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (requesterSession == null) {
			ctx.close();
			return;
		}

		try {

			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, requesterSession.getUserNameId(),
					requesterSession.getPassword(), requesterSession.getAuthToken(), OPCODE_REQUEST);

			ByteBuf request = Unpooled.wrappedBuffer(decryptedPayload);
			
			System.out.println("Payload >>>" + Utils.bytesToHex(decryptedPayload));

			// 2. Lemos os 12 bytes que contêm o nome de usuário diretamente do payload.
			String targetNickname = Utils.stringDecode(request.readBytes(16));
			System.out.println(requesterSession.getNickName() + " está reportando por: " + targetNickname);
			int textSize = request.readByte();
			System.out.println("Tamanho do texto: " + textSize);
			// Se o texto for maior que 0 faz a leitura
			if (textSize > 0) {
				// Lê o texto da denúncia
				byte[] reportTextBytes = new byte[textSize];
				request.readBytes(reportTextBytes);

				// Converte para uma string
				String reportMessage = new String(reportTextBytes, StandardCharsets.ISO_8859_1);

				System.out.println("Mensagem do player: " + reportMessage);
			}
		} catch (Exception e) {
			System.err.println("Erro ao processar Report de Bad User: " + e.getMessage());
			e.getCause();
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