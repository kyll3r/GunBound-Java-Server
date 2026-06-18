package br.com.gunbound.emulator.model.entities.DTO.stats;

public class MapStatsDTO {

    private int mapId;
    private int wins;
    private int losses;

    public MapStatsDTO(int mapId, int wins, int losses) {
        this.mapId = mapId;
        this.wins = wins;
        this.losses = losses;
    }

    public MapStatsDTO() {
		// TODO Auto-generated constructor stub
	}

	// Getters e Setters
    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getWin() {
        return wins;
    }

    public void setWin(int wins) {
        this.wins = wins;
    }

    public int getLose() {
        return losses;
    }

    public void setLose(int losses) {
        this.losses = losses;
    }
}