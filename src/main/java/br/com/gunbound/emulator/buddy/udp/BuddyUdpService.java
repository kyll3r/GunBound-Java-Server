package br.com.gunbound.emulator.buddy.udp;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;


public class BuddyUdpService {
	private static final BuddyUdpService INSTANCE = new BuddyUdpService();
	private Channel channel;
	private EventLoopGroup group;

	private BuddyUdpService() {
	}

	public static BuddyUdpService getInstance() {
		return INSTANCE;
	}

	/**
	 * Inicia o serviço UDP. Deve ser chamado no startup do seu servidor.
	 */
	// Dentro da sua classe UdpService
    public synchronized void start() {
        if (group != null && !group.isTerminated()) {
            throw new IllegalStateException("Serviço UDP já está em execução");
        }

        ThreadFactory threadFactory = Executors.defaultThreadFactory(); // Usando Factory do Java
        group = new MultiThreadIoEventLoopGroup(
            Runtime.getRuntime().availableProcessors(),
            threadFactory,
            NioIoHandler.newFactory()
        );
        
	    try {
	        Bootstrap b = new Bootstrap(); 
	        
	        b.group(group)
	         .channel(NioDatagramChannel.class) // O canal UDP padrão do Netty (NIO)
	         .option(ChannelOption.SO_BROADCAST, false)
	         .handler(new ChannelInitializer<NioDatagramChannel>() {
                 @Override
                 protected void initChannel(NioDatagramChannel ch) throws Exception {
                     ch.pipeline().addLast(new BuddyUdpServiceHandler());
                 }
             });
	        

	        channel = b.bind(8352).sync().channel();
	        System.out.println("Serviço de envio UDP (NIO) iniciado e pronto.");

	    } catch (InterruptedException e) {
	        System.err.println("Falha ao iniciar o serviço UDP.");
	        Thread.currentThread().interrupt();
	        // Lembre-se de parar o grupo em caso de falha na inicialização.
	        group.shutdownGracefully();
	    }
	    // Não encerre o grupo aqui, ele precisa continuar rodando.
	    // Encerre-o em um método stop() ou no desligamento do servidor.
	}
	
	
	
    /**
     * Envia um DatagramPacket (dados + destino) pelo canal UDP.
     * @param packet O pacote a ser enviado.
     */
    public void send(DatagramPacket packet) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(packet.retain());
        }
    }
	
	/**
	 * Envia dados para um endereço UDP específico.
	 * 
	 * @param targetAddress O endereço do destinatário.
	 * @param data          O ByteBuf com os dados a serem enviados.
	 */
	public void send(InetSocketAddress targetAddress, ByteBuf data) {
		if (channel != null && channel.isActive()) {
			// No UDP, enviamos um DatagramPacket, que combina os dados com o endereço de
			// destino.
			channel.writeAndFlush(new DatagramPacket(data.retain(), targetAddress));
		}
	}

	public void stop() {
		if (channel != null) {
			channel.close();
		}
		if (group != null) {
			group.shutdownGracefully();
		}
	}
}