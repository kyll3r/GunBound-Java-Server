package br.com.gunbound.emulator.model.entities.DTO.stats;

import java.util.List;

public class PlayerStatsDTO {

    private List<MapStatsDTO> mapStats;
    private List<MobileStatsDTO> mobileStats;

    public PlayerStatsDTO(List<MapStatsDTO> mapStats, List<MobileStatsDTO> mobileStats) {
        this.mapStats = mapStats;
        this.mobileStats = mobileStats;
    }

    public PlayerStatsDTO() {
		// TODO Auto-generated constructor stub
	}

	// Getters e Setters
    public List<MapStatsDTO> getMapStats() {
        return mapStats;
    }

    public void setMapStats(List<MapStatsDTO> mapStats) {
        this.mapStats = mapStats;
    }

    public List<MobileStatsDTO> getMobileStats() {
        return mobileStats;
    }

    public void setMobileStats(List<MobileStatsDTO> mobileStats) {
        this.mobileStats = mobileStats;
    }
}