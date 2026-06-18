package br.com.gunbound.emulator.model.DAO;

import br.com.gunbound.emulator.model.entities.DTO.PlayLogDTO;

public interface PlayLogDAO {
    void save(PlayLogDTO playlog);
}
