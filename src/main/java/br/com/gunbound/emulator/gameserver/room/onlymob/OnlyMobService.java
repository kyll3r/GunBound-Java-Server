package br.com.gunbound.emulator.gameserver.room.onlymob;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import br.com.gunbound.emulator.gameserver.room.GameRoom;
import br.com.gunbound.emulator.gameserver.room.model.Tank;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;

public class OnlyMobService {

	/*
	 * ========================= VALIDAÇÃO DO PLAYER =========================
	 */

	/**
	 * Verifica se o tank atual do player não é permitido de acordo com as regras do
	 * OnlyMob.
	 */
	private static boolean isTankNotAllowed(RoomMobiles roomMobiles, Tank playerTank) {
		if (playerTank == Tank.RANDOM) {
			return true;
		}

		return !roomMobiles.isEnabled(playerTank);
	}

	/*
	 * ========================= RANDOMIZAÇÃO =========================
	 */

	/**
	 * Sorteia um tank permitido para a sala.
	 */
	private static Tank getRandomAllowedTank(RoomMobiles roomMobiles) {
		List<Tank> allowed = roomMobiles.getAllowedMobiles();

		if (allowed.isEmpty()) {
			return Tank.JD; // fallback defensivo
		}

		return allowed.get(ThreadLocalRandom.current().nextInt(allowed.size()));
	}

	/*
	 * ========================= OPERAÇÃO PRINCIPAL =========================
	 */

	/**
	 * Valida o tank do player e, se necessário, substitui por um tank permitido.
	 *
	 * @return true se o tank do player foi alterado
	 */
	public static void validateAndApply(PlayerSession player, GameRoom room) {
		if (!room.getRoomMobiles().isOnlyMobEnabled()) {
			return;
		}

		Tank currentPrimaryTank = Tank.fromId(player.getRoomTankPrimary());

		setNewTank(player, room, currentPrimaryTank, 1);

		if (room.isAtagRoom()) {
			Tank currentSecTank = Tank.fromId(player.getRoomTankSecondary());
			setNewTank(player, room, currentSecTank, 2);
		}

	}

	private static void setNewTank(PlayerSession player, GameRoom room, Tank currentTank, int tankOp) {
		// Quando o tank do player nao esta liberado
		if (isTankNotAllowed(room.getRoomMobiles(), currentTank)) {
			Tank newTank = getRandomAllowedTank(room.getRoomMobiles());
			if (tankOp == 1)
				player.setInGameTankPrimary(newTank.getId());
			else
				player.setInGameTankSecondary(newTank.getId());
		} else {// Quando o tank do player esta liberado
			if (tankOp == 1)
				player.setInGameTankPrimary(player.getRoomTankPrimary());
			else
				player.setInGameTankSecondary(player.getRoomTankSecondary());
		}
	}
}