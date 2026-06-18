package br.com.gunbound.emulator.buddy.db.dao.imp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.gunbound.emulator.buddy.db.dao.BuddyUserDAO;
import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;
import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;

public class BuddyUserJDBC implements BuddyUserDAO {

	@Override
	public BuddyUserDTO getUserByUserId(String userIdQuery) {
		String sql = "SELECT " + "u.Id, u.UserId, u.Gender, u.Password, "
				+ "g.NickName, g.Guild, g.GuildRank, g.MemberGuildCount, "
				+ "g.TotalGrade, g.SeasonGrade "
				+ "FROM user u JOIN game g ON u.UserId = g.UserId WHERE u.UserId = ?";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, userIdQuery);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					BuddyUserDTO user = new BuddyUserDTO();

					return BuildUserResultSet(rs, user);
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		return null;
	}

	@Override
	public BuddyUserDTO getUserByNickname(String nicknameQuery) {
		String sql = "SELECT " + "u.Id, u.UserId, u.Gender, u.Password, "
				+ "g.NickName, g.Guild, g.GuildRank, g.MemberGuildCount, "
				+ "g.TotalGrade, g.SeasonGrade "
				+ "FROM user u JOIN game g ON u.UserId = g.UserId WHERE g.NickName = ?"; // Alteração: usando NickName
																							// ao invés de UserId

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, nicknameQuery); // Agora passando nicknameQuery como parâmetro
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					BuddyUserDTO user = new BuddyUserDTO();

					return BuildUserResultSet(rs, user);
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		return null;
	}


	private BuddyUserDTO BuildUserResultSet(ResultSet rs, BuddyUserDTO user) throws SQLException {
		user.setId(rs.getInt("Id"));
		user.setUserId(rs.getString("UserId"));
		user.setGender(rs.getInt("Gender"));
		user.setPassword(rs.getString("Password"));

		user.setNickname(rs.getString("NickName"));
		user.setGuild(rs.getString("Guild"));
		user.setGuildRank(rs.getInt("GuildRank"));
		user.setMemberGuildCount(rs.getInt("MemberGuildCount"));

		user.setTotalGrade(rs.getInt("TotalGrade"));
		user.setSeasonGrade(rs.getInt("SeasonGrade"));

		return user;
	}

}
