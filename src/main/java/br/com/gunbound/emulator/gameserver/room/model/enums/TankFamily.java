package br.com.gunbound.emulator.gameserver.room.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum TankFamily {
	BIONIC("bionic"), SHIELD("shield"), MECHANIC("mechanic"), RIDER("rider");

	private final String alias;

	private static final Map<String, TankFamily> ALIASES = new HashMap<>();

	// Preenche o mapa de apelidos
	static {
		for (TankFamily family : values()) {
			ALIASES.put(family.alias, family);
			ALIASES.put("bio", BIONIC);
			ALIASES.put("shld", SHIELD);
			ALIASES.put("sld", SHIELD);
			ALIASES.put("mec", MECHANIC);
			ALIASES.put("mc", MECHANIC);
		}
	}

	TankFamily(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	// Método para buscar a família pelo apelido
	public static TankFamily fromAlias(String alias) {
		return ALIASES.get(alias.toLowerCase());
	}
}