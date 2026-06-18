package br.com.gunbound.emulator.model.entities.DTO.stats.mock;

import java.util.ArrayList;
import java.util.List;

import br.com.gunbound.emulator.model.entities.DTO.stats.MapStatsDTO;
import br.com.gunbound.emulator.model.entities.DTO.stats.MobileStatsDTO;
import br.com.gunbound.emulator.model.entities.DTO.stats.PlayerStatsDTO;

public class StatsJDBC implements StatsDAO {
   

    @Override
    public PlayerStatsDTO findStatsByUserId(String userId) {
        // --- IMPORTANTE ---
        // Substitua esta lógica mockada pela sua consulta real ao banco de dados.
        // Você precisará fazer SELECTs nas suas tabelas de histórico de jogos,
        // agrupando por mapa e por mobile para obter as vitórias e derrotas.
        
        // Dados de exemplo para os mapas
        List<MapStatsDTO> mapStats = new ArrayList<>();
        mapStats.add(new MapStatsDTO(1, 10, 5));
        mapStats.add(new MapStatsDTO(2, 8, 3));
        mapStats.add(new MapStatsDTO(3, 15, 10));
        mapStats.add(new MapStatsDTO(4, 5, 5));
        mapStats.add(new MapStatsDTO(5, 20, 15));
        mapStats.add(new MapStatsDTO(6, 2, 1));
        mapStats.add(new MapStatsDTO(7, 7, 7));
        mapStats.add(new MapStatsDTO(8, 12, 8));
        mapStats.add(new MapStatsDTO(9, 3, 4));
        mapStats.add(new MapStatsDTO(10, 9, 6));
        mapStats.add(new MapStatsDTO(11, 25, 10));

        // Dados de exemplo para os mobiles
        List<MobileStatsDTO> mobileStats = new ArrayList<>();
        mobileStats.add(new MobileStatsDTO(0x00, 5, 5));   // Armor
        mobileStats.add(new MobileStatsDTO(0x01, 3, 4));   // Mage
        mobileStats.add(new MobileStatsDTO(0x02, 5, 6));   // Nak
        mobileStats.add(new MobileStatsDTO(0x03, 2, 3));   // Trico
        mobileStats.add(new MobileStatsDTO(0x04, 6, 7));   // Big Foot
        mobileStats.add(new MobileStatsDTO(0x05, 1, 2));   // Boomer
        mobileStats.add(new MobileStatsDTO(0x06, 4, 5));   // Raon
        mobileStats.add(new MobileStatsDTO(0x07, 3, 4));   // Lightning
        mobileStats.add(new MobileStatsDTO(0x08, 6, 7));   // JD
        mobileStats.add(new MobileStatsDTO(0x09, 2, 1));   // A.Sate
        mobileStats.add(new MobileStatsDTO(0x0A, 5, 6));  // Ice
        mobileStats.add(new MobileStatsDTO(0x0B, 4, 3));  // Turtle
        mobileStats.add(new MobileStatsDTO(0x0C, 6, 2));  // Grub
        mobileStats.add(new MobileStatsDTO(0x0D, 1, 1));  // Aduka
        mobileStats.add(new MobileStatsDTO(0x11, 16, 0)); // Dragon
        mobileStats.add(new MobileStatsDTO(0x12, 1, 2));  // Knight


        return new PlayerStatsDTO(mapStats, mobileStats);
    }
}