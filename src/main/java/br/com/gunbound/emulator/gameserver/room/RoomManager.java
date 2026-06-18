package br.com.gunbound.emulator.gameserver.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Gerencia todas as salas de jogo (GameRooms) ativas no servidor. utiliza
 * PriorityBlockingQueue para gerenciar os IDs (pega o menor) de forma
 * thread-safe.
 */
public final class RoomManager {

	private static final RoomManager INSTANCE = new RoomManager();
	private static final int MAX_ROOMS = 1000; // Define o número máximo de salas

	// Mapeia o ID da sala para a sua instância.
	private final Map<Integer, GameRoom> activeRooms = new ConcurrentHashMap<>();

	// Fila thread-safe que mantém os IDs disponíveis, sempre oferecendo o menor
	// primeiro.
	private final PriorityBlockingQueue<Integer> availableRoomIds;

	/**
	 * Construtor privado para reforçar o padrão Singleton. Inicializa a fila com
	 * todos os IDs de sala possíveis.
	 */
	private RoomManager() {
		availableRoomIds = new PriorityBlockingQueue<>(MAX_ROOMS);
		for (int i = 0; i < MAX_ROOMS; i++) {
			availableRoomIds.add(i);
		}
	}

	public static RoomManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Cria uma nova sala de jogo, utilizando o menor ID disponível da fila.
	 * 
	 * @return A instância da GameRoom recém-criada, ou null se não houver IDs
	 *         disponíveis.
	 */
	public GameRoom createRoom(PlayerSession creator, String title, String password, int capacity) {
		// Pega o menor ID disponível da fila de forma atômica e segura.
		Integer roomId = availableRoomIds.poll();

		if (roomId == null) {
			System.err.println("ROOM MANAGER: Não há IDs de sala disponíveis. Limite máximo atingido.");
			return null; // Nenhuma sala pôde ser criada
		}

		GameRoom room = new GameRoom(roomId, title, creator, capacity);

		if (password != null && !password.isEmpty()) {
			room.setPassword(password);
		}

		activeRooms.put(roomId, room);
		System.out.println("ROOM MANAGER: Sala criada por " + creator.getNickName() + " com o ID " + roomId);
		return room;
	}

	/**
	 * Remove uma sala do gerenciador e devolve seu ID para a fila.
	 * 
	 * @param roomId O ID da sala a ser removida.
	 */
	// public void removeRoom(int roomId) {
	// GameRoom room = activeRooms.remove(roomId);
	// if (room != null) {
	// Devolve o ID para a fila de forma segura para que possa ser reutilizado.
	// availableRoomIds.add(roomId);
	// System.out.println("ROOM MANAGER: Sala " + roomId + " removida e ID
	// liberado.");
	// }
	// }

	/**
	 * Remove uma sala do gerenciador e devolve seu ID para a fila de IDs
	 * disponíveis. A operação é feita de forma atômica usando computeIfPresent para
	 * garantir que a sala só será removida se ainda existir.
	 *
	 * @param roomId O ID da sala a ser removida.
	 */
	public void removeRoom(int roomId) {
		activeRooms.computeIfPresent(roomId, (id, room) -> {
			availableRoomIds.add(id); // Libera o ID para reutilização
			System.out.println("ROOM MANAGER: Sala " + id + " removida e ID liberado.");
			return null; // Retornando null, a entrada será removida do mapa
		});
	}

	/**
	 * Lida com a desconexão de um jogador.
	 * 
	 * @param player O jogador que desconectou.
	 */
	public void handlePlayerLeave(PlayerSession player) {
		if (player == null)
			return;

		GameRoom room = player.getCurrentRoom();

		if (room != null) {
			boolean wasHost = room.getRoomMaster().equals(player);

			// se sala está em jogo e player sai precisamos desconta a vida do camarada
			if (room.isGameStarted()) {
				player.setIsAlive(0);
			}
			// 1. Remove o jogador e descobre qual slot ele ocupava.
			int removedPlayerSlot = room.removePlayer(player);

			if (room.getPlayerCount() == 0) {
				// 2. Se a sala ficou vazia, remove-a.
				removeRoom(room.getRoomId());
			} else if (removedPlayerSlot != -1) {
				// 3. Se a sala não ficou vazia:

				// Notifica a todos sobre o slot que foi liberado.
				// room.submitAction(() -> room.notifyPlayerLeft(removedPlayerSlot));
				room.notifyPlayerLeft(removedPlayerSlot, wasHost, () -> {

					room.getRoomEventLoop().schedule(() -> {

						// Este código será executado sem bloquear e de forma segura.
						System.out.println("Executando validações de saída após atraso...");
						room.processPlayerExitValidations();

					}, 1000, TimeUnit.MILLISECONDS);

				});

				// Se quem saiu era o host, notifica sobre a migração.
				// if (wasHost) {
				// System.out.println("[DEBUG]: Entrou no if de washost" );
				// room.notifyHostMigration();
				// room.submitAction(() -> room.notifyHostMigration());
				// }

			}
		}
	}

	private static final Integer OPCODE_KICK_PLAYER = 0x3FFF;
	public boolean closeRoom(GameRoom room) {
		if (room == null) {
			return false;
		}

		// snapshot para evitar concorrência
		List<PlayerSession> recipients = new ArrayList<>(room.getPlayersBySlot().values());
		for (PlayerSession playerInRoom : recipients) {
			ByteBuf confirmationPacket = PacketUtils.generatePacket(playerInRoom, OPCODE_KICK_PLAYER,
					Unpooled.EMPTY_BUFFER, false);
			playerInRoom.getPlayerCtxChannel().eventLoop().execute(() -> {
				playerInRoom.getPlayerCtx().writeAndFlush(confirmationPacket);
		});
			
		}
		// add 04-01-26 - força fechar room bugado
		removeRoom(room.getRoomId());
		return true;
	}

	// public boolean isPlayerInAnyRoom(PlayerSession player) {
	// return player != null && player.getCurrentRoom() != null;
	// }

	// Menos eficiente
	public boolean isPlayerInAnyRoom(PlayerSession player) {
		if (player == null) {
			return false;
		}
		// Itera por todas as salas ativas
		for (GameRoom room : activeRooms.values()) {
			// O método containsValue() é eficiente em ConcurrentHashMap
			if (room.getPlayersBySlot().containsValue(player)) {
				return true; // Encontrou o jogador
			}
		}
		return false; // Não encontrou o jogador em nenhuma sala
	}

	// --- Outros métodos ---

	public GameRoom getRoomById(int roomId) {
		return activeRooms.get(roomId);
	}

	public Collection<GameRoom> getAllRooms() {
		return Collections.unmodifiableCollection(activeRooms.values());
	}
}