package br.com.gunbound.emulator.buddy;

import java.net.InetSocketAddress;

import br.com.gunbound.emulator.buddy.config.BuddyServerAttributes;
import br.com.gunbound.emulator.buddy.config.enums.BuddyOpcodes;
import br.com.gunbound.emulator.buddy.entities.BuddyPlayerSession;
import br.com.gunbound.emulator.buddy.entities.BuddySessionManager;
import br.com.gunbound.emulator.buddy.packet.readers.DeleteUserPacketReader;
import br.com.gunbound.emulator.buddy.packet.readers.GetInfoPlayerPacketReader;
import br.com.gunbound.emulator.buddy.packet.readers.LoginPacketReader;
import br.com.gunbound.emulator.buddy.packet.readers.SavePacketReader;
import br.com.gunbound.emulator.buddy.packet.readers.TunnelPacketReader;
import br.com.gunbound.emulator.buddy.packet.readers.UpdateUserStatePacketReader;
import br.com.gunbound.emulator.buddy.utils.PacketBuddyUtils;
import br.com.gunbound.emulator.utils.AuthTokenUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

// IMPORTANTE: Considerando que o BuddyServer NÃO tem sequence
// A estrutura que o Decoder entrega é [Opcode (2 bytes)] + [Payload]

public class GunBundBuddyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		BuddyPlayerSession session = new BuddyPlayerSession(ctx);
		// Associa a sessão ao canal para que outros handlers possam acessá-la
		ctx.channel().attr(BuddyServerAttributes.BUDDY_PLAYER_SESSION_KEY).set(session);
		InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
		System.out.println("BuddyServer: Cliente conectado! IP: " + remoteAddress.getAddress().getHostAddress());
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) { // ALTERADO para channelRead0

		BuddyPlayerSession session = ctx.channel().attr(BuddyServerAttributes.BUDDY_PLAYER_SESSION_KEY).get();

		// Validação mínima: o pacote deve conter pelo menos um opcode.
		if (msg.readableBytes() < 2) {
			System.err.println("BuddyServer: Pacote inválido recebido (sem opcode).");
			return;
		}

		// 1. Lê o comando (2 bytes, little-endian).
		int opcode = msg.readUnsignedShortLE();
		
		// 2. O restante dos bytes no ByteBuf é o payload real do pacote (utilizado para pegar o tamanho do payload.
		byte[] payloadData = new byte[msg.readableBytes()];
		msg.readBytes(payloadData);
		
		// 3. Reseta o leitor
		msg.resetReaderIndex();
		
		// 4. Skipa os 2 bytes do opcode pois o Reader foi resetado.
		msg.skipBytes(2);

		System.err.println("BuddyServer: Comando recebido: 0x" + Integer.toHexString(opcode) + ", Payload size=" + payloadData.length);

		switch (BuddyOpcodes.fromId(opcode)) {
		case BUDDY_AUTHTOKEN_REQ:
			handleSessionStart(session, msg);
			break;
		case BUDDY_SESSION_AUTHENTICATION:
			LoginPacketReader.read(session, msg);
			break;
		case SVC_USER_STATE:
			UpdateUserStatePacketReader.read(session, msg);
			break;
		case BUDDY_ADD_GETINFO_REQUEST:
			GetInfoPlayerPacketReader.read(session, msg);
			break;
		case BUDDY_REMOVE_REQUEST:
			DeleteUserPacketReader.read(session, msg);
			break;	
		case BUDDY_TUNNEL_PACKET:
			TunnelPacketReader.read(session, msg);
			break;
		case BUDDY_SAVE_PACKET:
			SavePacketReader.read(session, msg);
			break;
		default:
			System.err.println("Opcode de jogo desconhecido no BuddyServer: 0x" + Integer.toHexString(opcode));
			// session.ctx().close();
			break;
		}
	}

	private void handleSessionStart(BuddyPlayerSession session, ByteBuf payload) {
		System.out.println("Recebido pedido de Token para: " + session.getSessionId());

		// Gera o ByteBuf do token de 4 bytes usando a classe utilitária otimizada.
		ByteBuf authTokenBuf = AuthTokenUtils.generateAuthToken(session.ctx().alloc(), Utils.randomIntNumber());
		// Converte o ByteBuf para um array de bytes para armazenar na sessão para o
		// payload do pacote.
		byte[] authToken = new byte[authTokenBuf.readableBytes()];
		authTokenBuf.readBytes(authToken);
		authTokenBuf.release(); // Libere o ByteBuf temporário!
		// Armazena o token de 4 bytes na sessão do cliente.
		session.setAuthToken(authToken);

		// TODO: Ler o UserID e o Token de Sessão do payload.
		// TODO: Validar o token. Se válido, carregar dados do usuário e amigos.
		PacketBuddyUtils.sendPacket(session.ctx(), BuddyOpcodes.BUDDY_AUTHTOKEN_RESPONSE.getId(),
				Unpooled.wrappedBuffer(authToken));
	}

	/**
	 * Gera um token de 4 bytes a partir de um inteiro positivo.
	 *
	 * @param allocator     O alocador de buffers do canal.
	 * @param positiveToken O token (inteiro positivo de 4 bytes) a ser convertido.
	 * @return Um ByteBuf contendo o token de 4 bytes.
	 */
	public static ByteBuf generateAuthToken(ByteBufAllocator allocator, int positiveToken) {
		// Aloca um ByteBuf de 4 bytes.
		ByteBuf tokenBuf = allocator.buffer(4);

		// Escreve o inteiro de 4 bytes no buffer (little-endian).
		tokenBuf.writeIntLE(positiveToken);

		return tokenBuf;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// O SimpleChannelInboundHandler já libera o buffer em caso de exceção.
		cause.printStackTrace();

		BuddySessionManager.getInstance().removePlayer(ctx.channel());

		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

		System.err.println("BuddyServer: Cliente de jogo desconectado de " + ctx.channel().remoteAddress());

		BuddySessionManager.getInstance().removePlayer(ctx.channel());
		ctx.close(); // Fecha a conexão em caso de erro.
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				BuddySessionManager.getInstance().removePlayer(ctx.channel());
				ctx.close();
			} else {
				super.userEventTriggered(ctx, evt);
			}
		}
	}
}