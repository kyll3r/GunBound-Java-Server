package br.com.gunbound.emulator.buddy.packet.readers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import br.com.gunbound.emulator.buddy.config.enums.BuddyOpcodes;
import br.com.gunbound.emulator.buddy.db.services.BuddyUserService;
import br.com.gunbound.emulator.buddy.db.services.impl.BuddyUserServiceImpl;
import br.com.gunbound.emulator.buddy.entities.BuddyPlayerSession;
import br.com.gunbound.emulator.buddy.entities.BuddySessionManager;
import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;
import br.com.gunbound.emulator.buddy.utils.PacketBuddyUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GetInfoPlayerPacketReader {

	private static final int BUDDY_ADD_GETINFO_REQUEST = BuddyOpcodes.BUDDY_ADD_GETINFO_REQUEST.getId();
	private static final BuddySessionManager buddySessionManager = BuddySessionManager.getInstance();
	
	private static final BuddyUserService userService = new BuddyUserServiceImpl();

	//apos um usuario aceitar o convite o server precisa carregar as informacoes na buddy do novo amigo
	public static void read(BuddyPlayerSession session, ByteBuf payload) {
		System.out.println("RECV> SVC_BUDDY_ADD (0x" + Integer.toHexString(BUDDY_ADD_GETINFO_REQUEST) + ")");

		if (session == null) {
			System.err.println("Erro crítico: Sessão do jogador é nula no LoginPacketReader.");
			// Não podemos fechar o canal aqui sem o contexto, o handler deve fazer isso.
			return;
		}

		// --- VALIDAÇÃO INICIAL ---
		if (payload.readableBytes() < 16) {
			System.err.println("Pacote" + Integer.toHexString(BUDDY_ADD_GETINFO_REQUEST)
					+ "com tamanho de payload incorreto. Esperado: 80, Recebido: " + payload.readableBytes());
			session.ctx().close();
			return;
		}

		try {

			byte[] userBytes = new byte[16];
			payload.readBytes(userBytes);

			//String userId = new String(userBytes, StandardCharsets.ISO_8859_1);
			String userId = Utils.extractString(userBytes);
			sendBuddieInfo(session,userId);

			System.out.println("[DEBUG] UserId >> " + userId);

		} catch (Exception e) {
			System.err.println("Falha crítica ao descriptografar ou processar o pacote 0x1010:");
			e.printStackTrace();
			session.ctx().close();
		}
	}

	private static void sendBuddieInfo(BuddyPlayerSession session, String userId) {

		ByteBuf response = Unpooled.buffer();
		response.writeShortLE(0);

		BuddyPlayerSession playerSession = buddySessionManager.getSessionPlayerById(userId);

		BuddyUserDTO queriedUser = userService.findUserByUserId(userId);// pegar dados do amigo

		if (queriedUser != null) {
			System.out.println("ENTROU NO IF queriedUser != null");	
				response.writeBytes(Utils.resizeBytes(queriedUser.getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
				response.writeBytes(Utils.resizeBytes(queriedUser.getNickname().getBytes(StandardCharsets.ISO_8859_1), 12)); // nick 12b																									
				response.writeBytes(Utils.resizeBytes(queriedUser.getGuild().getBytes(StandardCharsets.ISO_8859_1), 8)); // Guild 8b
				response.writeShortLE(queriedUser.getTotalGrade());

				

			// resp opcode 0x3FFF
			PacketBuddyUtils.sendPacket(session.ctx(), BuddyOpcodes.BUDDY_ADD_GETINFO_RESP.getId(), response);
			
			if(playerSession !=null) {
				System.out.println("playerSession !=null");	
				List<BuddyUserDTO> userList = new ArrayList<BuddyUserDTO>();
				userList.add(queriedUser);
				LoginPacketReader.sendBuddiesStatus(session, userList);
			}
			
		}
		// notifyBuddiesStatus(session, userList);

	}

}