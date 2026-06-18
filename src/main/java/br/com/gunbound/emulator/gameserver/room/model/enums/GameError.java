package br.com.gunbound.emulator.gameserver.room.model.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeração para os Modos de Jogo do GunBound.
 */
public enum GameError {
	WRONG_VERSION(0x0060),
    LOGIN_PROIBIDO(0x0030),
    SHOP_ERROR(0x000004),//Erro usado quando item nao ta na menu/item/chest
    USER_NOT_FOUND(0x0000), // Valor padrão para modos não reconhecidos
    UNKNOWN(0x00); // Valor padrão para modos não reconhecidos

    private final int id;

    // Mapa para busca rápida do modo de jogo pelo ID
    private static final Map<Integer, GameError> BY_ID = new HashMap<>();

    // Bloco estático para preencher o mapa quando a classe for carregada
    static {
        for (GameError mode : values()) {
        	BY_ID.put(mode.id, mode);
        }
    }

    GameError(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Obtém um GameError a partir de seu ID numérico.
     *
     * @param id O ID do modo de jogo.
     * @return O GameMode correspondente, ou UNKNOWN se o ID não for encontrado.
     */
    public static GameError fromId(int id) {
        return BY_ID.getOrDefault(id, UNKNOWN);
    }
}