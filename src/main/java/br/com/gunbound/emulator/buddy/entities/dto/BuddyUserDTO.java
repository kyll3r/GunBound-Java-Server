package br.com.gunbound.emulator.buddy.entities.dto;

public class BuddyUserDTO {
	// --- user table ---
	private int id;
	private String userId;
	private int gender;
	private String password;

	// --- game table ---
	private String nickname;
	private String guild;
	private int guildRank;
	private int memberGuildCount;

	private int totalGrade;
	private int seasonGrade;

	public BuddyUserDTO() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserId() {
		return userId.toLowerCase();
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		if (nickname.isEmpty() || nickname.isBlank() || nickname == null) {
			return userId;
		}
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGuild() {
		return guild;
	}

	public void setGuild(String guild) {
		this.guild = guild;
	}

	public int getGuildRank() {
		return guildRank;
	}

	public void setGuildRank(int guildRank) {
		this.guildRank = guildRank;
	}

	public int getMemberGuildCount() {
		return memberGuildCount;
	}

	public void setMemberGuildCount(int memberGuildCount) {
		this.memberGuildCount = memberGuildCount;
	}

	public int getTotalGrade() {
		return totalGrade;
	}

	public void setTotalGrade(int totalGrade) {
		this.totalGrade = totalGrade;
	}

	public int getSeasonGrade() {
		return seasonGrade;
	}

	public void setSeasonGrade(int seasonGrade) {
		this.seasonGrade = seasonGrade;
	}

}
