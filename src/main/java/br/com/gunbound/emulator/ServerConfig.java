package br.com.gunbound.emulator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe central de configuração do servidor. Carrega todas as propriedades do
 * arquivo 'config.properties' e as fornece de forma global e estática para toda
 * a aplicação. Implementada como um Singleton para garantir uma única
 * instância.
 */
public final class ServerConfig {

	private static final String CONFIG_FILE = "config/config.properties";
	private static final ServerConfig instance = new ServerConfig();

	private final Properties properties = new Properties();

	/**
	 * Construtor privado que carrega o arquivo de configuração.
	 */
	private ServerConfig() {
		try (InputStream input = new FileInputStream(CONFIG_FILE)) {
			properties.load(input);
			System.out.println("Arquivo de configuração '" + CONFIG_FILE + "' carregado com sucesso.");
		} catch (Exception e) {
			System.err.println("FATAL: Erro ao carregar o arquivo de configuração '" + CONFIG_FILE + "'.");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retorna a única instância da classe de configuração.
	 * 
	 * @return A instância do ServerConfig.
	 */
	public static ServerConfig getInstance() {
		return instance;
	}

	/**
	 * Método genérico para buscar uma propriedade, com tratamento de erro.
	 */
	private String getProperty(String key) {
		String value = properties.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			throw new RuntimeException("A chave de configuração obrigatória '" + key
					+ "' não foi encontrada ou está vazia em " + CONFIG_FILE);
		}
		return value.trim();
	}

	// --- Métodos de Acesso às Configurações ---

	public String getServerPublicIp() {
		return getProperty("server.public.ip");
	}

	public String getBrokerServ1Name() {
		return getProperty("broker.serv1.name");
	}
	
	public String getBrokerServ1Descr() {
		return getProperty("broker.serv1.descr");
	}
	
	public int getBrokerPort() {
		return Integer.parseInt(getProperty("broker.port"));
	}

	public int getGameServerPort() {
		return Integer.parseInt(getProperty("gameserver.port"));
	}
	
	public int getGameServerTankRatio() {
		return Integer.parseInt(getProperty("gameserver.tank.hidden.ratio"));
	}
	
	public int getGameServerStage0Probability() {
		return Integer.parseInt(getProperty("gameserver.stage0_probability"));
	}
	
	public int getGameServerGoldFactor() {
		return Integer.parseInt(getProperty("gameserver.goldfactor"));
	}
	
	public int getGameServerScoreFactor() {
		return Integer.parseInt(getProperty("gameserver.scorefactor"));
	}
	
	public int getGameServerEventTrigger() {
		return Integer.parseInt(getProperty("gameserver.eventtrigger"));
	}
	
	public int getGameServerEventActProp() {
		return Integer.parseInt(getProperty("gameserver.eventactprop"));
	}
	
	public Integer getGameServerHonkPrice() {
		return Integer.parseInt(getProperty("gameserver.honkprice"));
	}
	
	public Integer getGameServerBalloonPrice() {
		return Integer.parseInt(getProperty("gameserver.balloonprice"));
	}
	
	public Integer getGameServerColorPrice() {
		return Integer.parseInt(getProperty("gameserver.colorprice"));
	}
	
	public String getGameServerChannelMent() {
		return getProperty("gameserver.channelment");
	}
	
	public String getGameServerChannelDayMsg() {
		return getProperty("gameserver.channeldaymsg");
	}
	
	public String getGameServerChannelAfterNoonMsg() {
		return getProperty("gameserver.channelafternoonmsg");
	}
	
	public String getGameServerChannelNightMsg() {
		return getProperty("gameserver.channelnightmsg");
	}
	
	public String getGameServerRoomMent() {
		return getProperty("gameserver.roomment");
	}
	
	public int getGameServerVersionFirst() {
		return Integer.parseInt(getProperty("gameserver.versionfirst"));
	}
	
	public int getGameServerVersionLast() {
		return Integer.parseInt(getProperty("gameserver.versionlast"));
	}
	
	public int getGameServerFuncRestrict() {
		return Integer.parseInt(getProperty("gameserver.funcrestrict"));
	}
	
	public int getGameServerPassableAuthority() {
		return Integer.parseInt(getProperty("gameserver.passableauthority"));
	}
	
	public int getGameServerSuperUserItem() {
		return Integer.parseInt(getProperty("gameserver.superuseritem"));
	}

	public int getBuddyServerPort() {
		return Integer.parseInt(getProperty("buddy.port"));
	}

	public int getBuddyUdpPort() {
		return Integer.parseInt(getProperty("buddy.udp.port"));
	}

	public String getDbUrl() {
		return getProperty("db.url");
	}

	public String getDbUser() {
		return getProperty("db.user");
	}

	public String getDbPassword() {
		return getProperty("db.password");
	}
	
	public String getDbuseSsl() {
		return getProperty("db.useSSL");
	}
}