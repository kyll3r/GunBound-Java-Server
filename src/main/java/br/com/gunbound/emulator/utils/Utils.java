package br.com.gunbound.emulator.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import br.com.gunbound.emulator.ServerConfig;
import br.com.gunbound.emulator.model.entities.DTO.ChestDTO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Utils {

	private static ServerConfig serverConfig = ServerConfig.getInstance();

	/**
	 * Metodo auxiliar para converter um ByteBuf em uma string hexadecimal.
	 * 
	 * @param buffer O ByteBuf a ser convertido.
	 * @return Uma string representando o conteúdo hexadecimal do buffer.
	 */
	public static String toHexString(ByteBuf buffer) {
		// Converte o ByteBuf para uma array de bytes
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes(bytes);

		// Converte a array de bytes para uma string hexadecimal
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			// Formata cada byte como dois caracteres hexadecimais
			hexString.append(String.format("%02X ", b));
		}
		return hexString.toString().trim();
	}

	public static int randomIntNumber() {

		// Cria uma instância de Random
		Random random = new Random();

		// Gera um inteiro aleatório de 32 bits (4 bytes)
		int rndNum = random.nextInt(Integer.MAX_VALUE); // sempre positivo
		;
		System.out.println("ID de 4 bytes gerado: " + rndNum);
		return rndNum;

	}

	public static byte[] fourBytesTokenGet(int token) {
		// Esse metodo será sempre usado quando necessário converter o token de int para
		// byte

		// Cria um buffer de 4 bytes
		ByteBuffer buffer = ByteBuffer.allocate(4);

		buffer.putInt(token);

		// Obtém o array de bytes
		byte[] idBytes = buffer.array();

		System.out.println("ID de 4 bytes recebido: " + token);
		System.out.println("Representação em bytes: " + Arrays.toString(idBytes));

		return idBytes;
	}

	public static String stringDecode(ByteBuf inputBytes) {
		// Cria um StringBuilder para construir a String.
		StringBuilder result = new StringBuilder();

		// Verifica se o ByteBuf é nulo ou não tem bytes legíveis.
		if (inputBytes == null || !inputBytes.isReadable()) {
			return "";
		}

		// Itera sobre os bytes legíveis do ByteBuf.
		while (inputBytes.isReadable()) {
			// Lê o próximo byte.
			byte currentByte = inputBytes.readByte();

			// Verifica se o byte não é o terminador nulo.
			if (currentByte != 0) {
				// Adiciona o caractere correspondente ao resultado.
				result.append((char) currentByte);
			} else {
				// Se encontrar um byte nulo, para de ler e retorna o resultado.
				return result.toString();
			}
		}

		// Retorna o resultado caso não encontre um byte nulo antes do fim.
		return result.toString();
	}

	/**
	 * Converte um array de bytes em uma string hexadecimal. ( TA DUPLIADO DENTRO DO
	 * GunBounDecrypt) preciso refatorar depois
	 */
	public static String bytesToHex(byte[] bytes) {
		if (bytes == null) {
			return "null";
		}
		final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] toLittleEndian(int valor) {
		byte[] bytes = new byte[4]; // Um inteiro tem 4 bytes

		// Preenche o array de bytes na ordem Little Endian
		bytes[0] = (byte) (valor & 0xFF); // LSB
		bytes[1] = (byte) ((valor >> 8) & 0xFF);
		bytes[2] = (byte) ((valor >> 16) & 0xFF);
		bytes[3] = (byte) ((valor >> 24) & 0xFF); // MSB

		return bytes;
	}

	/**
	 * Redimensiona um array de bytes para um tamanho específico, truncando ou
	 * preenchendo com zeros.
	 * 
	 * @param source O array de bytes de origem.
	 * @param size   O tamanho desejado.
	 * @return Um novo array de bytes com o tamanho especificado.
	 */
	public static byte[] resizeBytes(byte[] source, int size) {
		byte[] resized = new byte[size];
		// Copia os bytes da origem para o novo array.
		// Se a origem for menor, o restante será preenchido com 0s.
		// Se a origem for maior, ela será truncada.
		System.arraycopy(source, 0, resized, 0, Math.min(source.length, size));
		return resized;

	}

	/**
	 * Converte um array de bytes em um ByteBuf de forma otimizada. Esse método cria
	 * uma cópia dos bytes de entrada.
	 *
	 * @param bytes O array de bytes de entrada.
	 * @return Um novo ByteBuf que contém os bytes copiados.
	 */
	public static ByteBuf toByteBuf(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		// Utiliza Unpooled.wrappedBuffer para evitar a cópia.
		// O ByteBuf criado compartilha o array de bytes subjacente.
		// Isso é ótimo para desempenho, mas exige cuidado com a liberação do buffer.
		return Unpooled.wrappedBuffer(bytes);
	}

	/**
	 * Converte uma String em array de bytes
	 *
	 * @param String Uma sequencia de string de entrada.
	 * @return Um novo array de Byte que contém os bytes copiados.
	 */
	public static byte[] hexStringToByteArray(String s) {
		s = s.replaceAll("\\s+", ""); // remove espaços
		int len = s.length();
		byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Extrai uma String a partir de um array de bytes, interrompendo a leitura
	 * assim que encontra o primeiro byte nulo (`0x00`).
	 *
	 * @param byteArray O array de bytes contendo uma String, possivelmente com um
	 *                  terminador nulo (`0x00`) no final ou lixo de memoria.
	 * @return String, sem incluir o byte nulo e qualquer dado após ele.
	 */

	public static String extractString(byte[] byteArray) {
		int length = byteArray.length;

		// Percorre o array procurando o primeiro byte 0x00
		int nullByteIndex = -1;
		for (int i = 0; i < length; i++) {
			if (byteArray[i] == 0x00) {
				nullByteIndex = i;
				break;
			}
		}

		if (nullByteIndex != -1) {
			// Se encontrar o byte 00, cria um novo array até esse ponto
			byte[] stringBytes = new byte[nullByteIndex];
			System.arraycopy(byteArray, 0, stringBytes, 0, nullByteIndex);

			// Converte para String com o charset apropriado (ISO-8859-1 ou outro)
			return new String(stringBytes, StandardCharsets.ISO_8859_1);
		} else {
			// Se não encontrar o byte 00, lê todo o array
			return new String(byteArray, StandardCharsets.ISO_8859_1);
		}
	}

	/**
	 * Extrai uma string do {@link ByteBuf} até o primeiro byte nulo (`0x00`). Se
	 * não encontrar o byte nulo, lê toda a sequência restante do buffer. Utiliza o
	 * charset {@link StandardCharsets#ISO_8859_1} para a conversão.
	 *
	 * @param byteBuf O buffer contendo os dados a serem lidos.
	 * @return A string extraída até o byte nulo ou até o final do buffer.
	 */
	public static String extractString(ByteBuf byteBuf) {
		// Procurar o índice do primeiro byte 00 (terminador nulo)
		int nullByteIndex = byteBuf.indexOf(byteBuf.readerIndex(), byteBuf.writerIndex(), (byte) 0);

		if (nullByteIndex != -1) {
			// Se encontrar o terminador nulo, lê até esse ponto
			byte[] stringBytes = new byte[nullByteIndex - byteBuf.readerIndex()];
			byteBuf.getBytes(byteBuf.readerIndex(), stringBytes);

			// Avança o índice de leitura para depois do terminador nulo
			byteBuf.readerIndex(nullByteIndex + 1);

			return new String(stringBytes, StandardCharsets.ISO_8859_1); // Ou outro charset que você utilizar
		} else {
			// Se não encontrar, lê toda a string até o final do ByteBuf
			byte[] nicknameBytes = new byte[byteBuf.readableBytes()];
			byteBuf.readBytes(nicknameBytes);

			return new String(nicknameBytes, StandardCharsets.ISO_8859_1); // Ou outro charset que você utilizar
		}
	}

	/**
	 * Faz o Calculo da possibilidade de vir dragao ou knight Se o parametro da
	 * função estiver dentro do range gerado em chance o Mobile sera dragao o knight
	 * Ex: hiddenTankRatio = 10 (10%), chance foi 9. Dragon And Knight escolhido EX:
	 * hiddenTankRatio = 10 (10%), chance foi 20. Sera os mobiles 'normais'
	 *
	 * @param DragonAndKnight valor da possibilidade
	 * @return int, o mobile sorteado.
	 */

	public static int randomMobile() {

		Integer hiddenTankRatio = serverConfig.getGameServerTankRatio();
		hiddenTankRatio = hiddenTankRatio != null ? hiddenTankRatio : 5;

		Random random = new Random();

		// Gera um número aleatório entre 0 e 100
		int chance = random.nextInt(100);

		int resultado;

		// se o parametro é 10 o numero da var chance for 9 então a chance esta entre 0
		// e 10.
		if (chance <= hiddenTankRatio) {
			// Gera 17 ou 18
			resultado = 17 + random.nextInt(2); // 17 ou 18
		} else { // 20% de chance
			// Gera um número entre 0 e 13
			resultado = random.nextInt(14); // 0 a 13
		}

		System.out.println("Resultado: " + resultado);
		return resultado;
	}

	public static int randomMap() {

		Integer stage0Prop = serverConfig.getGameServerStage0Probability();
		stage0Prop = stage0Prop != null ? stage0Prop : 5;

		Random random = new Random();

		// Gera um número aleatório entre 0 e 100
		int chance = random.nextInt(100);

		int resultado;

		// se o parametro é 10 o numero da var chance for 9 então a chance esta entre 0
		// e 10.
		if (chance <= stage0Prop) {
			resultado = 0;
		} else { // 20% de chance
			// Gera um número entre 0 e 13
			resultado = random.nextInt(10) + 1; // Randomiza entre mapas de 1 a 10
		}

		System.out.println("Resultado: " + resultado);
		return resultado;
	}

	public static boolean activeEvent() {

		Integer eventActProp = serverConfig.getGameServerEventActProp();
		eventActProp = eventActProp != null ? eventActProp : 0;

		Random random = new Random();

		// Gera um número aleatório entre 0 e 100
		int chance = random.nextInt(100);

		return chance <= eventActProp;

	}

	/**
	 * Verifica se uma String é 'conversivel' a int Primariamente foi usado apenas
	 * para fechar salas
	 *
	 * @param str A string a ser convertida
	 * @return boolean, true ou false.
	 */

	public static boolean isInteger(String str) {
		try {
			// Tenta converter a string para inteiro
			Integer.parseInt(str);
			return true; // Se a conversão for bem-sucedida, retorna true
		} catch (NumberFormatException e) {
			// Se houver um erro na conversão (não for um número válido), retorna false
			return false;
		}
	}

	// O código do item que define o status de "Power User"
	private static final int POWER_USER_ITEM_CODE = 204801;

	/**
	 * Verifica se há um item ativo do tipo "Power User" na lista fornecida.
	 * 
	 * Este método percorre a lista de itens e verifica se existe algum item com o
	 * código definido para "Power User" (POWER_USER_ITEM_CODE) e se a data de
	 * expiração do item é nula ou está no futuro em relação ao momento atual.
	 * 
	 * @param items A lista de itens (objetos {@link ChestDTO}) que serão
	 *              verificados. A lista pode ser nula ou vazia. Se for nula ou
	 *              vazia, o método retorna 0.
	 * 
	 * @return Retorna 1 se um item "Power User" ativo for encontrado na lista, caso
	 *         contrário, retorna 0.
	 */
	public static int hasActivePowerUserItem(List<ChestDTO> items) {
		// Verifica se a lista é nula ou vazia
		if (items == null || items.isEmpty()) {
			return 0; // Retorna 0 se não houver itens
		}

		// Verifica se existe um item "Power User" ativo
		boolean hasActiveItem = items.stream().anyMatch(item -> item.getItem() == POWER_USER_ITEM_CODE
				&& (item.getExpire() == null || convertToLocalDateTime(item.getExpire()).isAfter(LocalDateTime.now())));

		// Retorna 1 se o item "Power User" estiver ativo, 0 caso contrário
		return hasActiveItem ? 1 : 0;
	}

	public static boolean playerIsBanned(Timestamp timestamp) {
		if (timestamp == null) {
			return false; // Retorna 0 se não houver itens
		}
		return convertToLocalDateTime(timestamp).isAfter(LocalDateTime.now());
	}

	/**
	 * Converte um objeto {@link Timestamp} para {@link LocalDateTime}. Normalmente
	 * é usado para lidar com datas de expiração de avatares
	 */
	public static LocalDateTime convertToLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}

	/**
	 * @param str A string da qual os caracteres serão extraídos.
	 * @param idx O número máximo de caracteres a serem retornados.
	 * @return Uma string contendo os primeiros <code>idx</code> caracteres de
	 *         <code>str</code>, ou uma string vazia se a entrada for
	 *         <code>null</code> ou vazia. Se a string original for menor ou igual a
	 *         <code>idx</code> caracteres, ela é retornada inalterada.
	 */
	public static String getStringCharacters(String str, int idx) {
		if (str == null || str.isEmpty()) {
			return ""; // Retorna uma string vazia se a entrada for null ou em branco
		}

		// Retorna os primeiros 'idx' caracteres, garantindo que a string não seja maior
		// que isso
		return str.length() <= idx ? str : str.substring(0, idx);
	}

	/**
	 * Converte um Timestamp para um valor inteiro de 4 bytes (segundos desde a
	 * época Unix).
	 *
	 * @param timestamp O Timestamp a ser convertido.
	 * @return O valor do Timestamp como um inteiro (em segundos).
	 */
	public static int convertTimestampToInt(Timestamp timestamp) {
		if (timestamp == null) {
			throw new IllegalArgumentException("O Timestamp não pode ser nulo.");
		}

		// Converte o Timestamp para milissegundos e depois para segundos (4 bytes)
		long milliseconds = timestamp.getTime(); // Obtém o valor em milissegundos
		int seconds = (int) (milliseconds / 1000); // Converte para segundos (4 bytes)

		return seconds;
	}
	
	public static Map<Integer, Timestamp> checkTimeToBan(String comando) {
        Map<Integer, Timestamp> resultado = new HashMap<>();
        
        // Verificar se o comando é eterno
        if (comando.equals("-e")) {
            // Eterno = 100 anos em horas
            //long tempoEmHoras = Instant.now().plus(100, ChronoUnit.YEARS).toEpochMilli() / 3600000;
        	Timestamp expire = Timestamp.valueOf(LocalDateTime.now().plusYears(100));
        	resultado.put(-100, expire);
            return resultado;
        }

        // Verificar se o comando começa com "-d" (dias)
        if (comando.startsWith("-d")) {
            String valor = comando.substring(2); // Pega a parte depois de "-d"
            try {
                int dias = Integer.parseInt(valor); // Tenta converter para inteiro
                Timestamp expire = Timestamp.valueOf(LocalDateTime.now().plusDays(dias));
                resultado.put(-1, expire);
                return resultado;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // Verificar se o comando começa com "-h" (horas)
        if (comando.startsWith("-h")) {
            String valor = comando.substring(2); // Pega a parte depois de "-h"
            try {
               int horas = Integer.parseInt(valor);
            	Timestamp time = Timestamp.valueOf(LocalDateTime.now().plusHours(horas));
                resultado.put(-1, time); // Retorna o valor de horas
                return resultado;
            } catch (NumberFormatException e) {
            	return null;
            }
        }

        return resultado;
    }

	public static String msgByHour() {
		
		String dayMsg = serverConfig.getGameServerChannelDayMsg();
		dayMsg = dayMsg != null ? dayMsg : "*Good Morning"; // Mensagem do dia
		
		String afternoonMsg = serverConfig.getGameServerChannelAfterNoonMsg();
		afternoonMsg = afternoonMsg != null ? afternoonMsg : "*Good Afternoon"; // Mensagem da tarde
		
		String nightMsg = serverConfig.getGameServerChannelNightMsg();
		nightMsg = nightMsg != null ? nightMsg : "*Good Evening"; // Mensagem da noite
		
		LocalTime agora = LocalTime.now();

		if (agora.isBefore(LocalTime.NOON)) {
			return dayMsg + " ";
		} else if (agora.isBefore(LocalTime.of(18, 0))) {
			return afternoonMsg + " ";
		} else {
			return nightMsg + " ";
		}
	}

}
