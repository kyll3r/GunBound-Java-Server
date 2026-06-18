package br.com.gunbound.emulator.buddy.packet.readers;

import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.buddy.config.enums.BuddyOpcodes;
import br.com.gunbound.emulator.buddy.db.exceptions.DbInsertDuplicate;
import br.com.gunbound.emulator.buddy.db.services.BuddyListService;
import br.com.gunbound.emulator.buddy.db.services.PacketService;
import br.com.gunbound.emulator.buddy.db.services.impl.BuddyListServiceImpl;
import br.com.gunbound.emulator.buddy.db.services.impl.PacketServiceImpl;
import br.com.gunbound.emulator.buddy.entities.BuddyPlayerSession;
import br.com.gunbound.emulator.buddy.entities.BuddySessionManager;
import br.com.gunbound.emulator.buddy.entities.dto.PacketDTO;
import br.com.gunbound.emulator.buddy.utils.PacketBuddyUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;

/**
 * Handler do packet tunnel (opcode 0x2020) para múltiplos subprotocolos.
 */
public class TunnelPacketReader {

	private static final PacketService packetService = new PacketServiceImpl();
	private static final BuddyListService buddyListService = new BuddyListServiceImpl();

	public static void read(BuddyPlayerSession session, ByteBuf buf) {
		if (buf.readableBytes() < 7) {
			System.err.println("TunnelPacket: pacote muito curto.");
			return;
		}

		buf.markReaderIndex(); // Permite fazer replay do payload para relay

		// Lê campos padrão do "envelope" tunnel
		short tunnelType = buf.readShortLE(); // Ex: 0x1101
		byte tunnelFlag = buf.readByte(); // Ex: 0x01
		short msgLen = buf.readShortLE(); // Pode ser tamanho da msg ou campo variado

		System.out.format("TunnelPacket: type=0x%04X flag=0x%02X msgLen=0x%04X\n", tunnelType, tunnelFlag, msgLen);

		// Decide pelo tipo
		if (tunnelType == 0x1101) {
			// Chat relay
			buf.resetReaderIndex();
			sendChatMessage(session, buf);
		} else if (tunnelType == 0x4101) {
			// enviar o convite para adicionar um novo amigo
			buf.resetReaderIndex();
			sendReqOrRplToPlayer(session, buf, true);
		} else if (tunnelType == 0x4201) {
			// envia a resposta do convite para adicionar um novo amigo
			buf.resetReaderIndex();
			sendReqOrRplToPlayer(session, buf, false);
		} else if (tunnelType == 0x5100) {
			// Exemplo de "status" ou "entrada em sala/lobby", precisa analisar mais
			buf.resetReaderIndex();
			updatePlayerStatus(session, buf);
		} else {
			// Formato futuro/desconhecido: relay bruto (encaminha para outros se
			System.out.println("TunnelPacket: tipo não tratado ainda, dumpando bytes restantes:");
			buf.resetReaderIndex();
			byte[] all = new byte[buf.readableBytes()];
			buf.readBytes(all);
			System.out.println(Utils.bytesToHex(all));
			//Interessante fechar a sessao para nao usar softwares externos
			//session.ctx().close();
		}
	}

	/**
	 * Enviar mensagem de um player para outro
	 */
	private static void sendChatMessage(BuddyPlayerSession sender, ByteBuf buf) {
		// buf.readShortLE(); // opcode
		buf.skipBytes(1); // 0x01 
		short flag = buf.readShortLE();
		short msgLen = buf.readShortLE(); // Ex: 0x0002

		System.out.println("MSG LEN >> " + msgLen);
		byte[] msgBytes = new byte[msgLen];
		buf.readBytes(msgBytes);

		String message = new String(msgBytes, StandardCharsets.ISO_8859_1);

		// String senderNick = readFixedString(buf, 16);

		byte[] userIdBytes = new byte[buf.readableBytes()];
		buf.readBytes(userIdBytes);

		String user = new String(userIdBytes, StandardCharsets.ISO_8859_1).trim();

		BuddyPlayerSession reciever = BuddySessionManager.getInstance().getSessionPlayerById(user);

		if (reciever != null) {
			ByteBuf response = reciever.ctx().alloc().buffer();

			response.writeBytes(
					Utils.resizeBytes(sender.getUser().getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
			response.writeBytes(
					Utils.resizeBytes(sender.getUser().getNickname().getBytes(StandardCharsets.ISO_8859_1), 12)); // nick
																													// 13b
			response.writeShortLE(flag);
			response.writeShortLE(msgLen);
			response.writeBytes(message.getBytes(StandardCharsets.ISO_8859_1));

			// Resp 0x2021
			PacketBuddyUtils.sendPacket(reciever.ctx(), BuddyOpcodes.BUDDY_TUNNEL_PACKET_RESP.getId(), response);
		}
	}

	/**
	 * Envia Uma requisição ou resposta para um determinado player
	 * @param sender quem esta enviando ou respondendo a requisicao
	 * @param buf o payload da mensagem
	 * @param invite Se o invite for {@code true} é um requisição para adicionar um novo amigo.
	 * Se o invite for {@code false} é a resposta de uma requisição
	 */
	private static void sendReqOrRplToPlayer(BuddyPlayerSession sender, ByteBuf buf, boolean invite) {
		int sizePacket = 5; // padrao para resposta
		if (invite) {//se é convite
			sizePacket = 13;
		}

		byte tunnelType = buf.readByte(); // Flag
		byte[] infoToSend = new byte[sizePacket];
		buf.readBytes(infoToSend);

		buf.readShort();

		byte[] userIdBytes = new byte[buf.readableBytes()];
		buf.readBytes(userIdBytes);

		// String user = new String(userIdBytes, StandardCharsets.ISO_8859_1).trim();
		String user = Utils.extractString(userIdBytes);

		System.out.println("[DEBUG] USER (0x4101) >> " + user);

		BuddyPlayerSession reciever = BuddySessionManager.getInstance().getSessionPlayerById(user);

		try {
			if (reciever != null) {
				System.out.println("[DEBUG] ENTROOU >> reciever != null");
				ByteBuf response = reciever.ctx().alloc().buffer();

				response.writeBytes(
						Utils.resizeBytes(sender.getUser().getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
				response.writeBytes(
						Utils.resizeBytes(sender.getUser().getNickname().getBytes(StandardCharsets.ISO_8859_1), 12)); // nick
																														// 12b
				response.writeBytes(infoToSend);

				// Resp 0x2021
				PacketBuddyUtils.sendPacket(reciever.ctx(), BuddyOpcodes.BUDDY_TUNNEL_PACKET_RESP.getId(), response);
				
				checkReply(sender, invite, infoToSend, user);
				
				
			} else {// player offline salva a mensagem

				PacketDTO p = new PacketDTO();
				p.setReceiver(user);
				p.setSender(sender.getUser().getUserId());
				p.setCode(BuddyOpcodes.BUDDY_TUNNEL_PACKET.getId() & 0xFFFF);
				p.setBody(infoToSend);

				// Salvar registro
				packetService.save(p);

				checkReply(sender, invite, infoToSend, user);
			}

		} catch (DbInsertDuplicate e) {
			System.err.println(e.getMessage());

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private static void checkReply(BuddyPlayerSession sender, boolean invite, byte[] infoToSend, String user) {
		if (!invite) {//se for resposta do convite
			int answerCheck = infoToSend[4] & 0xFF;

			System.out.println("[DEBUG] >> Aceitou? " + answerCheck);
			if (answerCheck == 1) {
				System.out.println("[DEBUG] >> inviteCheck == 1 ");
				buddyListService.addBuddy(sender.getUser().getUserId(), user, "");
				buddyListService.addBuddy(user, sender.getUser().getUserId(), "");
			}
		}
	}

	/**
	 * Envia atualização de estado do jogador
	 */
	private static void updatePlayerStatus(BuddyPlayerSession sender, ByteBuf buf) {
		byte tunnelType = buf.readByte(); // 0x00
		byte[] infoToSend = new byte[28];
		buf.readBytes(infoToSend);

		buf.readShort();

		byte[] userIdBytes = new byte[buf.readableBytes()];
		buf.readBytes(userIdBytes);

		String user = new String(userIdBytes, StandardCharsets.ISO_8859_1).trim();

		BuddyPlayerSession reciever = BuddySessionManager.getInstance().getSessionPlayerById(user);

		if (reciever != null) {
			ByteBuf response = reciever.ctx().alloc().buffer();

			response.writeBytes(
					Utils.resizeBytes(sender.getUser().getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
			response.writeBytes(
					Utils.resizeBytes(sender.getUser().getNickname().getBytes(StandardCharsets.ISO_8859_1), 12)); // nick
																													// 13b
			response.writeBytes(infoToSend);

			// Resp 0x2021
			PacketBuddyUtils.sendPacket(reciever.ctx(), BuddyOpcodes.BUDDY_TUNNEL_PACKET_RESP.getId(), response);
		}
	}
}
