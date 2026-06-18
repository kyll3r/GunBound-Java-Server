package br.com.gunbound.emulator.buddy.db.dao.imp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.gunbound.emulator.buddy.db.dao.PacketDao;
import br.com.gunbound.emulator.buddy.db.exceptions.DbInsertDuplicate;
import br.com.gunbound.emulator.buddy.entities.dto.PacketDTO;
import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;

public class PacketJDBC implements PacketDao {

	@Override
	public int insert(PacketDTO packet) {
		String sql = "INSERT INTO packet (Receiver, Sender, Code, Body) VALUES (?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pst.setString(1, packet.getReceiver());
			pst.setString(2, packet.getSender());
			pst.setInt(3, packet.getCode());
			pst.setBytes(4, packet.getBody());
			pst.executeUpdate();
			try (ResultSet keys = pst.getGeneratedKeys()) {
				if (keys.next())
					return keys.getInt(1);
			}
		} catch (SQLException e) {
			// Verifica se é erro de chave única duplicada (Duplicate entry)
			if (e.getErrorCode() == 1062) {
				// Pode retornar um valor especial, lançar uma exceção específica, logar, etc.
				throw new DbInsertDuplicate("Já existe uma mensagem desse tipo entre esses usuários.");
			} else {
				throw new DbException("Erro ao inserir packet: " + e.getMessage());
			}
		}
		return -1;
	}

	@Override
	public PacketDTO getBySerialNo(int serialNo) {
		String sql = "SELECT SerialNo, Receiver, Sender, Code, Body, Time FROM packet WHERE SerialNo = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setInt(1, serialNo);
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					return populatePacketDTO(rs);
				}
			}
		} catch (SQLException e) {
			throw new DbException("Erro ao buscar packet: " + e.getMessage());
		}
		return null;
	}

	@Override
	public List<PacketDTO> getByReceiver(String receiver) {
		List<PacketDTO> list = new ArrayList<>();
		String sql = "SELECT SerialNo, Receiver, Sender, Code, Body, Time FROM packet WHERE Receiver = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setString(1, receiver);
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					list.add(populatePacketDTO(rs));
				}
			}
		} catch (SQLException e) {
			throw new DbException("Erro ao buscar packets por Receiver: " + e.getMessage());
		}
		return list;
	}

	@Override
	public boolean deleteBySerialNo(int serialNo) {
		String sql = "DELETE FROM packet WHERE SerialNo = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {

			pst.setInt(1, serialNo);

			int rowsAffected = pst.executeUpdate();
			return rowsAffected > 0; // true se deletou algum registro
		} catch (SQLException e) {
			throw new DbException("Erro ao deletar packet pelo SerialNo: " + e.getMessage());
		}
	}

	private PacketDTO populatePacketDTO(ResultSet rs) throws SQLException {
		PacketDTO p = new PacketDTO();
		p.setSerialNo(rs.getInt("SerialNo"));
		p.setReceiver(rs.getString("Receiver"));
		p.setSender(rs.getString("Sender"));
		p.setCode(rs.getInt("Code"));
		p.setBody(rs.getBytes("Body"));
		p.setTime(rs.getTimestamp("Time"));
		return p;
	}
}
