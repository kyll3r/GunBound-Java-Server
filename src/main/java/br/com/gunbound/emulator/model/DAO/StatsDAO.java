package br.com.gunbound.emulator.model.DAO;

import br.com.gunbound.emulator.model.entities.DTO.stats.PlayerStatsDTO;

public interface StatsDAO {

    /**
     * Busca todas as estatísticas de mobiles e mapas para um determinado jogador.
     * @param userId O ID do usuário.
     * @return Um objeto PlayerStatsDTO preenchido com as listas de estatísticas.
     */
    PlayerStatsDTO getPlayerStatsByUserId(String userId);
    
    
    /**
     * Atualiza (ou insere) o registro de estatística para um mobile específico.
     */
    void updateMobileRecord(String userId, int mobileId, boolean didWin);
    
    /**
     * Atualiza (ou insere) o registro de estatística para um mapa específico.
     */
    void updateStageRecord(String userId, int mapId, boolean didWin); 
}