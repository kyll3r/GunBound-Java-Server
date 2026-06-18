package br.com.gunbound.emulator.gameserver.packets.writers;

import br.com.gunbound.emulator.model.entities.DTO.stats.MapStatsDTO;
import br.com.gunbound.emulator.model.entities.DTO.stats.MobileStatsDTO;
import br.com.gunbound.emulator.model.entities.DTO.stats.PlayerStatsDTO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PlayerStatsWriter {

	public static final int OPCODE_RESPONSE = 0x1013;

	public static ByteBuf build(PlayerStatsDTO stats) {

		ByteBuf buffer = Unpooled.buffer();

		// Escreve os stats dos mapas (stages)
		buffer.writeShortLE(stats.getMapStats().size());
		for (MapStatsDTO mapStat : stats.getMapStats()) {
			buffer.writeIntLE(mapStat.getMapId());
			buffer.writeIntLE(mapStat.getWin());
			buffer.writeIntLE(mapStat.getLose());
			
			//.wrappedBuffer(new byte[] { (byte) 0x30, (byte) 0x00 }));
		}

		// Escreve os stats dos mobiles
		buffer.writeShortLE(stats.getMobileStats().size());
		for (MobileStatsDTO mobileStat : stats.getMobileStats()) {
			buffer.writeIntLE(mobileStat.getMobileId());
			buffer.writeIntLE(mobileStat.getWin());
			buffer.writeIntLE(mobileStat.getLose());
		}
		
		//buffer.writeBytes(Utils.hexStringToByteArray("0A000A0000001B00000029000000010000000000000005000000050000000100000001000000020000001B00000029000000030000001B00000029000000040000001B00000029000000060000001B00000029000000070000001B00000029000000080000001B00000029000000090000001B0000002900000007000D00000003000000040000000400000017000000290000000100000000000000010000000300000001000000010000000C0000000100000000000000020000001700000029000000050000001700000029000000"));

		return buffer;
	}
}