package br.com.gunbound.emulator.model.entities.game;

public class PlayerGameResult {
	Integer normalGold=0;
	Integer bonusGold=0;
	Integer eventPoint=0;
	Integer normalGp=0;
	Integer hits=0;
	Integer damage=0;

	public PlayerGameResult(Integer normalGold, Integer bonusGold, Integer eventPoint, Integer normalGp,
			Integer bonusGp, Integer damage) {
		super();
		this.normalGold = normalGold;
		this.bonusGold = bonusGold;
		this.eventPoint = eventPoint;
		this.normalGp = normalGp;
		this.hits = bonusGp;
		this.damage = damage;
	}
	
	public Integer getNormalGold() {
		return normalGold;
	}
	public void setNormalGold(Integer normalGold) {
		this.normalGold = normalGold;
	}
	public Integer getBonusGold() {
		return bonusGold;
	}
	public void setBonusGold(Integer bonusGold) {
		this.bonusGold = bonusGold;
	}
	public Integer getEventPoint() {
		return eventPoint;
	}
	public void setEventPoint(Integer eventPoint) {
		this.eventPoint = eventPoint;
	}
	public Integer getNormalGp() {
		return normalGp;
	}
	public void setNormalGp(Integer normalGp) {
		this.normalGp = normalGp;
	}
	public Integer getHits() {
		return hits;
	}
	public void setHits(Integer bonusGp) {
		this.hits = bonusGp;
	}
	
	public Integer getDamage() {
		return damage;
	}

	public void setDamage(Integer damage) {
		this.damage = damage;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PlayerGameResult [normalGold=");
		builder.append(normalGold);
		builder.append(", bonusGold=");
		builder.append(bonusGold);
		builder.append(", eventPoint=");
		builder.append(eventPoint);
		builder.append(", normalGp=");
		builder.append(normalGp);
		builder.append(", bonusGp=");
		builder.append(hits);
		builder.append("]");
		return builder.toString();
	}
}
