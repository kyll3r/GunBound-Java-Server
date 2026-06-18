package br.com.gunbound.emulator.gameserver.packets.readers;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;

import br.com.gunbound.emulator.gameserver.handlers.GameAttributes;
import br.com.gunbound.emulator.gameserver.packets.readers.room.RoomKickPlayerReader;
import br.com.gunbound.emulator.gameserver.room.GameRoom;
import br.com.gunbound.emulator.gameserver.room.RoomManager;
import br.com.gunbound.emulator.gameserver.room.model.enums.GameMode;
import br.com.gunbound.emulator.gameserver.room.onlymob.MobCommandHandler;
import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.model.entities.game.PlayerSessionManager;
import br.com.gunbound.emulator.services.HonkService;
import br.com.gunbound.emulator.services.UserService;
import br.com.gunbound.emulator.services.excepion.InsufficientFundsException;
import br.com.gunbound.emulator.services.impl.HonkServiceImpl;
import br.com.gunbound.emulator.services.impl.UserServiceImpl;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class ServerCommandReader {

	private static final int OPCODE_REQUEST = 0x5100;

	static PlayerSessionManager psm = PlayerSessionManager.getInstance();
	static RoomManager roomMngr = RoomManager.getInstance();
	static final UserService userService = new UserServiceImpl();
	static final HonkService honkService = new HonkServiceImpl();

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> GENERIC_COMMAND (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();

		if (player == null)
			return;

		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		processCommand(ctx, payload, player);
	}

	private static void processCommand(ChannelHandlerContext ctx, byte[] payload, PlayerSession player) {

		ByteBuf request = Unpooled.wrappedBuffer(payload).skipBytes(1);

		// 1. O payload inteiro é a string do commando. Usamos o stringDecode.
		String[] commandParts = Utils.stringDecode(request).split(" ", 2);// limita comando em 2 partes
		String command = commandParts[0];
		String paramCmd = commandParts.length > 1
				? String.join(" ", Arrays.copyOfRange(commandParts, 1, commandParts.length))
				: ""; // Valor padrão vazio caso não haja parâmetro
		System.out.printf("COMMAND: %s paramCmd: %s \n", command, paramCmd);

		if (command.equals("close")) {
			if (!checkIfaGameMaster(player))
				return;

			if (paramCmd.isEmpty()) {
				GameRoom room = player.getCurrentRoom();
				if (room != null) {
					// checkIfaRoomMaster(player, room);
					sendPrint(player, "Room Was Closed");
					//room.submitAction(() -> closeRoom(room), ctx);
					roomMngr.closeRoom(room);
				}
			} else {

				if (Utils.isInteger(paramCmd)) {
					Integer rNmbr = Integer.valueOf(paramCmd);

					// -1 porque aqui é um array no array a sala 2 = 1
					GameRoom room = roomMngr.getRoomById(rNmbr - 1);

					if (room != null) {
						sendPrint(player, "Room Was Closed");
						//room.submitAction(() -> closeRoom(room), ctx);
						roomMngr.closeRoom(room);
					}
				}
			}

		} else if (command.equals("bcm")) {
			if (!checkIfaGameMaster(player))
				return;

			String msgToSend = paramCmd;
			String color = "";

			if (msgToSend != null) {
				char first = paramCmd.charAt(0);

				if (isSpecialChar(first)) {
					color = String.valueOf(first);
					msgToSend = paramCmd.substring(1);
				}
			}

			// validar espaço no inicio
			if (msgToSend.startsWith(" ")) {
				msgToSend = msgToSend.substring(1); // remover
			}

			msgToSend = color + player.getNickName() + "] " + msgToSend;

			MessageBcmReader.broadcastSendMessage(msgToSend);

		} else if (command.equals("dc")) {
			if (!checkIfaGameMaster(player))
				return;

			// Se o parâmetro (nickname) não for passado, envia mensagem de erro
			if (paramCmd.isEmpty()) {
				sendPrint(player, "ADMIN >> Please provide a nickname");
				return;
			}

			PlayerSession psToDc = psm.getSessionPlayerByNickname(paramCmd);

			if (psToDc != null) {
				sendPrint(player, "ADMIN >> " + psToDc.getNickName() + " Was disconnected");
				// sendPrint(psToDc, "You Were Disconnected by a GM");
				psToDc.getPlayerCtx().close();
			} else {
				sendPrint(player, "ADMIN >> Player not found");
			}

		} else if (command.equals("room")) {
			if (!checkIfaGameMaster(player))
				return;

			try {
				int roomId = Integer.parseInt(paramCmd) - 1;
				GameRoom room = RoomManager.getInstance().getRoomById(roomId);

				if (room != null) {
					StringBuilder strRoom = new StringBuilder();

					strRoom.append("ROOM INFO\r\n");
					strRoom.append("Title: " + room.getTitle() + "\r\n");
					strRoom.append("Pwd: " + room.getPassword() + "\r\n");
					strRoom.append("Master: " + room.getRoomMaster().getNickName() + "\r\n");
					strRoom.append("Mode: " + GameMode.fromId(room.getGameMode()) + "\r\n");
					strRoom.append("Players: " + room.getPlayerCount() + "\r\n");
					strRoom.append("Started: " + room.getStartTime() + "\r\n");

					sendPrint(player, strRoom.toString());

				} else {
					sendPrint(player, "Admin >> Room not found.");
				}

			} catch (Exception e) {
				sendPrint(player, "Admin >> Room not found -1.");
			}

		} else if (command.equals("online")) {
			if (!checkIfaGameMaster(player))
				return;

			Integer totalOn = PlayerSessionManager.getInstance().getActivePlayerCount();
			sendPrint(player, "Total Online: " + totalOn);

		} else if (command.equals("ban")) {
			if (!checkIfaGameMaster(player))
				return;

			try {
				String[] infoParts = paramCmd.split(" ", 3);
				String time = infoParts[0];
				String playerToBan = infoParts[1];
				String reason = infoParts[2];

				UserDTO usertoBan = userService.findUserByNickName(playerToBan);
				if (usertoBan != null) {
					Map<Integer, Timestamp> infoToban = Utils.checkTimeToBan(time);

					if (infoToban.containsKey(-1)) {
						userService.banAnyPlayer(usertoBan.getUserId(), -1, infoToban.get(-1), reason, player);
					} else if (infoToban.containsKey(-100)) {
						userService.banAnyPlayer(usertoBan.getUserId(), -100, infoToban.get(-100), reason, player);
					} else {
						sendPrint(player, "ADMIN >> Wrong command");
					}

				} else {
					sendPrint(player, "ADMIN >> user not found");
				}
			} catch (Exception e) {
				sendPrint(player, "ADMIN >> Wrong command -1");
				e.printStackTrace();
			}

			sendPrint(player, "ADMIN >> Player was banned successfully.");

		} else if (command.equals("k")) {
			if (!checkIfaGameMaster(player))
				return;

			// Se o parâmetro (nickname) não for passado, envia mensagem de erro
			if (paramCmd.isEmpty()) {
				sendPrint(player, "ADMIN >> Please provide a nickname");
				return;
			}

			PlayerSession psToKick = psm.getSessionPlayerByNickname(paramCmd);

			if (psToKick != null) {

				GameRoom room = psToKick.getCurrentRoom();

				if (room != null) {
					sendPrint(player, "ADMIN >> " + psToKick.getNickName() + " Was kicked");
					RoomKickPlayerReader.removePlayerFromRoom(room, psToKick);
				} else {
					sendPrint(player, "ADMIN >> " + psToKick.getNickName() + " isn't Playing");
				}

			} else {
				sendPrint(player, "ADMIN >> Player not found");
			}

		} else if (command.equals("passkey")) {
			if (!checkIfaRoomMaster(player, player.getCurrentRoom()))
				return;

			PlayerSession psTopassKey = psm.getSessionPlayerByNickname(paramCmd);

			if (psTopassKey != null) {
				player.getCurrentRoom().passKeyRoomMaster(psTopassKey);
				// sendPrint(player, "NewMaster: " + psTopassKey.getNickName());
			}

		} else if (command.equals("onlymob") || command.equals("om")
				|| command.equals("only")) {
			if (!checkIfaRoomMaster(player, player.getCurrentRoom()))
				return;

			GameRoom room = player.getCurrentRoom();

			try {
				MobCommandHandler.handleOnly(room, paramCmd);

			} catch (IllegalArgumentException e) {
				sendPrint(player, "Syntax Error");
				System.out.println("Parametro invalido no mobile");
			} catch (Exception e) {
				sendPrint(player, "An error occurred.");
				System.err.println("Erro no onlymob: " + e.getCause());
				e.printStackTrace();
			}

			room.broadcastMobileConfig();

		} else if (command.equals("randomteam")) {
			if (!checkIfaRoomMaster(player, player.getCurrentRoom()))
				return;

			GameRoom room = player.getCurrentRoom();
			// Não pode ser usado se o jogo já começou
			if (room.isGameStarted()) {
				return;
			}

			// Submete a ação ao executor da sala para garantir thread-safety
			room.submitAction(() -> {
				boolean isNowEnabled = room.toggleRandomTeamsEnabled();
				String feedback = isNowEnabled ? "Times aleatórios [ATIVADOS]!" : "Times aleatórios [DESATIVADOS]!";
				sendPrint(player, feedback);
			}, ctx);

		} else if (command.equals("textcolor") || command.equals("tc")) {
			try {

				if (paramCmd == null)
					return;

				int colorId = Integer.parseInt(paramCmd);

				honkService.processPurchaseAlterColors(player, colorId, false);
				sendPrint(player, "Success!");

			} catch (InsufficientFundsException e) {
				sendPrint(player, "You don't have enough gold.");
				System.out.println("Sem gold para ativar balao");
			} catch (Exception e) {
				sendPrint(player, "An error occurred.");
				System.err.println("Erro para comprar cor do texto: " + e.getCause());
				e.printStackTrace();
			}
		} else if (command.equals("bl") || command.equals("balloon")) {
			try {

				if (paramCmd == null)
					return;

				int colorId = Integer.parseInt(paramCmd);

				honkService.processPurchaseAlterColors(player, colorId, true);
				sendPrint(player, "Success!");

			} catch (InsufficientFundsException e) {
				sendPrint(player, "You don't have enough gold.");
				System.out.println("Sem gold para ativar balao");
			} catch (Exception e) {
				sendPrint(player, "An error occurred.");
				System.err.println("Erro para comprar balao: " + e.getCause());
				e.printStackTrace();
			}
		} else if (command.equals("bzn") || command.equals("honk")) {
			try {
				String msgToSend = paramCmd;

				if (msgToSend == null)
					return;

				String msgFormatted = player.getNickName() + "] " + msgToSend;
				honkService.processPurchaseHonk(player, msgToSend);
				MessageBcmReader.bznSendMessage(msgFormatted);

			} catch (InsufficientFundsException e) {
				sendPrint(player, "You don't have enough gold to honk.");
				System.out.println("Sem gold para buzinar");
			} catch (Exception e) {
				System.err.println("Erro para bozinar: " + e.getCause());
			}
		} else {
			sendPrint(player, "Unknown command");
		}

	}

	/*
	 * private static void closeRoom(GameRoom room) { // snapshot para evitar
	 * concorrência List<PlayerSession> recipients = new
	 * ArrayList<>(room.getPlayersBySlot().values()); for (PlayerSession
	 * playerInRoom : recipients) { RoomKickPlayerReader.removePlayerFromRoom(room,
	 * playerInRoom); } // add 04-01-26 - força fechar room bugado
	 * RoomManager.getInstance().removeRoom(room.getRoomId()); }
	 */

	// logica para verificar se é um RoomMaster.
	private static boolean checkIfaRoomMaster(PlayerSession player, GameRoom room) {
		if (room == null || !player.equals(room.getRoomMaster())) {
			// Apenas o dono da sala pode fechar ela.
			return false;
		}
		return true;
	}

	// logica para verificar se é um GameMaster.
	private static boolean checkIfaGameMaster(PlayerSession player) {
		if (player.getAuthority() >= 99) {
			return true;
		}
		return false;
	}

	private static boolean isSpecialChar(char c) {
		return c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '*' || c == '&';
	}

	private static void sendPrint(PlayerSession player, String msg) {
		MessageBcmReader.printMsgToPlayer(player, msg);
	}
}