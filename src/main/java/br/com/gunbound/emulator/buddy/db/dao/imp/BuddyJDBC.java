package br.com.gunbound.emulator.buddy.db.dao.imp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.gunbound.emulator.buddy.db.dao.BuddyListDao;
import br.com.gunbound.emulator.buddy.db.exceptions.DbInsertDuplicate;
import br.com.gunbound.emulator.buddy.entities.BuddyList;
import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;

public class BuddyJDBC implements BuddyListDao {

	@Override
	public List<BuddyList> findBuddiesByUserId(String userId) {
		List<BuddyList> buddiesList = new ArrayList<>();
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

	@Override
	public int addBuddy(String userId, String buddyId, String category) {
		String sql = "INSERT INTO buddylist (UserId, Category, BuddyId) VALUES (?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pst.setString(1, userId);
			pst.setString(2, category);
			pst.setString(3, buddyId);
			pst.executeUpdate();
			try (ResultSet keys = pst.getGeneratedKeys()) {
				if (keys.next())
					return keys.getInt(1);
			}
		} catch (SQLException e) {
			// Verifica se é erro de chave única duplicada (Duplicate entry)
			if (e.getErrorCode() == 1062) {
				// Pode retornar um valor especial, lançar uma exceção específica, logar, etc.
				throw new DbInsertDuplicate("Já existe relacionamento entre esses usuários.");
			} else {
				throw new DbException("Erro ao inserir buddie: " + e.getMessage());
			}
		}
		return -1;

	}

	@Override
	public boolean deleteBuddy(String userId, String buddyId) {
		String sql = "DELETE FROM buddylist WHERE UserId = ? AND BuddyId = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {

			pst.setString(1, userId);
			pst.setString(2, buddyId);

			int rowsAffected = pst.executeUpdate();
			return rowsAffected > 0; // true se deletou algum registro
		} catch (SQLException e) {
			throw new DbException("Erro ao deletar buddie pelo deleteBuddy: " + e.getMessage());
		}

	}

	@Override
	public boolean updateBuddyGroup(String userId, String buddyId, String newCategory) {
		// TODO Auto-generated method stub
		return 0 > 0;
	}

	// 'Mapper' auxiliar
	private BuddyList populateBuddy(ResultSet rs) throws SQLException {
		BuddyList b = new BuddyList();
		b.setUserId(rs.getString("UserId"));
		b.setCategory(rs.getString("Category"));
		b.setBuddyId(rs.getString("BuddyId"));
		return b;
	}

}
