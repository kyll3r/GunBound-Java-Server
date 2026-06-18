package br.com.gunbound.emulator.buddy.packet.writers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import br.com.gunbound.emulator.buddy.config.enums.BuddyOpcodes;
import br.com.gunbound.emulator.buddy.db.services.BuddyUserService;
import br.com.gunbound.emulator.buddy.db.services.PacketService;
import br.com.gunbound.emulator.buddy.db.services.impl.BuddyUserServiceImpl;
import br.com.gunbound.emulator.buddy.db.services.impl.PacketServiceImpl;
import br.com.gunbound.emulator.buddy.entities.BuddyPlayerSession;
import br.com.gunbound.emulator.buddy.entities.BuddySessionManager;
import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;
import br.com.gunbound.emulator.buddy.entities.dto.PacketDTO;
import br.com.gunbound.emulator.buddy.utils.PacketBuddyUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;

public class BuddyMail {

	private static final PacketService packetService = new PacketServiceImpl();
	private static final BuddyUserService userService = new BuddyUserServiceImpl();

	public static void mailCheck(BuddyPlayerSession session) {

		List<PacketDTO> listPacket = packetService.getByReceiver(session.getUser().getUserId());

		System.out.println("[DEBUG] >> Entrou");

		listPacket.forEach(packet -> {

			System.out.println("[DEBUG] SubOpcode >>" + (packet.getCode() & 0xFFFF));

			//switch (packet.getCode() & 0xFFFF) {
			//case 0xC011:
			sendTunnelPacket(packet);
				//sendChatMessage(packet);
				//break;
			//case 0x2020:
				//sendTunnelPacket(packet);
				//break;
			//}

		});

	}

	private static void sendTunnelPacket(PacketDTO packet) {

		BuddyPlayerSession reciever = BuddySessionManager.getInstance().getSessionPlayerById(packet.getReceiver());

		if (reciever != null) {
			ByteBuf response = reciever.ctx().alloc().buffer();

			// 1. Busca o usuário no "banco de dados". e armazena na sessão.
			BuddyUserDTO sender = userService.findUserByUserId(packet.getSender());

			response.writeBytes(Utils.resizeBytes(sender.getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
			response.writeBytes(Utils.resizeBytes(sender.getNickname().getBytes(StandardCharsets.ISO_8859_1), 12)); // nick
			if ((packet.getCode() & 0xFFFF) == 0xC011) {
				response.writeShortLE(packet.getCode());
			}
			response.writeBytes(packet.getBody());

			// Resp 0x2021
			PacketBuddyUtils.sendPacket(reciever.ctx(), BuddyOpcodes.BUDDY_TUNNEL_PACKET_RESP.getId(), response);

			// deletar a mensagem que está no banco
			packetService.deleteBySerialNo(packet.getSerialNo());
		}
	}

	/*private static void sendTunnelPacket(PacketDTO packet) {
		System.out.println("ENTROUUUUUUUUUUUU");
		BuddyPlayerSession reciever = BuddySessionManager.getInstance().getSessionPlayerById(packet.getReceiver());

		ByteBuf response = reciever.ctx().alloc().buffer();

		// Inicia a Factory para buscar no banco de dados
		UserDAO factory = DAOFactory.CreateUserDao();
		// 2. Busca o usuário no "banco de dados". e armazena na sessão.
		UserDTO sender = factory.getUserByUserId(packet.getSender());

		response.writeBytes(Utils.resizeBytes(sender.getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
		response.writeBytes(Utils.resizeBytes(sender.getNickname().getBytes(StandardCharsets.ISO_8859_1), 12)); // nick
		response.writeBytes(packet.getBody());

		// Resp 0x2021
		PacketBuddyUtils.sendPacket(reciever.ctx(), BuddyOpcodes.BUDDY_TUNNEL_PACKET_RESP.getId(), response);

		// deletar a mensagem que está no banco
		// packetService.deleteBySerialNo(packet.getSerialNo());
	}*/

}
