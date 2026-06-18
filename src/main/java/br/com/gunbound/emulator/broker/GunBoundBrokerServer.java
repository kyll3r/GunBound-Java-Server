package br.com.gunbound.emulator.broker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import br.com.gunbound.emulator.PacketSizeTracker;
import br.com.gunbound.emulator.model.entities.ServerOption;
import br.com.gunbound.emulator.utils.PacketDecoder;
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
 * Servidor broker do GunBound que gerencia o diretório de servidores de jogo
 * disponíveis. Utiliza Netty 5.x com a API moderna (IoEventLoopGroup).
 */
public class GunBoundBrokerServer {

    private final String host;
    private final int port;
    private final List<ServerOption> serverOptions;
    private final List<Object> worldSession;

    // Campos para gerenciar o ciclo de vida do servidor
    private IoEventLoopGroup bossGroup;
    private IoEventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean isRunning = false;

    /**
     * Construtor do servidor broker.
     * 
     * @param host Endereço IP para bind
     * @param port Porta para escutar conexões
     * @param serverOptions Lista de servidores de jogo disponíveis
     * @param worldSession Lista thread-safe de sessões ativas para rastreamento de ocupação
     */
    public GunBoundBrokerServer(String host, int port, List<ServerOption> serverOptions, List<Object> worldSession) {
        this.host = host;
        this.port = port;
        this.serverOptions = new ArrayList<>(serverOptions); // Cópia defensiva
        this.worldSession = Collections.synchronizedList(worldSession);
    }

    /**
     * Inicia o servidor broker. Este método retorna imediatamente após o bind
     * para permitir uso com virtual threads.
     * 
     * @throws Exception se houver erro ao iniciar o servidor
     */
    public void start() throws Exception {
        if (isRunning) {
            System.out.println("Broker Server já está rodando na porta " + port);
            return;
        }

        // IoEventLoopGroup para aceitar novas conexões (1 thread dedicada)
        bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());

        // IoEventLoopGroup para processar I/O das conexões estabelecidas
        // 0 = número padrão de threads (2 * número de cores do processador)
        workerGroup = new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory());

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler("BrokerServerAcceptor", LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                            // Detecta 4 min e 30 segundos sem dados do cliente
                            new IdleStateHandler(270, 0, 0, TimeUnit.SECONDS),

                            // Logger para debug do tráfego de rede
                            new LoggingHandler("BrokerLogger", LogLevel.DEBUG),

                            // Decoder para separar pacotes do protocolo GunBound
                            new PacketDecoder(),

                            // Rastreador de tamanho dos pacotes de saída
                            new PacketSizeTracker(),

                            // Handler com a lógica de negócio do broker
                            new GunBoundBrokerServerHandler(serverOptions, worldSession)
                        );
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

            // Faz bind na porta e aguarda completar
            ChannelFuture f = b.bind(host, port).sync();
            serverChannel = f.channel();
            isRunning = true;

            System.out.println("Broker Server iniciado com sucesso");
            System.out.println("Escutando em " + host + ":" + port);
            System.out.println("\nDiretório de Servidores:");
            for (ServerOption serverOption : serverOptions) {
                System.out.println("  - " + serverOption.getServerName() + 
                                 " (" + serverOption.getServerDescription() + ")" +
                                 " na porta " + serverOption.getServerPort());
            }

            // Não bloqueia aqui - retorna imediatamente para permitir uso com virtual threads

        } catch (Exception e) {
            System.err.println("Erro ao iniciar Broker Server na porta " + port);
            e.printStackTrace();
            stop(); // Limpa recursos em caso de erro
            throw e;
        }
    }

    /**
     * Para o servidor broker graciosamente, fechando todas as conexões
     * e liberando recursos.
     */
    public void stop() {
        if (!isRunning) {
            System.out.println("Broker Server já está parado");
            return;
        }

        System.out.println("Parando Broker Server na porta " + port + "...");
        isRunning = false;

        try {
            // Fecha o canal do servidor (para de aceitar novas conexões)
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close().sync();
            }
        } catch (InterruptedException e) {
            System.err.println("Interrompido ao fechar canal do Broker Server");
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

        System.out.println("Broker Server parado com sucesso");
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

    /**
     * Método main para iniciar apenas o Broker Server de forma standalone.
     * Para produção, use GunBoundStarter.java que inicializa todos os servidores.
     * 
     * @param args [0] = host (opcional, padrão: 0.0.0.0), [1] = porta (opcional, padrão: 8400)
     */
    public static void main(String[] args) throws Exception {
        // Configurações padrão
        String host = "0.0.0.0";
        int brokerPort = 8400;

        // Processa argumentos da linha de comando
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            try {
                brokerPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida fornecida. Usando porta padrão: " + brokerPort);
            }
        }

        // Configura lista de servidores de jogo disponíveis
        List<ServerOption> serverOptions = new ArrayList<>();
        serverOptions.add(new ServerOption(
            "GunBound Legacy", 
            "AVATAR OFF", 
            "127.0.0.1", 
            8360, 
            0, 
            500, 
            true
        ));
        serverOptions.add(new ServerOption(
            "GunBound Legacy", 
            "AVATAR ON", 
            "127.0.0.1", 
            8361, 
            0, 
            100, 
            true
        ));

        // Lista para rastrear sessões de jogadores (ocupação dos servidores)
        List<Object> worldSession = new ArrayList<>();

        // Cria e inicia o servidor
        GunBoundBrokerServer server = new GunBoundBrokerServer(host, brokerPort, serverOptions, worldSession);

        // Adiciona shutdown hook para encerramento gracioso
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nEncerrando Broker Server...");
            server.stop();
        }));

        server.start();

        // Mantém a aplicação rodando
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
