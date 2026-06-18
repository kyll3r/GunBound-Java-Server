package br.com.gunbound.emulator.services.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.DAO.GameBuddyDAO;
import br.com.gunbound.emulator.model.DAO.StatsDAO;
import br.com.gunbound.emulator.model.DAO.UserDAO;
import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.model.entities.DTO.stats.PlayerStatsDTO;
import br.com.gunbound.emulator.model.entities.game.GameBuddyList;
import br.com.gunbound.emulator.model.entities.game.PlayerGameResult;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.UserService;
import br.com.gunbound.emulator.services.excepion.UserNotFoundException;

public class UserServiceImpl implements UserService {

	private final UserDAO userDAO;
	private final StatsDAO statsDAO;
	private final GameBuddyDAO gameBuddyDAO;

	public UserServiceImpl() {
		this.userDAO = DAOFactory.CreateUserDao();
		this.statsDAO = DAOFactory.CreateStatsDao(); 
		this.gameBuddyDAO = DAOFactory.CreateGameBuddyDAO(); 
	}
	
	@Override
	public UserDTO getUserById(String userId) {

		// 1. Busca o usuário principal
		Optional<UserDTO> userOpt = userDAO.getUserByUserId(userId);
		
		if (userOpt.isEmpty()) {
			throw new UserNotFoundException("Usuário não encontrado.");
		}
		
		UserDTO userDTO = userOpt.get();

		// 2. Busca as estatísticas do usuário
		try {
			PlayerStatsDTO stats = statsDAO.getPlayerStatsByUserId(userId);
			userDTO.setStats(stats); // 3. Anexa as estatísticas ao DTO do usuário
		} catch (Exception e) {
			// Em caso de falha ao carregar estatísticas, loga o erro mas continua
			// o processo de login. O jogador ficará com estatísticas zeradas.
			System.err.println("Erro ao carregar estatísticas para o usuário " + userId + ": " + e.getMessage());
			// Opcional: userDTO.setStats(new PlayerStatsDTO()); // Garante que não seja nulo
		}
		
		
		// 3. Busca os amigos do usuario
		try {
			List<GameBuddyList> buddyLists = this.getBuddiesByUser(userId);
			userDTO.setBuddyLists(buddyLists);// Anexa o samigos ao DTO do usuário
		} catch (Exception e) {
			// Em caso de falha ao carregar estatísticas, loga o erro mas continua
			// o processo de login.
			System.err.println("Erro ao carregar amigos do usuário " + userId + ": " + e.getMessage());	
		}
		
		return userDTO;
	}

	@Override
	public void applyGameResults(PlayerSession player, PlayerGameResult result) {
		// Executa a operação de salvar no banco de dados em uma thread separada
		// para não bloquear a thread principal do jogo ou o EventLoop do Netty.
		CompletableFuture.runAsync(() -> {
			try {
				// A query no DAO já é atômica (GP = GP + ?, etc.)
				userDAO.updateUserStatsAfterGame(player.getUserNameId(), result.getNormalGp(), result.getNormalGold(),
						result.getEventPoint(),result.getHits(),result.getDamage());
				System.out.println("Dados de " + player.getNickName() + " salvos no banco de dados.");

				// Atualizar a sessão do jogador em memória com os novos valores.
				player.setGold(player.getGold() + result.getNormalGold());
				player.setTotalScore(player.getTotalScore() + result.getNormalGp());
				
				player.setEventScore0(player.getEventScore0() + result.getEventPoint());
				player.setAccumShot(player.getAccumShot() + result.getHits());
				player.setAccumDamage(player.getAccumDamage() + result.getDamage());

			} catch (Exception e) {
				System.err.println(
						"### ERRO CRÍTICO: Falha ao salvar os dados do jogador " + player.getNickName() + " no banco:");
				e.printStackTrace();
			}
		});
	}
	
	@Override
	public void banAnyPlayer(String userId, int authy, Timestamp time, String reason, PlayerSession admin) {
		
		userDAO.banPlayer(userId, authy, time);
		userDAO.insertBanLog(userId, time, reason, admin.getUserNameId(), admin.getNickName());
	}

	@Override
	public UserDTO findUserByNickName(String nickName) {
		// 1. Busca o usuário no banco de dados.
		UserDTO user = userDAO.getUserByNickname(nickName);
		if (user == null) {
			return null;
		}
		return user;
	}
	
	@Override
	public List<GameBuddyList> getBuddiesByUser(String userId) {
		return gameBuddyDAO.findBuddiesByUserId(userId);
	}
}
