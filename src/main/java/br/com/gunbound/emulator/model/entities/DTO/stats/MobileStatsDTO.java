package br.com.gunbound.emulator.model.entities.DTO.stats;

public class MobileStatsDTO {

	private int mobileId;
	private int wins;
	private int losses;

	public MobileStatsDTO(int mobileId, int wins, int losses) {
		this.mobileId = mobileId;
		this.wins = wins;
		this.losses = losses;
	}

	public MobileStatsDTO() {
		// TODO Auto-generated constructor stub
	}

	// Getters e Setters
	public int getMobileId() {
		return mobileId;
	}

	public void setMobileId(int mobileId) {
		this.mobileId = mobileId;
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