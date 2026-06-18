package br.com.gunbound.emulator.services;

import java.util.List;

import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptBuyDTO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptGiftDTO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptSellDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;

public interface ReceiptService {
	
	public List<ReceiptGiftDTO> getPlayerReceiptGiftItems(PlayerSession player);
	void insertReceiptGift(ReceiptGiftDTO rGift);
	public void updateReceiptGiftConfirmed(PlayerSession player);
	public void insertReceiptBuy(ReceiptBuyDTO rBuy);
	public void insertReceiptSell(ReceiptSellDTO rSell);
	public void insertReceiptLogGeneral(PlayerSession ps, String item, String description);
	public void insertReceiptLogHonk(PlayerSession ps, String msg);
}
