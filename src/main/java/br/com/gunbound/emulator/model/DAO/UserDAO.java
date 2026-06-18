package br.com.gunbound.emulator.model.DAO;

import java.sql.Timestamp;
import java.util.Optional;

import br.com.gunbound.emulator.model.entities.DTO.UserDTO;

public interface UserDAO {
	public Optional<UserDTO> getUserByUserId(String userIdQuery);
	public UserDTO getUserByNickname(String NickNameIdQuery);
	
	//operacoes com Gold
	void updateMinusGold(String playerId, int value);
	void updateAddGold(String playerId, int value);
	
	//operacoes com Cash
	void updateMinusCash(String playerId, int value);
	void updateAddCash(String playerId, int value);
	
	//responsavel por atualizar a autoridade do player
	public void updateAuthority(String playerId, int value);
	
	//responsavel pelo banimento de um player.
	public void banPlayer(String playerId, int authy, Timestamp expire);
	//insere o log de ban
	public int insertBanLog(String userBanned, Timestamp duration, String reason, String adminId, String adminNick);
	
	//atualiza cores
	public void updateColors(String playerId, int colorId, Timestamp expire, boolean isBalao) ;
	
	/**
	 * Atualiza os status de um jogador (GP, Gold, Evento) após o término de uma partida.
	 * Esta operação é atômica, somando os novos valores aos existentes.
	 * @param userId O ID do usuário a ser atualizado.
	 * @param gpGanhos A quantidade de GP a ser adicionada.
	 * @param goldGanhos A quantidade de Gold a ser adicionada.
	 * @param eventPointsGanhos A quantidade de pontos de evento a ser adicionada.
	 * @param hits A quantidade de hits acertados na partida
	 * @param damage A quantidade de pontos de dano aferida pelo player na partida.
	 */
	void updateUserStatsAfterGame(String userId, int gpGanhos, int goldGanhos, int eventPointsGanhos, int hits, int damage);
}
