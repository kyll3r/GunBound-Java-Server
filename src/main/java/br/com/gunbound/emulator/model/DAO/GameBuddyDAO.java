package br.com.gunbound.emulator.model.DAO;

import java.util.List;

import br.com.gunbound.emulator.model.entities.game.GameBuddyList;

public interface GameBuddyDAO {
		List<GameBuddyList> findBuddiesByUserId(String userId);
	}
