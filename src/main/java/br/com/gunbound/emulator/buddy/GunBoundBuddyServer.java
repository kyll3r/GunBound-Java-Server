package br.com.gunbound.emulator.buddy;

import java.util.concurrent.TimeUnit;

import br.com.gunbound.emulator.buddy.config.BuddyPacketDecoder;
import br.com.gunbound.emulator.buddy.config.BuddyPacketLoggerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.IoEventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Servidor Buddy do GunBound responsável pelo sistema de amigos e mensagens.
 * Utiliza Netty 5.x com a API moderna (IoEventLoopGroup).
 */
public class GunBoundBuddyServer {

	private final String host;
	private final int port;

	// Campos para gerenciar o ciclo de vida do servidor
	private IoEventLoopGroup bossGroup;
	private IoEventLoopGroup workerGroup;
	private Channel serverChannel;
	private volatile boolean isRunning = false;

	/**
	 * Construtor do servidor Buddy.
	 * 
	 * @param host Endereço IP para bind
	 * @param port Porta para escutar conexões
	 */
	public GunBoundBuddyServer(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Inicia o servidor Buddy. Este método retorna imediatamente após o bind para
	 * permitir uso com virtual threads.
	 * 
	 * @throws Exception se houver erro ao iniciar o servidor
	 */
	public void start() throws Exception {
		if (isRunning) {
			System.out.println("Buddy Server já está rodando na porta " + port);
			return;
		}

		// IoEventLoopGroup para aceitar novas conexões (1 thread dedicada)
		bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());

		// IoEventLoopGroup para processar I/O das conexões estabelecidas
		// 0 = número padrão de threads (2 * número de cores do processador)
		workerGroup = new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory());

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler("BuddyServerAcceptor", LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(
									// Detecta 4 min e 30 segundos sem dados do cliente
									new IdleStateHandler(270, 0, 0, TimeUnit.SECONDS),

									// Logger para debug do tráfego de rede
									new LoggingHandler("BuddyConnection", LogLevel.DEBUG),

									// Decoder para separar pacotes do protocolo Buddy
									new BuddyPacketDecoder(),

									// Logger específico de pacotes Buddy
									new BuddyPacketLoggerHandler(),

									// Handler com a lógica de negócio do sistema Buddy
									new GunBundBuddyServerHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.TCP_NODELAY, true);

			// Faz bind na porta e aguarda completar
			ChannelFuture f = b.bind(host, port).sync();
			serverChannel = f.channel();
			isRunning = true;

			System.out.println("Buddy Server iniciado com sucesso");
			System.out.println("Escutando em " + host + ":" + port);

			// Não bloqueia aqui - retorna imediatamente para permitir uso com virtual
			// threads

		} catch (Exception e) {
			System.err.println("Erro ao iniciar Buddy Server na porta " + port);
			e.printStackTrace();
			stop(); // Limpa recursos em caso de erro
			throw e;
		}
	}

	/**
	 * Para o servidor Buddy graciosamente, fechando todas as conexões e liberando
	 * recursos.
	 */
	public void stop() {
		if (!isRunning) {
			System.out.println("Buddy Server já está parado");
			return;
		}

		System.out.println("Parando Buddy Server na porta " + port + "...");
		isRunning = false;

		try {
			// Fecha o canal do servidor (para de aceitar novas conexões)
			if (serverChannel != null && serverChannel.isOpen()) {
				serverChannel.close().sync();
			}
		} catch (InterruptedException e) {
			System.err.println("Interrompido ao fechar canal do Buddy Server");
			Thread.currentThread().interrupt();
		}

		// Desliga os event loops graciosamente
		// quietPeriod: tempo mínimo antes de desligar (0 = imediato)
		// timeout: tempo máximo para aguardar tarefas pendentes (15s)
		if (workerGroup != null) {
			workerGroup.shutdownGracefully(0, 15, TimeUnit.SECONDS);
		}
		if (bossGroup != null) {
			bossGroup.shutdownGracefully(0, 15, TimeUnit.SECONDS);
		}

		System.out.println("Buddy Server parado com sucesso");
	}

	/**
	 * Verifica se o servidor está em execução.
	 * 
	 * @return true se o servidor está rodando, false caso contrário
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Obtém a porta em que o servidor está escutando.
	 * 
	 * @return número da porta
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Obtém o host configurado para o servidor.
	 * 
	 * @return endereço do host
	 */
	public String getHost() {
		return host;
	}
}
