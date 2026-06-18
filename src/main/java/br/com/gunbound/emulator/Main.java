package br.com.gunbound.emulator;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import br.com.gunbound.emulator.buddy.utils.BuddyCipher;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;

public class Main {
	
	public static void main(String[] args) {
		
		try {
			//mainol1();
			mainFixa();
			//mainol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//mainFixa();
	
	}
	
	public static void mainol1() throws Exception {
		byte[] byteToDecrypt = Utils.hexStringToByteArray((
				"d1 c5 85 01 bd d9 af aa c4 77 04 72 c1 2c e0 7f fe a5 f3 29 94 75 84 4b 06 69 f6 30 f6 df 1d 43").replace(" ", ""));

		// Spondo que os últimos 32 bytes sejam criptografados
		int encryptedStart = byteToDecrypt.length - 32;
		byte[] encryptedPart = Arrays.copyOfRange(byteToDecrypt, encryptedStart, byteToDecrypt.length);
		
		byte[] block = adjustTo16ByteMultiple(encryptedPart);

		byte[] decryptedPayload = BuddyCipher.gunboundDynamicDecrypt(byteToDecrypt, "kyll3r", "1234",
				new byte[] {(byte)0x50, (byte)0x0a, (byte)0x75, (byte)0x65}, 0x0000);

		System.out.println("\nHexDump:" + Utils.bytesToHex(decryptedPayload));
		String decryptedText = new String(decryptedPayload, StandardCharsets.ISO_8859_1);
		System.out.println("Texto descriptografado: " + decryptedText);
	}
	
	public static void mainol() throws Exception {
		byte[] byteToDecrypt = Utils.hexStringToByteArray((
				"A24F581693DC65F5AE8C60D938EA0C62B58331BFB8B0B8BD2ED3090B8C1105105534854B374C8CD9DFB6A92230A96F126ABF45E115A88BAB4A4C61D28FC04982A4C43F4CDE0C0758EA2F3123E1FA50EC").replace(" ", ""));

		// Spondo que os últimos 32 bytes sejam criptografados
		int encryptedStart = byteToDecrypt.length - 48;
		byte[] encryptedPart = Arrays.copyOfRange(byteToDecrypt, encryptedStart, byteToDecrypt.length);
		
		byte[] block = adjustTo16ByteMultiple(encryptedPart);

		byte[] decryptedPayload = BuddyCipher.gunboundDynamicDecrypt(block, "ReDy", "diguinhobr",
				new byte[] {(byte)0x6B, (byte)0x6A, (byte)0x29, (byte)0xAD }, 0x0000);

		System.out.println("HexDump:" + Utils.bytesToHex(decryptedPayload));
		String decryptedText = new String(decryptedPayload, StandardCharsets.ISO_8859_1);
		System.out.println("Texto descriptografado: " + decryptedText);
	}

	public static void mainFixa() {
		try {
			
			// Chave fornecida em hexadecimal
			String hexKey = "0000636F6D73696B0000D222EAFAEE3A";
			//String hexKey = "2c45926cf3396642b670d006a1fa8182";
			byte[] key = hexStringToBytes(hexKey); // Convertendo a chave de hex para bytes

			// Bloco cifrado (substitua com seu bloco real em hexadecimal)
			//String hexCipherText = "A24F581693DC65F5AE8C60D938EA0C62"
			
			//32 bytes
					String hexCipherText = "14 80 12 75 C1 5B 5E DE E4 3F 49 F8 F4 C6 3F 4E D3 41 96 C8 "
					.replace(" ", "");

			byte[] block = adjustTo16ByteMultiple(hexStringToBytes(hexCipherText)); // Convertendo o texto cifrado de
								
			// hex para bytes
			// Descriptografando o bloco usando a chave fornecida
			byte[] decryptedData = GunBoundCipher.aesDecryptBlock(block, key);

			// Exibindo os bytes descriptografados
			System.out.println("Bytes descriptografados:");
			for (byte b : decryptedData) {
				System.out.printf("0x%02X ", b);
			}
			System.out.println(); // Apenas para adicionar uma linha de quebra no final

			String decryptedText = new String(decryptedData, StandardCharsets.ISO_8859_1);
			System.out.println("\nTexto descriptografado: " + decryptedText);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] adjustTo16ByteMultiple(byte[] inputData) {

		System.out.println("Bytes criptografados sem padding:");
		for (byte b : inputData) {
			System.out.printf("0x%02X ", b);
		}
		System.out.println();

		// Calcula o comprimento máximo múltiplo de 16
		int newLength = (inputData.length / 16) * 16;

		// Verifica se existe alguma sobra de bytes
		int excessBytes = inputData.length - newLength;

		// Se houver sobra, descarta os primeiros "excessBytes" bytes
		if (excessBytes > 0) {
			byte[] adjustedData = new byte[newLength];
			System.arraycopy(inputData, excessBytes, adjustedData, 0, newLength); // Tira lixo dos bytes

			System.out.println("Bytes criptografados com padding:");
			for (byte b : inputData) {
				System.out.printf("0x%02X ", b);

			}

			return adjustedData;
		}

		System.out.println("Bytes criptografados com padding:");
		for (byte b : inputData) {
			System.out.printf("0x%02X ", b);
		}
		System.out.println();
		// Caso o comprimento já seja múltiplo de 16, retorna o dado original
		return inputData;
	}

	// Função de conversão de hex para bytes
	public static byte[] hexStringToBytes(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	// Função de preenchimento da chave (pode ser usada para garantir o tamanho da
	// chave)
	public static byte[] padKey(String key, int length) {
		byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
		byte[] paddedKey = new byte[length];
		System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
		// Preenche com 0x00 antes de colocar a chave
		// System.arraycopy(keyBytes, 0, paddedKey, length - keyBytes.length,
		// keyBytes.length);

		return paddedKey;
	}

	// Função de decriptação AES (sem padding no modo NoPadding)
	public static byte[] aesDecryptBlock(byte[] block, byte[] key) throws NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

		// Verifique se o comprimento do bloco é múltiplo de 16 bytes, se não, adicione
		// padding
		if (block.length % 16 != 0) {
			block = addPadding(block);
		}

		return GunBoundCipher.aesDecryptBlock(block, key);
	}

	// Função para adicionar padding (preencher com 0x00 até o próximo múltiplo de
	// 16 bytes)
	public static byte[] addPadding(byte[] data) {
		int length = data.length;
		int paddingLength = 16 - (length % 16);
		byte[] paddedData = new byte[length + paddingLength];

		System.arraycopy(data, 0, paddedData, 0, length);
		return paddedData;
	}
}
