package br.com.gunbound.emulator.buddy.udp;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.buddy.entities.BuddyPlayerSession;
import br.com.gunbound.emulator.buddy.entities.BuddySessionManager;
import br.com.gunbound.emulator.buddy.utils.PacketBuddyUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class BuddyUdpServiceHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	private final BuddySessionManager sessionManager = BuddySessionManager.getInstance();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
		ByteBuf content = packet.content();
		
		System.err.println("PACOTE UDP RECEBIDO (8352)");
		
		byte[] byteArray;

		if (content.isDirect()) {
		    byteArray = new byte[content.readableBytes()];
		    content.getBytes(content.readerIndex(), byteArray);
		} else {
		    byteArray = content.array();  // Pode acessar diretamente o array se for um buffer heap
		}

		System.out.println("Conteudo >>" + Utils.bytesToHex(byteArray));
		
		// Resetando o ponteiro de leitura para ler novamente.
		content.resetReaderIndex(); // Volta o ponteiro para o início

		// O ping UDP inicial deve conter o AuthDword de 4 bytes
		if (content.readableBytes() < 4) {
			return;
		}

		// 1. LER O AUTHTOKEN PARA IDENTIFICAR O JOGADOR
		byte[] sessionToken = new byte[4];
		content.readBytes(sessionToken);

		// 2. ENCONTRAR A SESSÃO TCP DO JOGADOR USANDO O TOKEN
		// (Você precisará adicionar este método de busca ao seu PlayerSessionManager)
		//BuddyPlayerSession session = sessionManager.getSessionByAuthToken(sessionToken);
		BuddyPlayerSession session = null; //comentado 17/09 pq acho que nao vamos usar UDP

		if (session != null) {
			// 3. ARMAZENAR O ENDEREÇO UDP PÚBLICO DO CLIENTE NA SESSÃO
			InetSocketAddress clientUdpAddress = packet.sender();
			// session.setUdpAddress(clientUdpAddress);
			
			ByteBuf response = Unpooled.wrappedBuffer(sessionToken);
			
			// É ESSENCIAL usar .copy() no payload para que cada envio seja independente.
			DatagramPacket datagram = new DatagramPacket(response.copy(), clientUdpAddress);

			// O UdpService faz o envio.
			BuddyUdpService.getInstance().send(datagram.retain());
			
			
			System.out
					.println("Endereço UDP registrado para " + session.getUser().getUserId() + ": " + clientUdpAddress);

			// 4. ENVIAR O PACOTE 0x101F DE VOLTA PELA CONEXÃO TCP
			// A fábrica cria o pacote...
			createUdpAckPacket(session);
			// ...e a sessão o envia pelo seu canal TCP.
			//session.sendPacket(ackPacket);
		} else {
			System.err.println("Recebido ping UDP com AuthToken desconhecido: " + Utils.bytesToHex(sessionToken));
		}
	}

	/**
	 * Cria o pacote 0x101F para confirmar o registro do endereço UDP do cliente.
	 * 
	 * @param udpAddress O endereço IP e Porta a ser enviado no payload.
	 * @return Um ByteBuf pronto para ser enviado.
	 */
	public static void createUdpAckPacket(BuddyPlayerSession session) {
		// Payload: [IP (4 bytes)] + [Porta (2 bytes)]
		ByteBuf payload = Unpooled.buffer();

		payload.writeBytes(new byte[] {(byte)0xC0,(byte)0xA8});
		payload.writeBytes(session.getSessionUniqueId());
		payload.writeBytes(new byte[] {(byte)0x00,(byte)0xFF,(byte)0x3F});
		payload.writeBytes(new byte[] {(byte)0x01,(byte)0x00});
		payload.writeBytes(Utils.resizeBytes(session.getUser().getUserId().getBytes(StandardCharsets.ISO_8859_1), 16));
		// Escreve os 4 bytes do endereço IP
		payload.writeBytes(session.getIpBytes());
		// Escreve a porta como um short (2 bytes)
		payload.writeBytes(session.getUdpPortBytes());
		// Escreve os 4 bytes do endereço IP
		payload.writeBytes(session.getIpBytes());
		// Escreve a porta como um short (2 bytes)
		payload.writeBytes(session.getUdpPortBytes());

		PacketBuddyUtils.sendPacket(session.ctx(), 0x101F, payload);
	}
}
