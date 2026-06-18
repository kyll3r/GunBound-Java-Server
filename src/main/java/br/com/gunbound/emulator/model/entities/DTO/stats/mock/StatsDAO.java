package br.com.gunbound.emulator.model.entities.DTO.stats.mock;

import br.com.gunbound.emulator.model.entities.DTO.stats.PlayerStatsDTO;

public interface StatsDAO {
    PlayerStatsDTO findStatsByUserId(String userId);
}