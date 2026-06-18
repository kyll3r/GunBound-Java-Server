package br.com.gunbound.emulator.gameserver.packets.readers.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.gameserver.packets.writers.RoomWriter;
import br.com.gunbound.emulator.gameserver.room.GameRoom;
import br.com.gunbound.emulator.gameserver.room.RoomManager;
import br.com.gunbound.emulator.model.entities.game.GameBuddyList;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.model.entities.game.PlayerSessionManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomBuddyListReader {

	private static final int OPCODE_REQUEST = 0x2101;
	private static final int OPCODE_RESPONSE = 0x2103;
	// cliente exibe 6 salas por página.
	private static final int ROOMS_PER_PAGE = 6;

	private static final PlayerSessionManager ps = PlayerSessionManager.getInstance();
	private static final RoomManager rmngr = RoomManager.getInstance();

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_SORTED__BUDDY_LIST (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {
			// 1. Decodificar o filtro e o índice inicial da página
			int filterMode = request.readUnsignedByte();
			int startIndex = 0; // O índice inicial da sala a ser exibida. Padrão é 0.
			if (request.isReadable()) {
				// O cliente envia o índice da primeira sala da página (0, 6, 12, etc.)
				startIndex = request.readUnsignedByte();
			}

			String filterName = filterMode == 1 ? "ALL" : (filterMode == 2 ? "WAITING" : "UNKNOWN");
			System.out.println("Filtro de sala: " + filterName + ", Índice Inicial Solicitado: " + startIndex);

			// 2. Criar uma lista para inserir as salas
			List<GameRoom> filteredRooms = new ArrayList<GameRoom>(); // Usar List para permitir a criação de sub-listas

			//pegar o room da galera sem repetir sala que tem 2 amigos ou mais.
			Collection<GameRoom> buddiesRoom = isBuddieInAnyRoom(player.getBuddyLists());

			//adiciona toda a galera na lista
			filteredRooms.addAll(buddiesRoom);
			
			// 3. Calcular a paginação com base no índice inicial
			int totalRooms = filteredRooms.size();
			int endIndex = Math.min(startIndex + ROOMS_PER_PAGE, totalRooms);

			List<GameRoom> roomsForPage;
			if (startIndex >= totalRooms) {
				roomsForPage = Collections.emptyList(); // A página solicitada está fora dos limites
			} else {
				roomsForPage = filteredRooms.subList(startIndex, endIndex);
			}

			// A chamada passa a lista da página atual
			ByteBuf responsePayload = RoomWriter.writeRoomList(roomsForPage);

			ByteBuf responsePacket = PacketUtils.generatePacket(player, OPCODE_RESPONSE, responsePayload, true);

			ctx.writeAndFlush(responsePacket);

		} catch (Exception e) {
			System.err.println("Erro ao processar a lista de salas");
			e.printStackTrace();
		} finally {
			request.release();
		}
	}

	// Menos eficiente
	private static Collection<GameRoom> isBuddieInAnyRoom(List<GameBuddyList> buddies) {
		//System.out.println("Tamanho da lista de amigo? >>>" + buddies.size());
		
		// Sessoes ativas dos amigos
		List<PlayerSession> activesPS = new ArrayList<PlayerSession>();
		// salas que os amigos estao
		Collection<GameRoom> buddiesRoom = new HashSet<GameRoom>();

		for (GameBuddyList list : buddies) {

			// caso o amigo esteja online ele vai adicionar a lista
			if (ps.isPlayerOnlineByUserId(list.getBuddyId())) {
				activesPS.add(ps.getSessionPlayerByUserId(list.getBuddyId()));
			}
		}
		
		//System.out.println("Tamanho da lista online? >>>" + activesPS.size());

		for (PlayerSession psSession : activesPS) {
			// Itera por todas as salas ativas
			
				// O método containsValue() é eficiente em ConcurrentHashMap
				if (rmngr.isPlayerInAnyRoom(psSession)) {
					System.out.println(psSession.getNickName() + " Esta na sala " + psSession.getCurrentRoom() );
					buddiesRoom.add(psSession.getCurrentRoom());
				}
			
		}
		//System.out.println("tem room? >>>" + buddiesRoom.size());
		return buddiesRoom; // Não encontrou o jogador em nenhuma sala
	}

}
