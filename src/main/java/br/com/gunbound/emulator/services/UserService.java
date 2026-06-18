package br.com.gunbound.emulator.services;

import java.sql.Timestamp;
import java.util.List;

import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.model.entities.game.GameBuddyList;
import br.com.gunbound.emulator.model.entities.game.PlayerGameResult;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;

public interface UserService {
	
	/**
	 * Busca um player de acordo com o seu Id
	 * @param userId Os Dados do jogador.
	 */
	UserDTO getUserById(String userId);
	
	
	/**
	 * Atualiza as estatísticas de um jogador (GP, Gold, Pontos de Eventos) no banco
	 * de dados após o término de uma partida. A implementação deste método deve ser
	 * assíncrona para não bloquear a thread principal do jogo.
	 *
	 * @param player A sessão do jogador cujas estatísticas serão atualizadas.
	 * @param result O objeto com os resultados da partida (GP, Gold, etc.).
	 */
	void applyGameResults(PlayerSession player, PlayerGameResult result);
	
	
	/**
	 * Busca um player pelo nickname (muito util para funcao add/remove na buddy)
	 *
	 * @param nickName o Nick do player alvo da busca.
	 */
	
	UserDTO findUserByNickName(String nickName);
	
	//pegar os amigos do player
	public List<GameBuddyList> getBuddiesByUser(String userId);
	
	//banir player
	public void banAnyPlayer(String userId, int authy, Timestamp time, String reason, PlayerSession admin);
}
