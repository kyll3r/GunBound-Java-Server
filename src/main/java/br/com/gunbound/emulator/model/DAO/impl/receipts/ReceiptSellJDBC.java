package br.com.gunbound.emulator.model.DAO.impl.receipts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptSellDAO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptSellDTO;

public class ReceiptSellJDBC implements ReceiptSellDAO {
	/**
	 * Insere um novo registro de ReceiptGift.
	 */
	@Override
	public void insertReceiptSell(ReceiptSellDTO receiptBuy) {
		  String sql = "INSERT INTO receiptsell (Consumer, MenuId, Idx_Chest, CashChecked, GoldChecked, Time) " +
                  "VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, receiptBuy.getConsumer());
            st.setInt(2, receiptBuy.getMenuId());
            st.setInt(3, receiptBuy.getIdxChest());
            st.setInt(4, receiptBuy.getCashChecked());
            st.setInt(5, receiptBuy.getGoldChecked());
            st.setTimestamp(6, receiptBuy.getTime());


			st.executeUpdate();

		} catch (SQLException e) {
			throw new DbException("Erro ao inserir ReceiptGift: " + e.getMessage());
		}
	}


	/**
	 * Mapeia o ResultSet para um objeto ReceiptSellDTO.
	 */
    // Método auxiliar para mapear o ResultSet para um objeto ReceiptBuy
    private ReceiptSellDTO mapResultSetToReceiptBuy(ResultSet rs) throws SQLException {
        // Cria uma nova instância de ReceiptBuy
    	ReceiptSellDTO receiptSell = new ReceiptSellDTO();
        
        // Mapeia as colunas do ResultSet para os campos da classe ReceiptBuy
        receiptSell.setIdx(rs.getInt("Idx"));
        receiptSell.setConsumer(rs.getString("Consumer"));
        receiptSell.setMenuId(rs.getInt("MenuId"));
        receiptSell.setIdxChest(rs.getInt("Idx_Chest"));
        receiptSell.setCashChecked(rs.getInt("CashChecked"));
        receiptSell.setGoldChecked(rs.getInt("GoldChecked"));
        receiptSell.setTime(rs.getTimestamp("Time"));

        // Retorna o objeto ReceiptSell mapeado
        return receiptSell;
    }
}