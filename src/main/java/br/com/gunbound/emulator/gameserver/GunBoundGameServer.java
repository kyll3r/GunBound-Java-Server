package br.com.gunbound.emulator.gameserver;

import java.util.concurrent.TimeUnit;

import br.com.gunbound.emulator.PacketSizeTracker;
import br.com.gunbound.emulator.gameserver.handlers.GunBoundGameHandler;
import br.com.gunbound.emulator.helpers.PacketRateLimiter;
import br.com.gunbound.emulator.helpers.PacketSizeLimiter;
import br.com.gunbound.emulator.utils.PacketDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class GunBoundGameServer {

	private final int port;

	// Campos para gerenciar o ciclo de vida do servidor
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private Channel serverChannel;
	private volatile boolean isRunning = false;

	public GunBoundGameServer(int port) {
		this.port = port;
	}

	public void start() throws Exception {
		if (isRunning) {
			System.out.println("Game Server já está rodando na porta " + port);
			return;
		}

		// API moderna do Netty 5.x
		// 1 thread para aceitar conexões
		bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
		// 0 = número padrão de threads (geralmente 2 * número de cores)
		workerGroup = new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory());

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler("GameServerAcceptor", LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							ch.pipeline().addLast(
							        //Limita tamanho do pacote (proteção básica)
							        new PacketSizeLimiter(8192), // máx 8KB por pacote
							        // Limita taxa de pacotes (proteção contra flood)
							        new PacketRateLimiter(50, TimeUnit.SECONDS), // máx 50 pacotes/segundo
									// Detecta 4 min e 30 segundos sem dados do cliente
									new IdleStateHandler(270, 0, 0, TimeUnit.SECONDS),
									// Logger para debug
									new LoggingHandler("GameServerLogger", LogLevel.DEBUG),
									// Decoder de pacotes GunBound
									new PacketDecoder(),
									// Rastreador de tamanho de pacotes
									new PacketSizeTracker(),
									// Handler de lógica de jogo
									new GunBoundGameHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 1024).childOption(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_RCVBUF, 32 * 1024) // NOVO 32KB receive buffer
					.childOption(ChannelOption.SO_SNDBUF, 32 * 1024) // NOVO: 32KB send buffer
					.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024)) // NOVO:																								// backpressure
					.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

			// Faz bind na porta e aguarda completar
			ChannelFuture f = b.bind(port).sync();
			serverChannel = f.channel();
			isRunning = true;

			System.out.println("Game Server iniciado com sucesso na porta " + port);

			// Não bloqueia aqui - retorna imediatamente para permitir uso com virtual
			// threads

		} catch (Exception e) {
			System.err.println("Erro ao iniciar Game Server na porta " + port);
			e.printStackTrace();
			stop(); // Limpa recursos em caso de erro
			throw e;
		}
	}

	public void stop() {
		if (!isRunning) {
			System.out.println("Game Server já está parado");
			return;
		}

		System.out.println("Parando Game Server na porta " + port + "...");
		isRunning = false;

		try {
			// Fecha o canal do servidor (para de aceitar novas conexões)
			if (serverChannel != null && serverChannel.isOpen()) {
				serverChannel.close().sync();
			}
		} catch (InterruptedException e) {
			System.err.println("Interrompido ao fechar canal do Game Server");
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

		System.out.println("Game Server parado com sucesso");
	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getPort() {
		return port;
	}
}
