package br.com.gunbound.emulator.gameserver.room.model;

import java.util.HashMap;
import java.util.Map;

import br.com.gunbound.emulator.gameserver.room.model.enums.TankFamily;

/**
 * Enumeração para os Mobiles (Tanks) do GunBound.
 */
public enum Tank {
	ARMOR(0, "Armor", TankFamily.MECHANIC), 
	MAGE(1, "Mage", TankFamily.SHIELD),
	NAK(2, "Nak", TankFamily.MECHANIC),  
	TRICO(3, "Trico", TankFamily.BIONIC), 
	BIGFOOT(4, "Bigfoot", TankFamily.MECHANIC), 
	BOOMER(5, "Boomer", TankFamily.BIONIC), 
	RAON(6, "Raon", TankFamily.MECHANIC), 
	LIGHTNING(7, "Lightning", TankFamily.SHIELD), 
	JD(8, "J.D.", TankFamily.SHIELD), 
	ASATE(9, "A.Sate", TankFamily.SHIELD), 
	ICE(10, "Ice", TankFamily.BIONIC), 
	TURTLE(11, "Turtle", TankFamily.BIONIC), 
	GRUB(12, "Grub", TankFamily.BIONIC), 
	ADUKA(13, "Aduka", TankFamily.MECHANIC), 
	DRAGON(17, "Dragon", TankFamily.BIONIC), 
	KNIGHT(18, "Knight", TankFamily.MECHANIC),  
	RANDOM(255, "Random", TankFamily.RIDER);

	private final int id;
	private final String name;
	private TankFamily family;


    private static final Map<String, Tank> BY_NAME = new HashMap<>();

    static {
        for (Tank tank : values()) {
            BY_NAME.put(normalize(tank.name), tank);
        }
    }
    
	private static final Map<Integer, Tank> BY_ID = new HashMap<>();

	static {
		for (Tank t : values()) {
			BY_ID.put(t.id, t);
		}
	}

	Tank(int id, String name, TankFamily family) {
		this.id = id;
		this.name = name;
		this.family = family;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static Tank fromId(int id) {
		return BY_ID.getOrDefault(id, RANDOM);
	}
	
	//USADO no comando onlymob
	
    public TankFamily getFamily() {
        return family;
    }
	
    public static Tank fromName(String name) {
        return BY_NAME.get(normalize(name));
    }

    private static String normalize(String s) {
        return s.toLowerCase().replace(".", "").trim();
    }
	
	
}