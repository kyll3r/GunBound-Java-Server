package br.com.gunbound.emulator.buddy.entities;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.com.gunbound.emulator.buddy.packet.writers.PlayerNotifiers;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

public final class BuddySessionManager {

	private static final BuddySessionManager INSTANCE = new BuddySessionManager();

	// Índice por ChannelId para acessos rápidos por conexão
	private final Map<ChannelId, BuddyPlayerSession> byNettyChannelId = new ConcurrentHashMap<>();

	// Índice por id (lowercase) para buscas diretas
	private final Map<String, BuddyPlayerSession> byUserId = new ConcurrentHashMap<>();

	// Mapeia o AuthToken (como String para ser uma chave de mapa confiável) para o
	// ID do usuário
	//private final Map<String, BuddyPlayerSession> bySessionId = new ConcurrentHashMap<>();

	private BuddySessionManager() {
	}

	public static BuddySessionManager getInstance() {
		return INSTANCE;
	}

	public void addPlayer(BuddyPlayerSession session) {
		Channel channel = session.getChannel(); // nome do campo ctx mantido
		String id = session.getUser().getUserId();

		byNettyChannelId.put(channel.id(), session);
		byUserId.put(id, session);

		String SessionTokenKey = Utils.bytesToHex(session.getSessionUniqueId());
		//bySessionId.put(SessionTokenKey, session);

		System.out.println("Buddy Session Manager: Jogador " + session.getUser().getUserId()
				+ " conectado. Total de jogadores online: " + byNettyChannelId.size());
	}

	public void removePlayer(Channel channel) {
		BuddyPlayerSession removed = byNettyChannelId.remove(channel.id());

		if (removed != null) {
			byUserId.remove(removed.getUser().getUserId().toLowerCase());
			PlayerNotifiers.playerBecameOffline(removed); // notificar amigos que ficou offline
			System.out.println(
					"Buddy Session Manager: Jogador removido. Total de jogadores online: " + byNettyChannelId.size());
		}
	}

	public BuddyPlayerSession getPlayer(Channel channel) {
		return byNettyChannelId.get(channel.id());
	}

	public BuddyPlayerSession getSessionPlayerById(String id) {
		return byUserId.get(id.toLowerCase());
	}

	public Collection<BuddyPlayerSession> getAllPlayers() {
		return byNettyChannelId.values();
	}

	public int getActivePlayerCount() {
		return byNettyChannelId.size();
	}

	public boolean isPlayerOnline(String id) {
		return byUserId.containsKey(id.toLowerCase());
	}

	// O novo método de busca que o Handler UDP usará
	/*public BuddyPlayerSession getSessionByAuthToken(byte[] token) {
		String tokenKey = Utils.bytesToHex(token);
		BuddyPlayerSession user = bySessionId.get(tokenKey);
		if (user != null) {
			return user; // Reutiliza seu método de busca por ID
		}
		return null;
	}*/
}
