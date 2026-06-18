package br.com.gunbound.emulator.model.DAO.impl.receipts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptLogDAO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;

public class ReceiptLogJDBC implements ReceiptLogDAO {
	/**
	 * Insere um novo registro de ReceiptLog.
	 */
	@Override
	public void insertReceiptLog(PlayerSession ps, String item, String description ) {
		  String sql = "INSERT INTO receiptlog (UserId, UserNickName, Item, ItemDescription, Time) " +
		            "VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

			st.setString(1, ps.getUserNameId());
            st.setString(2, ps.getNickName());
            st.setString(3, item);
            st.setString(4, description);
            st.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

			st.executeUpdate();

		} catch (SQLException e) {
			throw new DbException("Erro ao inserir ReceiptLog: " + e.getMessage());
		}
	}
	
	@Override
	public void insertHonkLog(PlayerSession ps, String phrase) {
		  String sql = "INSERT INTO honklog (UserId, UserNickName, Phrase, Time) " +
		            "VALUES (?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

			st.setString(1, ps.getUserNameId());
            st.setString(2, ps.getNickName());
            st.setString(3, phrase);
            st.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

			st.executeUpdate();

		} catch (SQLException e) {
			throw new DbException("Erro ao inserir RHonklog: " + e.getMessage());
		}
	}

}