package br.com.gunbound.emulator.model.DAO.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.UserDAO;
import br.com.gunbound.emulator.model.entities.DTO.UserDTO;

public class UserJDBC implements UserDAO {

	@Override
	public Optional<UserDTO> getUserByUserId(String userIdQuery) {
		String sql = "SELECT " + "u.Id, u.UserId, u.Gender, u.Password, u.Status, u.MuteTime, u.RestrictTime, "
				+ "u.Authority, u.Authority2, u.AuthorityBackup, u.E_Mail, u.Country, u.User_Level, u.Dia, u.Mes, u.Ano, u.Created,"
				+ "g.NickName, g.Guild, g.GuildRank, g.MemberGuildCount, g.Gold, g.Cash, "
				+ "g.EventScore0, g.EventScore1, g.EventScore2, g.EventScore3, "
				+ "g.Prop1, g.Prop2, g.AdminGift, g.TotalScore, g.SeasonScore, g.TotalGrade, g.SeasonGrade, "
				+ "g.TotalRank, g.SeasonRank, g.AccumShot, g.AccumDamage, g.LastUpdateTime, g.NoRankUpdate, "
				+ "g.ClientData, g.Country as gameCountry, g.GiftProhibitTime, g.CorGame, g.CorGameTime, g.CorGameBalao, g.CorGameBalaoTime "
				+ "FROM user u JOIN game g ON u.UserId = g.UserId WHERE u.UserId = ?";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, userIdQuery);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					UserDTO user = new UserDTO();

					return Optional.of(BuildUserResultSet(rs, user));
					// return BuildUserResultSet(rs, user);
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		return Optional.empty();
	}

	@Override
	public UserDTO getUserByNickname(String nicknameQuery) {
		String sql = "SELECT " + "u.Id, u.UserId, u.Gender, u.Password, u.Status, u.MuteTime, u.RestrictTime, "
				+ "u.Authority, u.Authority2, u.AuthorityBackup, u.E_Mail, u.Country, u.User_Level, u.Dia, u.Mes, u.Ano, u.Created, "
				+ "g.NickName, g.Guild, g.GuildRank, g.MemberGuildCount, g.Gold, g.Cash, "
				+ "g.EventScore0, g.EventScore1, g.EventScore2, g.EventScore3, "
				+ "g.Prop1, g.Prop2, g.AdminGift, g.TotalScore, g.SeasonScore, g.TotalGrade, g.SeasonGrade, "
				+ "g.TotalRank, g.SeasonRank, g.AccumShot, g.AccumDamage, g.LastUpdateTime, g.NoRankUpdate, "
				+ "g.ClientData, g.Country as gameCountry, g.GiftProhibitTime "
				+ "FROM user u JOIN game g ON u.UserId = g.UserId WHERE g.NickName = ?"; // Alteração: usando NickName
																							// ao invés de UserId

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, nicknameQuery); // Agora passando nicknameQuery como parâmetro
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					UserDTO user = new UserDTO();

					return BuildUserResultSet(rs, user);
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		return null;
	}

	@Override
	public void updateAddGold(String playerId, int value) {
		updateGold(playerId, value, "+");
	}

	@Override
	public void updateMinusGold(String playerId, int value) {
		updateGold(playerId, value, "-");
	}

	public void updateGold(String playerId, int value, String op) {
		if (op == null)
			return;

		String sql = "UPDATE game SET Gold = Gold " + op + " ? WHERE UserId = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, value);
			stmt.setString(2, playerId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}

	}

	@Override
	public void updateAddCash(String playerId, int value) {
		updateCash(playerId, value, "+");
	}

	@Override
	public void updateMinusCash(String playerId, int value) {
		updateCash(playerId, value, "-");
	}

	public void updateCash(String playerId, int value, String op) {
		if (op == null)
			return;

		String sql = "UPDATE game SET Cash = Cash " + op + " ? WHERE UserId = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, value);
			stmt.setString(2, playerId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}

	}

	@Override
	public void updateUserStatsAfterGame(String userId, int gpGanhos, int goldGanhos, int eventPointsGanhos,
			int hits, int damage) {
		// A query usa 'CAMPO = CAMPO + ?' para garantir que a atualização seja atômica.
		// Isso evita problemas se duas partidas do mesmo jogador terminarem ao mesmo
		// tempo.
		String sql = "UPDATE game SET TotalScore = TotalScore + ?, SeasonScore = SeasonScore + ?, Gold = Gold + ?, EventScore0 = EventScore0 + ?,"
				+ " AccumShot = AccumShot + ?, AccumDamage = AccumDamage + ?  WHERE UserId = ?";

		// O bloco try-with-resources garante que a conexão e o statement sejam fechados
		// automaticamente.
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, gpGanhos);
			stmt.setInt(2, gpGanhos);
			stmt.setInt(3, goldGanhos);
			stmt.setInt(4, eventPointsGanhos);
			stmt.setInt(5, hits);
			stmt.setInt(6, damage);
			
			stmt.setString(7, userId);

			stmt.executeUpdate();

		} catch (SQLException e) {
			// Lança uma exceção de runtime para sinalizar o erro na camada de serviço.
			throw new DbException("Erro ao atualizar status do jogador " + userId + " após o jogo: " + e.getMessage());
		}
	}

	@Override
	public void updateAuthority(String playerId, int value) {
		if (playerId == null)
			return;

		String sql = "UPDATE user SET Authority = ? WHERE UserId = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, value);
			stmt.setString(2, playerId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}

	}
	
	@Override
	public void banPlayer(String playerId, int authy, Timestamp expire) {
		if (playerId == null)
			return;

		String sql = "UPDATE user SET Authority = ?, RestrictTime = ? WHERE UserId = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, authy);
			stmt.setTimestamp(2, expire);
			stmt.setString(3, playerId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}

	}
	
	@Override
	public void updateColors(String playerId, int colorId, Timestamp expire, boolean isBalao) {
	    if (playerId == null) return;

	    String colorColumn = isBalao ? "CorGameBalao" : "CorGame";
	    String timeColumn  = isBalao ? "CorGameBalaoTime" : "CorGameTime";

	    String sql = "UPDATE game SET " + colorColumn + " = ?, " + timeColumn + " = ? WHERE UserId = ?";

	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, colorId);
	        stmt.setTimestamp(2, expire);
	        stmt.setString(3, playerId);

	        stmt.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}

	}
	
	@Override
	public int insertBanLog(String userBanned, Timestamp duration, String reason, String adminId, String adminNick) {
		String sql = "INSERT INTO banlog (StartTime, UserId, Duration, Reason, JudgeId, JudgeNickName) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			pst.setString(2, userBanned);
			pst.setTimestamp(3, duration);
			pst.setString(4, reason);
			pst.setString(5, adminId);
			pst.setString(6, adminNick);

			pst.executeUpdate();
			try (ResultSet keys = pst.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getInt(1); // retorna o novo Idx
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		return -1; // erro
	}

	private UserDTO BuildUserResultSet(ResultSet rs, UserDTO user) throws SQLException {
		user.setId(rs.getInt("Id"));
		user.setUserId(rs.getString("UserId"));
		user.setGender(rs.getInt("Gender"));
		user.setPassword(rs.getString("Password"));
		user.setStatus(rs.getString("Status"));
		user.setMuteTime(rs.getTimestamp("MuteTime"));
		user.setRestrictTime(rs.getTimestamp("RestrictTime"));
		user.setAuthority(rs.getInt("Authority"));
		user.setAuthority2(rs.getInt("Authority2"));
		user.setAuthorityBackup(rs.getInt("AuthorityBackup"));
		user.setEmail(rs.getString("E_Mail"));
		user.setCountry(rs.getInt("Country"));
		user.setUserLevel(rs.getInt("User_Level"));
		user.setDia(rs.getInt("Dia"));
		user.setMes(rs.getInt("Mes"));
		user.setAno(rs.getInt("Ano"));
		user.setCreated(rs.getTimestamp("Created"));

		user.setNickname(rs.getString("NickName"));
		user.setGuild(rs.getString("Guild"));
		user.setGuildRank(rs.getInt("GuildRank"));
		user.setMemberGuildCount(rs.getInt("MemberGuildCount"));
		user.setGold(rs.getInt("Gold"));
		user.setCash(rs.getInt("Cash"));

		user.setEventScore0(rs.getInt("EventScore0"));
		user.setEventScore1(rs.getInt("EventScore1"));
		user.setEventScore2(rs.getInt("EventScore2"));
		user.setEventScore3(rs.getInt("EventScore3"));

		user.setProp1(rs.getString("Prop1"));
		user.setProp2(rs.getString("Prop2"));
		user.setAdminGift(rs.getInt("AdminGift"));
		user.setTotalScore(rs.getInt("TotalScore"));
		user.setSeasonScore(rs.getInt("SeasonScore"));
		user.setTotalGrade(rs.getInt("TotalGrade"));
		user.setSeasonGrade(rs.getInt("SeasonGrade"));
		user.setTotalRank(rs.getInt("TotalRank"));
		user.setSeasonRank(rs.getInt("SeasonRank"));
		user.setAccumShot(rs.getInt("AccumShot"));
		user.setAccumDamage(rs.getInt("AccumDamage"));
		user.setLastUpdateTime(rs.getTimestamp("LastUpdateTime"));

		user.setNoRankUpdate(rs.getBoolean("NoRankUpdate"));
		user.setClientData(rs.getBytes("ClientData"));
		user.setGameCountry(rs.getInt("gameCountry"));
		user.setGiftProhibitTime(rs.getTimestamp("GiftProhibitTime"));
		
		user.setCorGameTime(rs.getTimestamp("CorGameTime"));
		user.setCorGame(rs.getInt("CorGame"));
		
		user.setCorGameBalaoTime(rs.getTimestamp("CorGameBalaoTime"));
		user.setCorGameBalao(rs.getInt("CorGameBalao"));

		return user;
	}

}
