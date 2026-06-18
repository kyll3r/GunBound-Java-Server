package br.com.gunbound.emulator.model.DAO.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.GameBuddyDAO;
import br.com.gunbound.emulator.model.entities.game.GameBuddyList;

public class GameBuddyJDBC implements GameBuddyDAO {

	@Override
	public List<GameBuddyList> findBuddiesByUserId(String userId) {
		List<GameBuddyList> buddiesList = new ArrayList<GameBuddyList>();
		String sql = "SELECT * FROM buddylist WHERE UserId = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setString(1, userId);
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					buddiesList.add(populateBuddy(rs));
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		return buddiesList;
	}

	// 'Mapper' auxiliar
	private GameBuddyList populateBuddy(ResultSet rs) throws SQLException {
		GameBuddyList b = new GameBuddyList();
		b.setUserId(rs.getString("UserId"));
		b.setCategory(rs.getString("Category"));
		b.setBuddyId(rs.getString("BuddyId"));
		return b;
	}

}
