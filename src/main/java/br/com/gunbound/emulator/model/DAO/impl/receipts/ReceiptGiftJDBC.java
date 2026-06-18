package br.com.gunbound.emulator.model.DAO.impl.receipts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptGiftDAO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptGiftDTO;

public class ReceiptGiftJDBC implements ReceiptGiftDAO {

	/**
	 * Busca um ReceiptGift pelo idx.
	 */
	@Override
	public ReceiptGiftDTO getReceiptGiftByIdxChest(int idxChest) {
		ReceiptGiftDTO receiptGift = null;
		String sql = "SELECT * FROM receiptgift WHERE Idx_chest = ?";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
			st.setInt(1, idxChest);

			try (ResultSet rs = st.executeQuery()) {
				if (rs.next()) {
					receiptGift = mapToDTO(rs);
				}
			}
		} catch (SQLException e) {
			throw new DbException("Erro ao buscar ReceiptGift pelo idx: " + e.getMessage());
		}

		return receiptGift;
	}
	
	@Override
	public ReceiptGiftDTO getReceiptGiftByReceiverTop1(String Receiver) {
		ReceiptGiftDTO receiptGift = null;
		String sql = "SELECT * FROM receiptgift WHERE Receiver = ? AND Confirmed = 0 ORDER BY Idx LIMIT 1";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
			st.setString(1, Receiver);

			try (ResultSet rs = st.executeQuery()) {
				if (rs.next()) {
					receiptGift = mapToDTO(rs);
				}
			}
		} catch (SQLException e) {
			throw new DbException("Erro ao buscar ReceiptGift pelo Receiver: " + e.getMessage());
		}

		return receiptGift;
	}

	/**
	 * Busca todos os ReceiptGift's.
	 */
	@Override
	public List<ReceiptGiftDTO> getAllReceiptGiftsFromUser(String receiver) {
		List<ReceiptGiftDTO> receiptGifts = new ArrayList<>();
		String sql = "SELECT * FROM receiptgift WHERE Receiver = ? AND Confirmed = 0";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

			// Define o valor do parâmetro para a consulta
			st.setString(1, receiver);

			try (ResultSet rs = st.executeQuery()) {
				while (rs.next()) {
					receiptGifts.add(mapToDTO(rs));
				}
			}
		} catch (SQLException e) {
			throw new DbException("Erro ao buscar todos os ReceiptGifts: " + e.getMessage());
		}

		return receiptGifts;
	}

	/**
	 * Insere um novo registro de ReceiptGift.
	 */
	@Override
	public void insertReceiptGift(ReceiptGiftDTO receiptGift) {
		String sql = "INSERT INTO receiptgift (Idx_chest, MenuId, Volume, Sender,SenderNick, Receiver, ReceiverNick, GiftTime, Expiretype, Text, ConfirmTime, Confirmed) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

			st.setInt(1, receiptGift.getIdxChest());
			st.setInt(2, receiptGift.getMenuId());
			st.setInt(3, receiptGift.getVolume());
			st.setString(4, receiptGift.getSender());
			st.setString(5, receiptGift.getSenderNick());
			st.setString(6, receiptGift.getReceiver());
			st.setString(7, receiptGift.getReceiverNick());
			st.setTimestamp(8, receiptGift.getGiftTime());
			st.setString(9, receiptGift.getExpireType());
			st.setString(10, receiptGift.getText());
			st.setTimestamp(11, receiptGift.getConfirmTime());
			st.setInt(12, receiptGift.getConfirmed());

			st.executeUpdate();

		} catch (SQLException e) {
			throw new DbException("Erro ao inserir ReceiptGift: " + e.getMessage());
		}
	}

	/**
	 * Atualiza um registro de ReceiptGift.
	 */
	@Override
	public void updateReceiptGiftConfirmed(int idx) {
		// String sql = "UPDATE receiptgift SET Idx_chest = ?, MenuId = ?, Volume = ?,
		// Sender = ?, Receiver = ?, ReceiverNick = ?, "
		// + "GiftTime = ?, Expiretype = ?, ConfirmTime = ?, Confirmed = ? WHERE
		// Idx_chest = ?";

		String sql = "UPDATE receiptgift SET ConfirmTime = ?, Confirmed = ? WHERE Idx = ?";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

			st.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			st.setInt(2, 1);
			st.setInt(3, idx);

			st.executeUpdate();

		} catch (SQLException e) {
			throw new DbException("Erro ao atualizar ReceiptGift: " + e.getMessage());
		}
	}

	/**
	 * Mapeia o ResultSet para um objeto ReceiptGiftDTO.
	 */
	private ReceiptGiftDTO mapToDTO(ResultSet rs) throws SQLException {
		ReceiptGiftDTO receiptGift = new ReceiptGiftDTO();
		receiptGift.setIdx(rs.getInt("Idx"));
		receiptGift.setIdxChest(rs.getInt("Idx_chest"));
		receiptGift.setMenuId(rs.getInt("MenuId"));
		receiptGift.setVolume(rs.getInt("Volume"));
		receiptGift.setSender(rs.getString("Sender"));
		receiptGift.setSenderNick(rs.getString("SenderNick"));
		receiptGift.setReceiver(rs.getString("Receiver"));
		receiptGift.setReceiverNick(rs.getString("ReceiverNick"));
		receiptGift.setGiftTime(rs.getTimestamp("GiftTime"));
		receiptGift.setExpireType(rs.getString("Expiretype"));
		receiptGift.setText(rs.getString("Text"));
		receiptGift.setConfirmTime(rs.getTimestamp("ConfirmTime"));
		receiptGift.setConfirmed(rs.getInt("Confirmed"));
		return receiptGift;
	}
}