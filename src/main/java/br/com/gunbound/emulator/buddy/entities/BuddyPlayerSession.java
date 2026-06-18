package br.com.gunbound.emulator.buddy.entities;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import br.com.gunbound.emulator.buddy.config.enums.BuddySessionState;
import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class BuddyPlayerSession {

	// --- Atributos de Conexão e Identificação ---
	private final ChannelHandlerContext ctx; // A conexão de rede Netty deste jogador. ESSENCIAL.
	private final String sessionId; // Um ID único para esta sessão (pode ser o ID do canal).
	private final byte[] sessionUniqueId; // Um ID único para esta sessão (pode ser o ID do canal).

	private final String ipAddress; // IP do jogador.
	private final byte[] ipBytes; // bytes do IP do jogador.

	private int udpPortListen; // Porta que o jogador esta escutando
	private byte[] udpPortBytes; // Porta que o jogador esta escutando

	// --- Atributos de Estado e Segurança ---
	private BuddySessionState state; // O estado atual da sessão (controla o que o jogador pode fazer).
	private BuddyUserDTO user; // Dados do usuário (preenchido após o login).
	private List<BuddyList> friendList = new ArrayList<BuddyList>();
	private byte[] authToken; // authtoken dword gerado

	// --- Atributos de Localização e Atividade ---
	private int currentLobbyChannelId; // Em qual dos canais principais o jogador está.
	private int currentRoomId; // ID da sala de jogo em que o jogador está (0 se estiver no lobby).
	private long lastActivityTime; // Timestamp da última atividade (para detectar inatividade).

	/**
	 * Construtor: chamado quando uma nova conexão é estabelecida.
	 * 
	 * @param channel O canal Netty da nova conexão.
	 */
	public BuddyPlayerSession(ChannelHandlerContext ctx) {
		this.ctx = ctx;
		this.sessionId = ctx.channel().id().asShortText();
		this.sessionUniqueId = ByteBuffer.allocate(4).putInt(ctx.channel().id().hashCode()).array();
		this.ipAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
		this.ipBytes = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getAddress();
		this.state = BuddySessionState.CONNECTED; // Estado inicial
		this.lastActivityTime = System.currentTimeMillis();
	}

	// --- Métodos de Ação ---

	/**
	 * Envia um pacote de dados para este jogador.
	 * 
	 * @param packet O ByteBuf a ser enviado.
	 */
	public void sendPacket(ByteBuf packet) {
		if (ctx.channel() != null && ctx.channel().isActive() && ctx.channel().isWritable()) {
			ctx.channel().writeAndFlush(packet);
		}
	}

	/**
	 * Atualiza o timestamp da última atividade para prevenir desconexão por
	 * inatividade.
	 */
	public void updateActivity() {
		this.lastActivityTime = System.currentTimeMillis();
	}

	/**
	 * Verifica se o jogador já está autenticado.
	 * 
	 * @return true se o jogador já passou pela etapa de login.
	 */
	public boolean isAuthenticated() {
		return this.state.ordinal() >= BuddySessionState.AUTHENTICATED.ordinal();
	}

	/**
	 * Desconecta o jogador do servidor.
	 */
	public void close() {
		if (ctx.channel() != null && ctx.channel().isActive()) {
			// Opcional: Enviar um pacote de "você foi desconectado" antes de fechar.
			ctx.channel().close();
		}
	}

	// --- Getters e Setters ---
	// É crucial ter getters e setters para que os Handlers possam ler e
	// modificar o estado da sessão.

	public ChannelHandlerContext ctx() {
		return ctx;
	}

	public Channel getChannel() {
		return ctx.channel();
	}

	public String getSessionId() {
		return sessionId;
	}

	public byte[] getSessionUniqueId() {
		return sessionUniqueId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public byte[] getIpBytes() {
		return ipBytes;
	}

	public int getUdpPortListen() {
		return udpPortListen;
	}

	public void setUdpPortListen(int udpPortListen) {
		this.udpPortListen = udpPortListen;
	}

	public byte[] getUdpPortBytes() {
		return udpPortBytes;
	}

	public void setUdpPortBytes(byte[] udpPortBytes) {
		this.udpPortBytes = udpPortBytes;
	}

	public BuddySessionState getState() {
		return state;
	}

	public void setState(BuddySessionState state) {
		System.out.println("Sessão " + sessionId + " mudou de estado para: " + state);
		this.state = state;
	}

	public BuddyUserDTO getUser() {
		return user;
	}

	public void setUser(BuddyUserDTO user) {
		this.user = user;
	}

	public byte[] getAuthToken() {
		return authToken;
	}

	public void setAuthToken(byte[] authToken) {
		this.authToken = authToken;
	}

	public int getCurrentLobbyChannelId() {
		return currentLobbyChannelId;
	}

	public void setCurrentLobbyChannelId(int currentLobbyChannelId) {
		this.currentLobbyChannelId = currentLobbyChannelId;
	}

	public int getCurrentRoomId() {
		return currentRoomId;
	}

	public void setCurrentRoomId(int currentRoomId) {
		this.currentRoomId = currentRoomId;
	}

	public long getLastActivityTime() {
		return lastActivityTime;
	}

	public List<BuddyList> getFriendList() {
		return friendList;
	}

	public void setFriendList(List<BuddyList> friendList) {
		this.friendList = friendList;
	}
}