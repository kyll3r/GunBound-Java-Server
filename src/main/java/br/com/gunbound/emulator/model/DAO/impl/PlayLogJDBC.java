package br.com.gunbound.emulator.model.DAO.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.PlayLogDAO;
import br.com.gunbound.emulator.model.entities.DTO.PlayLogDTO;
import br.com.gunbound.emulator.model.entities.DTO.PlayLogDTO.PlayerLog;

public class PlayLogJDBC implements PlayLogDAO {

	@Override
	public void save(PlayLogDTO playlog) {
		StringBuilder sql = new StringBuilder(
				"INSERT INTO playlog (ServerIP, GameRoomID, GameRoomTitle, StartTime, EndTime, GameOption, WinTeamOrPlayer");
		StringBuilder values = new StringBuilder("?, ?, ?, ?, ?, ?, ?");

		// Constrói a query dinamicamente com base no número de jogadores
		for (PlayerLog player : playlog.getPlayers()) {
			int slot = player.getSlot();
			sql.append(", S").append(slot).append("_ID");
			sql.append(", S").append(slot).append("_TeamID");
			sql.append(", S").append(slot).append("_DeadTime");
			sql.append(", S").append(slot).append("_DeadCause");
			sql.append(", S").append(slot).append("_ScoreDelta");
			sql.append(", S").append(slot).append("_MoneyDelta");
			values.append(", ?, ?, ?, ?, ?, ?");
		}

		sql.append(") VALUES (").append(values).append(")");

		try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			// Define os parâmetros do cabeçalho
			stmt.setInt(1, playlog.getServerIp());
			stmt.setInt(2, playlog.getGameRoomId());
			stmt.setString(3, playlog.getGameRoomTitle());
			stmt.setTimestamp(4, Timestamp.valueOf(playlog.getStartTime()));
			stmt.setTimestamp(5, Timestamp.valueOf(playlog.getEndTime()));
			stmt.setInt(6, playlog.getGameOption());
			stmt.setInt(7, playlog.getWinTeamOrPlayer());

			// Define os parâmetros de cada jogador
			int paramIndex = 8;
			for (PlayerLog player : playlog.getPlayers()) {
				stmt.setString(paramIndex++, player.getUserId());
				stmt.setInt(paramIndex++, player.getTeamId());
				stmt.setTimestamp(paramIndex++,
						player.getDeadTime() != null ? Timestamp.valueOf(player.getDeadTime()) : null);
				stmt.setInt(paramIndex++, player.getDeadCause());
				stmt.setInt(paramIndex++, player.getScoreDelta());
				stmt.setInt(paramIndex++, player.getMoneyDelta());
			}

			stmt.executeUpdate();
			System.out.println("Playlog salvo para a sala: " + playlog.getGameRoomId());

		} catch (SQLException e) {
			throw new DbException("Erro ao salvar playlog: " + e.getMessage());
		}
	}
}
