package br.com.gunbound.emulator.buddy.db.dao;

import java.util.List;

import br.com.gunbound.emulator.buddy.entities.BuddyList;

public interface BuddyListDao {
		List<BuddyList> findBuddiesByUserId(String userId);

		int addBuddy(String userId, String buddyId, String category);

		boolean deleteBuddy(String userId, String buddyId);

		boolean updateBuddyGroup(String userId, String buddyId, String newCategory);
		// ... outros métodos ...
	}
