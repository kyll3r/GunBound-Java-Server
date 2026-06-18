package br.com.gunbound.emulator.services;

import br.com.gunbound.emulator.gameserver.room.GameRoom;

/**
 * Define o contrato para serviços que gerenciam a auditoria e o registro de partidas (playlogs).
 */
public interface PlayLogService {

    /**
     * Coleta os dados de uma partida finalizada e os persiste no banco de dados.
     * A implementação deste método deve ser assíncrona para não bloquear a thread do jogo.
     *
     * @param room A sala de jogo que acabou de terminar a partida, contendo todos os dados e resultados.
     */
    void recordMatchResults(GameRoom room);

}