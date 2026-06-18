package br.com.gunbound.emulator.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import br.com.gunbound.emulator.ServerConfig;

public class DatabaseManager {

	private static HikariDataSource dataSource;

	static {
		try {
			
	        // Carrega todas as configurações do arquivo config.properties
	        ServerConfig props = ServerConfig.getInstance();

	        // Obtém as configurações necessárias
	        String dburl = props.getDbUrl();
	        String user = props.getDbUser();
	        String password = props.getDbPassword();
			
			// 1. Carregar as propriedades do arquivo db.properties
			//Properties props = loadProperties();

			// 2. Configurar o HikariCP
			HikariConfig config = new HikariConfig();
			//config.setJdbcUrl(props.getProperty("dburl"));
			config.setJdbcUrl(dburl);
			//config.setUsername(props.getProperty("user"));
			config.setUsername(user);
			//config.setPassword(props.getProperty("password"));
			config.setPassword(password);
			config.setDriverClassName("org.mariadb.jdbc.Driver");

			// --- CONFIGURAÇÕES OTIMIZADAS E ROBUSTAS PARA PRODUÇÃO ---

			// Um pool de 25 conexões é um bom ponto de partida para até ~500-1000
			// jogadores,
			// pois as operações de DB são rápidas e, no seu código, assíncronas.
			config.setMaximumPoolSize(50);
			config.setMinimumIdle(5);

			// Tempo máximo que uma conexão pode viver no pool (ex: 30 minutos).
			// Isso força uma rotação gradual das conexões, prevenindo problemas de "memory
			// leak"
			// ou outros problemas de estado no driver do banco de dados.
			// DEVE ser alguns minutos a menos que o 'wait_timeout' do seu MariaDB/MySQL.
			config.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));

			// Tempo que uma conexão pode ficar ociosa no pool antes de ser considerada para
			// remoção (ex: 10 minutos).
			config.setIdleTimeout(TimeUnit.MINUTES.toMillis(10));

			// Tempo máximo para esperar por uma conexão do pool se ele estiver cheio (ex:
			// 30 segundos).
			config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(30));

			// **A SOLUÇÃO DEFINITIVA PARA CONEXÕES "ZUMBI"**
			// Frequência com que o HikariCP enviará um "keepalive" (um ping a nível de
			// aplicação)
			// em uma conexão ociosa para prevenir que ela seja fechada por timeouts do
			// banco de dados ou da rede (firewall).
			// Um valor de 2 minutos é seguro e eficiente.
			config.setKeepaliveTime(TimeUnit.MINUTES.toMillis(2));

			// Define uma query que o HikariCP usará para testar se uma conexão ainda está
			// viva antes de entregá-la à sua aplicação. Isso elimina o erro "Connection is
			// closed".
			config.setConnectionTestQuery("SELECT 1");

			// 4. Inicializar o DataSource do HikariCP
			dataSource = new HikariDataSource(config);
			System.out.println("Pool de conexões com o banco de dados inicializado com sucesso.");

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Falha CRÍTICA ao inicializar o pool de conexões com o banco de dados.", e);
		}
	}

	/**
	 * Pega uma conexão validada do pool.
	 * 
	 * @return Uma conexão de banco de dados ativa e garantida.
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}


	/*private static Properties loadProperties() {
		String caminhoDbProperties = "config/db.properties"; // Caminho padrão

		try (InputStream in = new FileInputStream(caminhoDbProperties)) {

			Properties props = new Properties();
			props.load(in);
			return props;
		} catch (IOException e) {
			throw new DbException("Erro ao carregar db.properties: " + e.getMessage());
		}
	}*/

	private DatabaseManager() {
	}
}
