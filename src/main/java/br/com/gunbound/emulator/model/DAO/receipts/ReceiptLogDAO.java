package br.com.gunbound.emulator.model.DAO.receipts;

import br.com.gunbound.emulator.model.entities.game.PlayerSession;

public interface ReceiptLogDAO {
	public void insertReceiptLog(PlayerSession ps, String item, String description);
	public void insertHonkLog(PlayerSession ps, String phrase);
}
