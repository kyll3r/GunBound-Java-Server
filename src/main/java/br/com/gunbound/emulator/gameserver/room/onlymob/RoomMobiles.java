package br.com.gunbound.emulator.gameserver.room.onlymob;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import br.com.gunbound.emulator.gameserver.room.model.Tank;
import br.com.gunbound.emulator.gameserver.room.model.enums.TankFamily;

public class RoomMobiles {

    private final Map<Tank, MobileRoom> mobiles = new EnumMap<>(Tank.class);

    public RoomMobiles() {
        initDefaults();
    }

    private void initDefaults() {
        for (Tank tank : Tank.values()) {
            //boolean enabled = tank != Tank.RANDOM;
            mobiles.put(tank, new MobileRoom(tank, true));
        }
    }

    /* =========================
       CONSULTA
       ========================= */

    public boolean isEnabled(Tank tank) {
        return mobiles.get(tank).isEnabled();
    }

    public Map<Tank, MobileRoom> getAll() {
        //return mobiles;
        // retorna cópia imutável para evitar que chamador modifique diretamente
        return Collections.unmodifiableMap(mobiles);
    }

    /* =========================
       CONTROLE SIMPLES
       ========================= */

    public void setEnabled(Tank tank, boolean enabled) {
        mobiles.get(tank).setEnabled(enabled);
    }

    public void disableAll() {
        mobiles.values().stream()
               .filter(m -> m.getTank() != Tank.RANDOM) // nunca no RANDOM
               .forEach(m -> m.setEnabled(false));
    }
    
    public void enableAll() {
        mobiles.values().stream()
               .forEach(m -> m.setEnabled(true));
    }

    /* =========================
       CONTROLE POR FAMÍLIA
       ========================= */

    public void setFamilyEnabled(TankFamily family, boolean enabled) {
        for (MobileRoom mobile : mobiles.values()) {
            if (mobile.getTank().getFamily() == family) {
                mobile.setEnabled(enabled);
            }
        }
    }
    
    /* =========================
    LISTAGEM DE TODOS MOBILES ALLOWED
    ========================= */
    public List<Tank> getAllowedMobiles() {
        return this.getAll().values().stream()  // Acessa todos os MobileRoom
                .filter(MobileRoom::isEnabled)       // Filtra os que estão habilitados
                .filter(m -> m.getTank() != Tank.RANDOM) // nunca no RANDOM
                .map(MobileRoom::getTank)            // Mapeia para o Tank correspondente
                .toList();                          // Retorna a lista final
    }
    
    
    /**
     * Verifica se a funcionalidade "OnlyMob" está ativada na sala.
     * 
     * Este método verifica se existe algum "Mobile" na sala que não esteja ativado. Caso haja pelo menos 
     * sim, o método retorna `true`, indicando que a funcionalidade "OnlyMob" está ativa. Caso 
     * contrário, retorna `false`, indicando que todos os "Mobiles" estão habilitados.
     * 
     * @return `true` se existir ao menos um "Mobile" desativado, indicando que a funcionalidade "OnlyMob" está 
     *         ativada; `false` caso contrário.
     */
	public boolean isOnlyMobEnabled() {
		return this.getAll().values().stream().anyMatch(m -> !m.isEnabled() && m.getTank() != Tank.RANDOM);
	}
    
}
