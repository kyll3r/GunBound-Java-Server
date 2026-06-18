package br.com.gunbound.emulator.buddy.packet.readers;

import br.com.gunbound.emulator.buddy.config.enums.BuddyOpcodes;
import br.com.gunbound.emulator.buddy.db.exceptions.DbInsertDuplicate;
import br.com.gunbound.emulator.buddy.db.services.BuddyListService;
import br.com.gunbound.emulator.buddy.db.services.impl.BuddyListServiceImpl;
import br.com.gunbound.emulator.buddy.entities.BuddyPlayerSession;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;

public class DeleteUserPacketReader {

	private static final int BUDDY_REMOVE = BuddyOpcodes.BUDDY_REMOVE_REQUEST.getId();
	private static final BuddyListService buddyListService = new BuddyListServiceImpl();

	// Apos usuario ter aceito um convite o estado dele precisa ser atualizado
	public static void read(BuddyPlayerSession session, ByteBuf payload) {
		System.out.println("RECV> SVC_BUDDY_REMOVE (0x" + Integer.toHexString(BUDDY_REMOVE) + ")");

		if (session == null) {
			System.err.println("Erro crítico: Sessão do jogador é nula.");
			// Não podemos fechar o canal aqui sem o contexto, o handler deve fazer isso.
			return;
		}

		// --- VALIDAÇÃO INICIAL ---
		if (payload.readableBytes() < 16) {
			System.err.println("Pacote" + Integer.toHexString(BUDDY_REMOVE)
					+ "com tamanho de payload incorreto. Esperado: 16, Recebido: " + payload.readableBytes());
			session.ctx().close();
			return;
		}

		try {

			byte[] userBytes = new byte[16];
			payload.readBytes(userBytes);
			String userId = Utils.extractString(userBytes);
			deleteBuddie(session, userId);

			System.out.println("[DEBUG] UserId >> " + userId);

		} catch (Exception e) {
			System.err.println("Falha crítica ao descriptografar ou processar o pacote (0x"
					+ Integer.toHexString(BUDDY_REMOVE) + ")");
			e.printStackTrace();
			session.ctx().close();
		}
	}

	private static void deleteBuddie(BuddyPlayerSession session, String userId) {
		try {
			buddyListService.deleteBuddy(session.getUser().getUserId(), userId);
			buddyListService.deleteBuddy(userId, session.getUser().getUserId());

		} catch (DbInsertDuplicate e) {
			System.err.println(e.getMessage());

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}
}