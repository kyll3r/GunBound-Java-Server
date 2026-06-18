package br.com.gunbound.emulator.services;

import br.com.gunbound.emulator.model.entities.game.PlayerSession;

public interface HonkService {
	
	public void processPurchaseHonk(PlayerSession player, String msg);
	public void processPurchaseAlterColors(PlayerSession player, int color, boolean balloon);
}
