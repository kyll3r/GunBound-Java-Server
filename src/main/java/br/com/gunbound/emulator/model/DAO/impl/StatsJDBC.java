package br.com.gunbound.emulator.model.DAO.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.StatsDAO;
import br.com.gunbound.emulator.model.entities.DTO.stats.MapStatsDTO;
import br.com.gunbound.emulator.model.entities.DTO.stats.MobileStatsDTO;
import br.com.gunbound.emulator.model.entities.DTO.stats.PlayerStatsDTO;

/**
 * Implementação JDBC do StatsDAO para buscar estatísticas do banco de dados.
 */
public class StatsJDBC implements StatsDAO {

    @Override
    public PlayerStatsDTO getPlayerStatsByUserId(String userId) {
        PlayerStatsDTO stats = new PlayerStatsDTO();
        
        // Usar try-with-resources garante que a conexão será fechada
        try (Connection conn = DatabaseManager.getConnection()) {
            
            // Carrega as estatísticas dos mobiles
            List<MobileStatsDTO> mobileStats = loadMobileStats(conn, userId);
            stats.setMobileStats(mobileStats);
            
            // Carrega as estatísticas dos mapas (stages)
            List<MapStatsDTO> mapStats = loadStageStats(conn, userId);
            stats.setMapStats(mapStats);

        } catch (SQLException e) {
            throw new DbException("Erro ao buscar estatísticas do jogador: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Busca os registros de vitórias/derrotas por mobile.
     */
    private List<MobileStatsDTO> loadMobileStats(Connection conn, String userId) throws SQLException {
        List<MobileStatsDTO> list = new ArrayList<>();
        String sql = "SELECT MobileId, Win, Lose FROM mobilerecord WHERE UserId = ?";
        
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, userId);
            
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    MobileStatsDTO mobileStat = new MobileStatsDTO();
                    mobileStat.setMobileId(rs.getInt("MobileId"));
                    mobileStat.setWin(rs.getInt("Win"));
                    mobileStat.setLose(rs.getInt("Lose"));
                    list.add(mobileStat);
                }
            }
        }
        return list;
    }

    /**
     * Busca os registros de vitórias/derrotas por mapa (stage).
     */
    private List<MapStatsDTO> loadStageStats(Connection conn, String userId) throws SQLException {
        List<MapStatsDTO> list = new ArrayList<>();
        String sql = "SELECT MapId, Win, Lose FROM stagerecord WHERE UserId = ?";
        
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, userId);
            
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    MapStatsDTO mapStat = new MapStatsDTO();
                    mapStat.setMapId(rs.getInt("MapId"));
                    mapStat.setWin(rs.getInt("Win"));
                    mapStat.setLose(rs.getInt("Lose"));
                    list.add(mapStat);
                }
            }
        }
        return list;
    }
    
    /**
     * Atualiza (ou insere) o registro de estatística para um mobile específico.
     * Usa "ON DUPLICATE KEY UPDATE" para ser uma operação atômica.
     */
    @Override
    public void updateMobileRecord(String userId, int mobileId, boolean didWin) {
    	
    	if(mobileId == 0xFF)
    		return;
        
        // Se o jogador venceu, a query tenta inserir (UserId, MobileId, 1, 0).
        // Se a chave (UserId, MobileId) já existir (DUPLICATE KEY),
        // ele executa o UPDATE, incrementando a coluna 'Win' em 1.
        String sql = didWin ?
            "INSERT INTO mobilerecord (UserId, MobileId, Win, Lose) VALUES (?, ?, 1, 0) " +
            "ON DUPLICATE KEY UPDATE Win = Win + 1" :
            
        // Se o jogador perdeu, a query tenta inserir (UserId, MobileId, 0, 1).
        // Se a chave já existir, ele executa o UPDATE, incrementando 'Lose' em 1.
            "INSERT INTO mobilerecord (UserId, MobileId, Win, Lose) VALUES (?, ?, 0, 1) " +
            "ON DUPLICATE KEY UPDATE Lose = Lose + 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            
            st.setString(1, userId);
            st.setInt(2, mobileId);
            st.executeUpdate();

        } catch (SQLException e) {
            // Lança uma DbException para ser tratada pelo PlayLogService (que fará o log do erro)
            throw new DbException("Erro ao fazer UPSERT em 'mobilerecord': " + e.getMessage());
        }
    }

    /**
     * Atualiza (ou insere) o registro de estatística para um mapa específico.
     * Usa "ON DUPLICATE KEY UPDATE" para ser uma operação atômica.
     */
    @Override
    public void updateStageRecord(String userId, int mapId, boolean didWin) {
    	
    	if(mapId == 0xFF)
    		return;
        
        // Lógica idêntica ao updateMobileRecord, mas para a tabela 'stagerecord'
        String sql = didWin ?
            "INSERT INTO stagerecord (UserId, MapId, Win, Lose) VALUES (?, ?, 1, 0) " +
            "ON DUPLICATE KEY UPDATE Win = Win + 1" :
            "INSERT INTO stagerecord (UserId, MapId, Win, Lose) VALUES (?, ?, 0, 1) " +
            "ON DUPLICATE KEY UPDATE Lose = Lose + 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            
            st.setString(1, userId);
            st.setInt(2, mapId);
            st.executeUpdate();

        } catch (SQLException e) {
            throw new DbException("Erro ao fazer UPSERT em 'stagerecord': " + e.getMessage());
        }
    }
}