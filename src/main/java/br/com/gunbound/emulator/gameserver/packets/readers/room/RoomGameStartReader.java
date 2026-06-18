package br.com.gunbound.emulator.gameserver.packets.readers.room;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.gameserver.room.GameRoom;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import io.netty.channel.ChannelHandlerContext;

public class RoomGameStartReader {
	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		GameRoom room = player.getCurrentRoom();

		if (player == null || player.getCurrentRoom() == null)
			return;

		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		processStartGame(ctx, payload, player, room);
	}

	private static void processStartGame(ChannelHandlerContext ctx, byte[] payload, PlayerSession player,
			GameRoom room) {

		// Apenas o dono da sala pode iniciar
		if (player.equals(player.getCurrentRoom().getRoomMaster()) && canStartGame(room)) {
			player.getCurrentRoom().startGame(payload);
		}
	}

	/**
	 * Verifica se o jogo pode ser iniciado com base nos jogadores prontos e na autoridade do mestre da sala.
	 * - O jogo só pode ser iniciado se houver pelo menos dois jogadores na sala, ou
	 * - O jogador que tentar iniciar o jogo for o mestre da sala e possuir autoridade >= 99.
	 * Além disso, todos os jogadores (exceto o mestre da sala) devem estar com status "pronto".
	 *
	 * @param room A instância da sala de jogo ({@link GameRoom}) a ser verificada.
	 * @return {@code true} se o jogo puder ser iniciado; caso contrário, {@code false}.
	 */
	private static boolean canStartGame(GameRoom room) {
	    PlayerSession master = room.getRoomMaster();
	    int totalPlayers = room.getPlayersBySlot().size();

	    // Caso o master esteja sozinho, só pode iniciar se tiver autoridade >= 99
	    if (totalPlayers <= 1) {
	        return master.getAuthority() >= 99;
	    }

	    // Se não estiver sozinho, checa se todos os outros jogadores estão prontos
	    return room.getPlayersBySlot().entrySet().stream()
	        .filter(entry -> !entry.getValue().equals(master))
	        .allMatch(entry -> room.isPlayerReady(entry.getKey()));
	}
}