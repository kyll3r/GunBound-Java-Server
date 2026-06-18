package br.com.gunbound.emulator.model.entities.DTO.receipts;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import br.com.gunbound.emulator.model.entities.DTO.ChestDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;

public class ReceiptSellDTO {
	private int idx;
	private String consumer;
	private int menuId;
	private int idxChest;
	private int cashChecked;
	private int goldChecked;
	private Timestamp time;

	// Construtores
	public ReceiptSellDTO() {
	}

	public ReceiptSellDTO(PlayerSession ps, ChestDTO chestDTO, int currencyType, int price) {
		this.consumer = ps.getUserNameId();
		this.menuId = chestDTO.getItem();
		this.idxChest = chestDTO.getIdx();
		if (currencyType == 0) {
			this.goldChecked = price;
		} else {
			this.cashChecked = price;
		}
		this.time = Timestamp.valueOf(LocalDateTime.now());
	}

	public ReceiptSellDTO(int idx, String consumer, int menuId, int idxChest, int cashChecked, int goldChecked,
			Timestamp time) {
		this.idx = idx;
		this.consumer = consumer;
		this.menuId = menuId;
		this.idxChest = idxChest;
		this.cashChecked = cashChecked;
		this.goldChecked = goldChecked;
		this.time = time;
	}

	// Getters e Setters

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getConsumer() {
		return consumer;
	}

	public void setConsumer(String consumer) {
		this.consumer = consumer;
	}

	public int getMenuId() {
		return menuId;
	}

	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}

	public int getIdxChest() {
		return idxChest;
	}

	public void setIdxChest(int idxChest) {
		this.idxChest = idxChest;
	}

	public int getCashChecked() {
		return cashChecked;
	}

	public void setCashChecked(int cashChecked) {
		this.cashChecked = cashChecked;
	}

	public int getGoldChecked() {
		return goldChecked;
	}

	public void setGoldChecked(int goldChecked) {
		this.goldChecked = goldChecked;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "ReceiptBuy{" + "idx=" + idx + ", consumer='" + consumer + '\'' + ", menuId=" + menuId + ", idxChest="
				+ idxChest + ", cashChecked=" + cashChecked + ", goldChecked=" + goldChecked + ", time=" + time + '}';
	}
}
