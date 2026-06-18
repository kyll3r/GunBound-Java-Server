package br.com.gunbound.emulator.gameserver.packets.writers;

import java.util.Map;

import br.com.gunbound.emulator.gameserver.room.GameRoom;
import br.com.gunbound.emulator.gameserver.room.model.Tank;
import br.com.gunbound.emulator.gameserver.room.onlymob.MobileRoom;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MobilePacketWriter {

    /**
     * Cria o pacote com todos os mobiles da sala.
     * Estrutura do pacote:
     * [1 byte] = quantidade de mobiles
     * Repetição para cada mobile:
     *   [1 byte] = ID do tank
     *   [1 byte] = ativo (1) / inativo (0)
     *
     * @param room - a sala
     * @return buf - buffer para enviar o pacote
     */
    public static ByteBuf writeMobUpdate(GameRoom room) {
    	
    	ByteBuf buf = Unpooled.buffer();
    	
        Map<Tank, MobileRoom> mobiles = room.getRoomMobiles().getAll();

        // contar tanks válidos (ignorando RANDOM)
        long count = mobiles.values().stream()
                .filter(m -> m.getTank() != Tank.RANDOM)
                .count();

        if (count > 255) count = 255; // garante caber em 1 byte

        buf.writeByte((int) count); // primeiro byte = quantidade de mobiles

        // escreve cada mobile
        for (Tank tank : Tank.values()) {
            if (tank == Tank.RANDOM) continue;

            MobileRoom mobile = mobiles.get(tank);
            buf.writeByte(tank.getId());
            buf.writeByte(mobile.isEnabled() ? 1 : 0);
        }
        return buf;
    }  	
}
