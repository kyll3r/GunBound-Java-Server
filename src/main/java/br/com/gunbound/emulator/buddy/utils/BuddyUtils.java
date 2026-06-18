package br.com.gunbound.emulator.buddy.utils;

public class BuddyUtils {

	public static String intToIp(int ip) {
		return String.format("%d.%d.%d.%d", (ip >> 24) & 0xFF, (ip >> 16) & 0xFF, (ip >> 8) & 0xFF, ip & 0xFF);
	}

	public static String intToIpLE(int ip) {
		return String.format("%d.%d.%d.%d", ip & 0xFF, (ip >> 8) & 0xFF, (ip >> 16) & 0xFF, (ip >> 24) & 0xFF);
	}

	public static String removeNulls(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (c != '\0') {
				sb.append(c);
			}
		}
		return sb.toString();
	}

}
