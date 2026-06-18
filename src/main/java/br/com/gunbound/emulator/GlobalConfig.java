package br.com.gunbound.emulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GlobalConfig {
	private static final Properties properties = new Properties();

    static {
        try (InputStream input = ServerConfig.class.getClassLoader().getResourceAsStream("gb.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    // Outros métodos de configuração...
    //public static int getServerPort() {
        //return Integer.parseInt(properties.getProperty("server.port", "8360"));
   // }
}
