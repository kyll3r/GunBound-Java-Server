package br.com.gunbound.emulator.buddy.packet.readers;

import java.util.stream.IntStream;

import br.com.gunbound.emulator.buddy.config.enums.BuddyOpcodes;
import br.com.gunbound.emulator.buddy.db.exceptions.DbInsertDuplicate;
import br.com.gunbound.emulator.buddy.db.services.PacketService;
import br.com.gunbound.emulator.buddy.db.services.impl.PacketServiceImpl;
import br.com.gunbound.emulator.buddy.entities.BuddyPlayerSession;
import br.com.gunbound.emulator.buddy.entities.dto.PacketDTO;
import br.com.gunbound.emulator.buddy.utils.BuddyCipher;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SavePacketReader {

	private static final int BUDDY_REQUEST = BuddyOpcodes.BUDDY_SAVE_PACKET.getId();
	private static final PacketService packetService = new PacketServiceImpl();

	public static void read(BuddyPlayerSession session, ByteBuf payload) {
		System.out.println("RECV> SVC_SAVE_PACKET (0x" + Integer.toHexString(BUDDY_REQUEST) + ")");

		if (session == null) {
			System.err.println("Erro crítico: Sessão do jogador é nula no SavePacketReader.");
			// Não podemos fechar o canal aqui sem o contexto, o handler deve fazer isso.
			return;
		}

		// --- VALIDAÇÃO INICIAL ---
		if (payload.readableBytes() < 16) {
			System.err.println("Pacote" + Integer.toHexString(BUDDY_REQUEST)
					+ "com tamanho de payload incorreto. Esperado: 16, Recebido: " + payload.readableBytes());
			session.ctx().close();
			return;
		}

		// instancia fora para puder usar no finnaly
		PacketDTO p = new PacketDTO();
		try {

			byte[] payloadToDecrypt = new byte[payload.readableBytes()];
			payload.readBytes(payloadToDecrypt);
			// Decryptar o pacote
			byte[] payload_decrypted = BuddyCipher.gunboundDynamicDecrypt(payloadToDecrypt,
					session.getUser().getUserId(), session.getUser().getPassword(), session.getAuthToken(), 0x0);

			// O ByteBuffer é conveniente aqui.
			ByteBuf buffer = Unpooled.wrappedBuffer(payload_decrypted);

			byte[] userBytes = new byte[16];
			buffer.readBytes(userBytes);

			// String userId = new String(userBytes, StandardCharsets.ISO_8859_1);
			String receiverUserId = Utils.extractString(userBytes);

			// byte[] opcodeBytes = new byte[2];
			// buffer.readBytes(opcodeBytes);
			short opcode = buffer.readShortLE();

			byte[] msgBytes = new byte[buffer.readableBytes()];

			buffer.readBytes(msgBytes);

			System.out.println("[DEBUG] UserId >> " + receiverUserId);
			System.out.println("[DEBUG] Opcode >> " + Integer.toHexString(opcode));
			System.out.println("[DEBUG] MSG >> " + Utils.bytesToHex(msgBytes));

			if (!isArrayZeroed(msgBytes)) {//verifica se mensagem ta com os bytes zerados
				
				p.setReceiver(receiverUserId);
				p.setSender(session.getUser().getUserId());
				p.setCode(opcode & 0xFFFF);
				p.setBody(msgBytes);

				// Salvar registro
				packetService.save(p);
			} else {
				System.err.println("Ignorando mensagem com bytes zerados...");
			}

		} catch (DbInsertDuplicate e) {
			System.err.println(e.getMessage());

		} catch (Exception e) {
			System.err.println("Falha crítica ao descriptografar ou processar o pacote 0x"
					+ Integer.toHexString(BUDDY_REQUEST) + ":");
			// e.printStackTrace();
			session.ctx().close();
		}
	}

	/**
	 * Verifica se todos os elementos de um array de bytes são iguais a zero.
	 *
	 * <p>
	 * Este método percorre o array de bytes fornecido e retorna {@code true} se
	 * todos os seus elementos forem iguais a {@code 0}. Caso contrário, retorna
	 * {@code false}. Se o array for {@code null}, o método também retorna
	 * {@code false} por padrão.
	 * </p>
	 *
	 * @param array o array de bytes a ser verificado
	 * @return {@code true} se todos os bytes forem zero e o array não for
	 *         {@code null}; {@code false} caso contrário
	 */
	private static boolean isArrayZeroed(byte[] array) {
		return array != null && IntStream.range(0, array.length).allMatch(i -> array[i] == 0);
	}
}