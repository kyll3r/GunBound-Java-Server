package br.com.gunbound.emulator.buddy.packet.writers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import br.com.gunbound.emulator.buddy.config.enums.BuddyOpcodes;
import br.com.gunbound.emulator.buddy.entities.BuddyList;
import br.com.gunbound.emulator.buddy.entities.BuddyPlayerSession;
import br.com.gunbound.emulator.buddy.entities.BuddySessionManager;
import br.com.gunbound.emulator.buddy.utils.PacketBuddyUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;

public class PlayerNotifiers {
	private static final BuddySessionManager buddySessionManager = BuddySessionManager.getInstance();

	public static void playerBecameOffline(BuddyPlayerSession session) {

		List<BuddyList> userList = session.getFriendList();

		userList.forEach(player -> {

			BuddyPlayerSession playerSession = buddySessionManager.getSessionPlayerById(player.getBuddyId());

			if (playerSession != null) {
				ByteBuf response = playerSession.ctx().alloc().buffer();

				response.writeShortLE(1);
				response.writeBytes(Utils.resizeBytes(player.getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
				response.writeByte(0);

				// resp opcode 0x3FFF
				PacketBuddyUtils.sendPacket(playerSession.ctx(), BuddyOpcodes.BUDDY_STATUS_SYNC.getId(), response);
			}
		});

	}

}
