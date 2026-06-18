package br.com.gunbound.emulator.services.impl;

import java.util.List;
import java.util.Optional;

import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptBuyDAO;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptGiftDAO;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptLogDAO;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptSellDAO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptBuyDTO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptGiftDTO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptSellDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.ReceiptService;

public class ReceiptServiceImpl implements ReceiptService {

	private final ReceiptGiftDAO receiptGiftDAO;
	private final ReceiptBuyDAO receiptBuyDAO;
	private final ReceiptSellDAO receiptSellDAO;
	private final ReceiptLogDAO receiptLogDAO;

	public ReceiptServiceImpl() {
		this.receiptGiftDAO = DAOFactory.CreateReceiptGiftDao();
		this.receiptBuyDAO = DAOFactory.CreateReceiptBuyDao();
		this.receiptSellDAO = DAOFactory.CreateReceiptSellDao();
		this.receiptLogDAO = DAOFactory.CreateReceiptLogDao();
	}

	@Override
	public List<ReceiptGiftDTO> getPlayerReceiptGiftItems(PlayerSession player) {
		return receiptGiftDAO.getAllReceiptGiftsFromUser(player.getUserNameId());
	}

	@Override
	public void insertReceiptGift(ReceiptGiftDTO rGift) {
		receiptGiftDAO.insertReceiptGift(rGift);
	}

	private Optional<ReceiptGiftDTO> getReceiptGiftByReceiverTop1(PlayerSession player) {
		// Retorna um Optional. Se o método original retornar null, o Optional estará
		// vazio.
		return Optional.ofNullable(receiptGiftDAO.getReceiptGiftByReceiverTop1(player.getUserNameId()));
	}

	// atualiza o primeiro gift dado.
	@Override
	public void updateReceiptGiftConfirmed(PlayerSession player) {
		Optional<ReceiptGiftDTO> dto = getReceiptGiftByReceiverTop1(player);

		if (dto.isPresent()) {
			receiptGiftDAO.updateReceiptGiftConfirmed(dto.get().getIdx());
		}
	}
	
	@Override
	public void insertReceiptBuy(ReceiptBuyDTO rBuy) {
		receiptBuyDAO.insertReceiptBuy(rBuy);
	}
	
	@Override
	public void insertReceiptSell(ReceiptSellDTO rSell) {
		receiptSellDAO.insertReceiptSell(rSell);
	}
	
	@Override
	public void insertReceiptLogGeneral(PlayerSession ps, String item, String description) {
		receiptLogDAO.insertReceiptLog(ps, item, description);
	}
	
	@Override
	public void insertReceiptLogHonk(PlayerSession ps, String msg) {
		receiptLogDAO.insertHonkLog(ps, msg);
	}


}
