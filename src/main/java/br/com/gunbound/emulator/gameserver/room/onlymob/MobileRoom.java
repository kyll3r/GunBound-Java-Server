package br.com.gunbound.emulator.gameserver.room.onlymob;

import br.com.gunbound.emulator.gameserver.room.model.Tank;

public class MobileRoom {

    private final Tank tank;
    private boolean enabled;

    public MobileRoom(Tank tank, boolean enabled) {
        this.tank = tank;
        this.enabled = enabled;
    }

    public Tank getTank() {
        return tank;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MobileRoom [tank=");
		builder.append(tank);
		builder.append(", enabled=");
		builder.append(enabled);
		builder.append("]");
		return builder.toString();
	}
}
