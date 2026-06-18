package br.com.gunbound.emulator.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import br.com.gunbound.emulator.gameserver.room.GameRoom;
import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.DAO.PlayLogDAO;
import br.com.gunbound.emulator.model.DAO.StatsDAO;
import br.com.gunbound.emulator.model.entities.DTO.PlayLogDTO;
import br.com.gunbound.emulator.model.entities.DTO.PlayLogDTO.PlayerLog;
import br.com.gunbound.emulator.model.entities.game.PlayerGameResult;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.PlayLogService;

public class PlayLogServiceImpl implements PlayLogService {

	private final PlayLogDAO playlogDAO;
	private final StatsDAO statsDAO;

	public PlayLogServiceImpl() {
		// A injeção de dependência poderia ser usada aqui, mas a Factory também é uma
		// boa abordagem.
		this.playlogDAO = DAOFactory.CreatePlayLogDao();
		this.statsDAO = DAOFactory.CreateStatsDao();
	}

	@Override
	public void recordMatchResults(GameRoom room) {
		// Executa toda a lógica de coleta e salvamento em uma thread separada.
		CompletableFuture.runAsync(() -> {
			try {
				// 1. Coleta os dados da partida e monta o DTO.
				PlayLogDTO log = buildPlaylogFromRoom(room);

				// 2. Chama o DAO para salvar no banco de dados.
				playlogDAO.save(log);
				
				//Salvar agora os stats
				saveStatsOnDb(log.getPlayers());
				
				

			} catch (Exception e) {
				System.err.println("### ERRO CRÍTICO: Falha ao salvar o playlog da sala " + room.getRoomId()
						+ " de forma assíncrona. ###");
				e.printStackTrace();
			}
		});
	}

	/**
	 * Método auxiliar para construir o DTO a partir do estado final da GameRoom.
	 * 
	 * @param room A sala de jogo finalizada.
	 * @return um PlayLogDTO preenchido e pronto para ser salvo.
	 */
	private PlayLogDTO buildPlaylogFromRoom(GameRoom room) {
		PlayLogDTO log = new PlayLogDTO();
		log.setServerIp(0); // TODO: Obter de um arquivo de configuração
		log.setGameRoomId(room.getRoomId() + 1); // Precisa salvar com +1 pois o indice começa em 0
		log.setGameRoomTitle(room.getTitle());
		log.setStartTime(room.getStartTime());
		log.setEndTime(LocalDateTime.now());
		log.setGameOption(room.getGameSettings());
		log.setWinTeamOrPlayer(room.getWinnerTeam());

		// Itera sobre os jogadores para preencher os dados de cada slot
		for (Map.Entry<Integer, PlayerSession> entry : room.getPlayersBySlot().entrySet()) {
			int slot = entry.getKey();
			PlayerSession player = entry.getValue();
			PlayerGameResult result = room.getResultGameBySlot().get(slot);

			if (player == null || result == null)
				continue;

			PlayLogDTO.PlayerLog playerLog = new PlayLogDTO.PlayerLog(slot, player.getUserNameId(),
					player.getRoomTeam(), result.getNormalGp(), // Mapeado para ScoreDelta
					result.getNormalGold() // Mapeado para MoneyDelta
			);

			// *************REGIAO DO STATS*************
			playerLog.setMapId(room.getInGameMapId());

			if (player.getInGameTankPrimary() != 0xFF) {
				playerLog.setTankPrimary(player.getInGameTankPrimary());
			}

			if (player.getInGameTankSecondary() != 0xFF) {
				playerLog.setTankSecondary(player.getInGameTankSecondary());
			}

			if (player.getRoomTeam() == room.getWinnerTeam()) {
				playerLog.setWin(true);
			} else {
				playerLog.setWin(false);
			}

			// *************FIM REGIAO DO STATS*************

			// Adiciona a hora da morte, se o jogador morreu
			if (player.getIsAlive() == 0) {
				playerLog.setDeadTime(player.getDeadTime());
				playerLog.setDeadCause(player.getDeadCause()); // TODO: Implementar causa da morte se aplicável
			}

			log.getPlayers().add(playerLog);
		}
		return log;
	}
	
	//Salvar os stats apos o fim do jogo
	private void saveStatsOnDb(List<PlayerLog> playerLog) {
		
		for(PlayerLog player : playerLog) {
			
			//Salva stats do mapa
			statsDAO.updateStageRecord(player.getUserId(), player.getMapId(), player.isWin());
			
			//Salva stats dos mobile
			statsDAO.updateMobileRecord(player.getUserId(), player.getTankPrimary(), player.isWin());
			statsDAO.updateMobileRecord(player.getUserId(), player.getTankSecondary(), player.isWin());
		}
		
	}
}