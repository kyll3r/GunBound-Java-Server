package br.com.gunbound.emulator.buddy.packet.readers;

import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.buddy.config.enums.BuddyOpcodes;
import br.com.gunbound.emulator.buddy.db.services.BuddyUserService;
import br.com.gunbound.emulator.buddy.db.services.impl.BuddyUserServiceImpl;
import br.com.gunbound.emulator.buddy.entities.BuddyPlayerSession;
import br.com.gunbound.emulator.buddy.entities.BuddySessionManager;
import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;
import br.com.gunbound.emulator.buddy.utils.PacketBuddyUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;

public class UpdateUserStatePacketReader {

	private static final int SVC_USER_STATE = BuddyOpcodes.SVC_USER_STATE.getId();
	private static final BuddySessionManager buddySessionManager = BuddySessionManager.getInstance();
	
	private static final BuddyUserService userService = new BuddyUserServiceImpl();

	//Apos usuario ter aceito um convite o estado dele precisa ser atualizado
	public static void read(BuddyPlayerSession session, ByteBuf payload) {
		System.out.println("RECV> SVC_BUDDY_ADD (0x" + Integer.toHexString(SVC_USER_STATE) + ")");

		if (session == null) {
			System.err.println("Erro crítico: Sessão do jogador é nula no LoginPacketReader.");
			// Não podemos fechar o canal aqui sem o contexto, o handler deve fazer isso.
			return;
		}

		// --- VALIDAÇÃO INICIAL ---
		if (payload.readableBytes() < 16) {
			System.err.println("Pacote" + Integer.toHexString(SVC_USER_STATE)
					+ "com tamanho de payload incorreto. Esperado: 16, Recebido: " + payload.readableBytes());
			session.ctx().close();
			return;
		}

		try {

			byte[] userBytes = new byte[16];
			payload.readBytes(userBytes);

			// String userId = new String(userBytes, StandardCharsets.ISO_8859_1);
			String userId = Utils.extractString(userBytes);
			sendBuddiesStatus(session, userId);

			System.out.println("[DEBUG] UserId >> " + userId);

		} catch (Exception e) {
			System.err.println("Falha crítica ao descriptografar ou processar o pacote (0x" + Integer.toHexString(SVC_USER_STATE) + ")");
			e.printStackTrace();
			session.ctx().close();
		}
	}

	private static void sendBuddiesStatus(BuddyPlayerSession session, String userId) {

		ByteBuf response = session.ctx().channel().alloc().buffer();
		response.writeShortLE(1);

		BuddyPlayerSession playerSession = buddySessionManager.getSessionPlayerById(userId);

		BuddyUserDTO queriedUser = userService.findUserByUserId(userId);// pegar dados do amigo

		if (queriedUser != null) {
			System.out.println("ENTROU NO IF queriedUser != null");
			response.writeBytes(Utils.resizeBytes(queriedUser.getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
			if (playerSession == null) {//se player esta offline
				response.writeByte(0);
			} else {//se player esta online
				response.writeByte(1);
				response.writeBytes(playerSession.getIpBytes());
				response.writeBytes(playerSession.getUdpPortBytes());
				response.writeBytes(playerSession.getIpBytes());
				response.writeBytes(playerSession.getUdpPortBytes());
			}

			// resp opcode 0x3FFF
			PacketBuddyUtils.sendPacket(session.ctx(), BuddyOpcodes.BUDDY_STATUS_SYNC.getId(), response);
		}

	}
}