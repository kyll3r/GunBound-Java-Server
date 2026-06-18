package br.com.gunbound.emulator.gameserver.room.onlymob;

import br.com.gunbound.emulator.gameserver.room.GameRoom;
import br.com.gunbound.emulator.gameserver.room.model.Tank;
import br.com.gunbound.emulator.gameserver.room.model.enums.TankFamily;

public class MobCommandHandler {

    public static void handleOnly(GameRoom room, String rawCommand) {

        //Lista de args aqui
        String args = rawCommand;

        if (args.isEmpty()) {
            throw new IllegalArgumentException("Uso: /only <tank|family>[,<tank>...]");
        }

        RoomMobiles mobiles = room.getRoomMobiles();

        // desativa tudo
        mobiles.disableAll();

        // separa argumentos
        String[] tokens = args.split(",");

        boolean somethingEnabled = false;

        for (String token : tokens) {
            String value = token.trim();

            // tenta resolver como Tank
            Tank tank = Tank.fromName(value);
            if (tank != null) {
                mobiles.setEnabled(tank,true);
                somethingEnabled = true;
                continue;
            }

            // tenta resolver como família
            TankFamily family = TankFamily.fromAlias(value);
            if (family != null) {
                mobiles.setFamilyEnabled(family,true);
                somethingEnabled = true;
                continue;
            }
            
            //voltar os mobiles ativos
            if(value.equals("all")) {
            	mobiles.enableAll();
                somethingEnabled = true;
                continue;
            }

            // token inválido
            System.out.println("Token ignorado: " + value);
        }
        
        room.getRoomMobiles().getAll().forEach((tank, mobile) -> {
            System.out.printf("Tank=%s id=%d enabled=%s%n",
                tank.name(), tank.getId(), mobile.isEnabled());
        });

        if (!somethingEnabled) {
            throw new IllegalArgumentException("Nenhum mobile ou família válida informada");
        }
    }
}
