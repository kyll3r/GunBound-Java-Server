package br.com.gunbound.emulator.model.DAO.receipts;

import java.util.List;

import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptGiftDTO;

public interface ReceiptGiftDAO {
	public ReceiptGiftDTO getReceiptGiftByIdxChest(int idxChest);
	public List<ReceiptGiftDTO> getAllReceiptGiftsFromUser(String receiver);
	public void insertReceiptGift(ReceiptGiftDTO receiptGift);
	public void updateReceiptGiftConfirmed(int idx);
	public ReceiptGiftDTO getReceiptGiftByReceiverTop1(String Receiver);
}
