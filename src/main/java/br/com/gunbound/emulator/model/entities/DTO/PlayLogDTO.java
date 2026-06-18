package br.com.gunbound.emulator.model.entities.DTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object que representa um registro completo de uma partida para
 * a tabela 'playlog'.
 */
public class PlayLogDTO {

	private int serverIp; // Mapeado para ServerIP
	private int gameRoomId;
	private String gameRoomTitle;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private int gameOption;
	private int winTeamOrPlayer;
	private List<PlayerLog> players = new ArrayList<>();

	// Getters e Setters para todos os campos...

	public int getServerIp() {
		return serverIp;
	}

	public void setServerIp(int serverIp) {
		this.serverIp = serverIp;
	}

	public int getGameRoomId() {
		return gameRoomId;
	}

	public void setGameRoomId(int gameRoomId) {
		this.gameRoomId = gameRoomId;
	}

	public String getGameRoomTitle() {
		return gameRoomTitle;
	}

	public void setGameRoomTitle(String gameRoomTitle) {
		this.gameRoomTitle = gameRoomTitle;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public int getGameOption() {
		return gameOption;
	}

	public void setGameOption(int gameOption) {
		this.gameOption = gameOption;
	}

	public int getWinTeamOrPlayer() {
		return winTeamOrPlayer;
	}

	public void setWinTeamOrPlayer(int winTeamOrPlayer) {
		this.winTeamOrPlayer = winTeamOrPlayer;
	}

	public List<PlayerLog> getPlayers() {
		return players;
	}

	public void setPlayers(List<PlayerLog> players) {
		this.players = players;
	}

	/**
	 * Sub-classe para representar os dados de um único jogador no log.
	 */
	public static class PlayerLog {
		private int slot;
		private String userId;
		private int teamId;
		private LocalDateTime deadTime; // Pode ser null se o jogador sobreviveu
		private int deadCause; // 0 se sobreviveu
		private int scoreDelta; // GP
		private int moneyDelta; // Gold
		
	    private int mapId=0xFF;
	    private int tankPrimary=0xFF;
	    private int tankSecondary=0xFF;
	    
	    private boolean win; // true para vitória, false para derrota/empate

		public PlayerLog(int slot, String userId, int teamId, int scoreDelta, int moneyDelta) {
			this.slot = slot;
			this.userId = userId;
			this.teamId = teamId;
			this.scoreDelta = scoreDelta;
			this.moneyDelta = moneyDelta;
			// deadTime e deadCause podem ser definidos posteriormente
		}

		// Getters e Setters...
		public int getSlot() {
			return slot;
		}

		public String getUserId() {
			return userId;
		}

		public int getTeamId() {
			return teamId;
		}

		public LocalDateTime getDeadTime() {
			return deadTime;
		}

		public void setDeadTime(LocalDateTime deadTime) {
			this.deadTime = deadTime;
		}

		public int getDeadCause() {
			return deadCause;
		}

		public void setDeadCause(int deadCause) {
			this.deadCause = deadCause;
		}

		public int getScoreDelta() {
			return scoreDelta;
		}

		public int getMoneyDelta() {
			return moneyDelta;
		}
		
	    public int getMapId() {
	        return mapId;
	    }

	    public void setMapId(int mapId) {
	        this.mapId = mapId;
	    }

	    public boolean isWin() {
	        return win;
	    }

	    public void setWin(boolean win) {
	        this.win = win;
	    }  
	    
		public int getTankSecondary() {
			return tankSecondary;
		}

		public void setTankSecondary(int tankSecondary) {
			this.tankSecondary = tankSecondary;
		}

		public int getTankPrimary() {
			return tankPrimary;
		}

		public void setTankPrimary(int tankPrimary) {
			this.tankPrimary = tankPrimary;
		}
	}
}
