package br.com.gunbound.emulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.gunbound.emulator.broker.GunBoundBrokerServer;
import br.com.gunbound.emulator.buddy.GunBoundBuddyServer;
import br.com.gunbound.emulator.gameserver.GunBoundGameServer;
import br.com.gunbound.emulator.model.entities.ServerOption;

public class GunBoundStarter {

    public static void main(String[] args) {
        ServerConfig config = ServerConfig.getInstance();

        String serverHost = config.getServerPublicIp();
        String serv1Name = config.getBrokerServ1Name();
        String serv1Descr = config.getBrokerServ1Descr();
        int brokerPort = config.getBrokerPort();
        int gamePortServer = config.getGameServerPort();
        int buddyPort = config.getBuddyServerPort();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        // Usa CopyOnWriteArrayList para melhor performance com virtual threads
        List<Object> gameServerSessions = new CopyOnWriteArrayList<>();

        List<ServerOption> serverOptions = new ArrayList<>();
        try {
            serverOptions.add(new ServerOption(
                serv1Name, serv1Descr, serverHost, 
                gamePortServer, 0, 500, true
            ));
        } catch (Exception e) {
            System.err.println("Erro ao criar ServerOption: " + e.getMessage());
            return;
        }

        // Inicializa servidores
        GunBoundGameServer gameServer = new GunBoundGameServer(gamePortServer);
        GunBoundBuddyServer buddyServer = new GunBoundBuddyServer(serverHost, buddyPort);
        GunBoundBrokerServer brokerServer = new GunBoundBrokerServer(
            serverHost, brokerPort, serverOptions, gameServerSessions
        );

        // Latches para sincronização
        CountDownLatch gameServerLatch = new CountDownLatch(1);
        CountDownLatch buddyServerLatch = new CountDownLatch(1);
        AtomicBoolean gameServerStarted = new AtomicBoolean(false);

        // Inicia Game Server
        executor.submit(() -> {
            try {
                gameServer.start();
                gameServerStarted.set(true);
                System.out.println("✓ Game Server iniciado com sucesso");
            } catch (Exception e) {
                System.err.println("✗ Falha ao iniciar Game Server: " + e.getMessage());
                e.printStackTrace();
            } finally {
                gameServerLatch.countDown();
            }
        });

        // Aguarda Game Server com timeout
        try {
            if (!gameServerLatch.await(10, TimeUnit.SECONDS)) {
                System.err.println("Timeout ao iniciar Game Server!");
                executor.shutdownNow();
                return;
            }

            if (!gameServerStarted.get()) {
                System.err.println("Game Server não iniciou. Abortando.");
                executor.shutdownNow();
                return;
            }
        } catch (InterruptedException e) {
            System.err.println("Inicialização interrompida");
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            return;
        }

        // Inicia Buddy Server
        executor.submit(() -> {
            try {
                buddyServer.start();
                System.out.println("Buddy Server iniciado com sucesso");
            } catch (Exception e) {
                System.err.println("Falha ao iniciar Buddy Server: " + e.getMessage());
                e.printStackTrace();
            } finally {
                buddyServerLatch.countDown();
            }
        });

        // Inicia Broker Server
        executor.submit(() -> {
            try {
                brokerServer.start();
                System.out.println("Broker Server iniciado com sucesso");
            } catch (Exception e) {
                System.err.println("Falha ao iniciar Broker Server: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Shutdown hook para encerramento gracioso
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n--- Encerrando servidores ---");
            try {
                gameServer.stop();
                buddyServer.stop();
                brokerServer.stop();
            } catch (Exception e) {
                System.err.println("Erro ao parar servidores: " + e.getMessage());
            }

            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("--- Gunbound Starter Encerrado ---");
        }));

        System.out.println("\n=== Gunbound Starter Iniciado ===");
        System.out.println("Broker Server: " + serverHost + ":" + brokerPort);
        System.out.println("Game Server: " + serverHost + ":" + gamePortServer);
        System.out.println("Buddy Server: " + serverHost + ":" + buddyPort);
        System.out.println("Pressione Ctrl+C para encerrar.\n");

        // Mantém aplicação rodando
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
