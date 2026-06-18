package br.com.gunbound.emulator.model.DAO.impl.receipts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptBuyDAO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptBuyDTO;

public class ReceiptBuyJDBC implements ReceiptBuyDAO {
	/**
	 * Insere um novo registro de ReceiptGift.
	 */
	@Override
	public void insertReceiptBuy(ReceiptBuyDTO receiptBuy) {
		  String sql = "INSERT INTO receiptbuy (Consumer, MenuId, Idx_Chest, CashChecked, GoldChecked, Time, BuyType, ExpireType, ReceiptGiftNo) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, receiptBuy.getConsumer());
            st.setInt(2, receiptBuy.getMenuId());
            st.setInt(3, receiptBuy.getIdxChest());
            st.setInt(4, receiptBuy.getCashChecked());
            st.setInt(5, receiptBuy.getGoldChecked());
            st.setTimestamp(6, receiptBuy.getTime());
            st.setString(7, receiptBuy.getBuyType());
            st.setString(8, receiptBuy.getExpireType());
            st.setInt(9, receiptBuy.getReceiptGiftNo());

			st.executeUpdate();

		} catch (SQLException e) {
			throw new DbException("Erro ao inserir ReceiptGift: " + e.getMessage());
		}
	}


	/**
	 * Mapeia o ResultSet para um objeto ReceiptGiftDTO.
	 */
    // Método auxiliar para mapear o ResultSet para um objeto ReceiptBuy
    private ReceiptBuyDTO mapResultSetToReceiptBuy(ResultSet rs) throws SQLException {
        // Cria uma nova instância de ReceiptBuy
        ReceiptBuyDTO receiptBuy = new ReceiptBuyDTO();
        
        // Mapeia as colunas do ResultSet para os campos da classe ReceiptBuy
        receiptBuy.setIdx(rs.getInt("Idx"));
        receiptBuy.setConsumer(rs.getString("Consumer"));
        receiptBuy.setMenuId(rs.getInt("MenuId"));
        receiptBuy.setIdxChest(rs.getInt("Idx_Chest"));
        receiptBuy.setCashChecked(rs.getInt("CashChecked"));
        receiptBuy.setGoldChecked(rs.getInt("GoldChecked"));
        receiptBuy.setTime(rs.getTimestamp("Time"));
        receiptBuy.setBuyType(rs.getString("BuyType"));
        receiptBuy.setExpireType(rs.getString("ExpireType"));
        receiptBuy.setReceiptGiftNo(rs.getInt("ReceiptGiftNo"));

        // Retorna o objeto ReceiptBuy mapeado
        return receiptBuy;
    }
}