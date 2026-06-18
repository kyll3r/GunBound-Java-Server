package br.com.gunbound.emulator.buddy.entities;

public class BuddyList {

	private String userId;
	private String category;
	private String buddyId;

	public BuddyList() {
	}

	public BuddyList(String userId, String category, String buddyId) {
		this.userId = userId;
		this.category = category;
		this.buddyId = buddyId;
	}

	// Getters e Setters

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getBuddyId() {
		return buddyId;
	}

	public void setBuddyId(String buddyId) {
		this.buddyId = buddyId;
	}

	@Override
	public String toString() {
		return "BuddyList{" + "userId='" + userId + '\'' + ", category='" + category + '\'' + ", buddyId='" + buddyId
				+ '\'' + '}';
	}
}