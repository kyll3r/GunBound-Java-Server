package br.com.gunbound.emulator.buddy.db.services.impl;

import java.util.List;

import br.com.gunbound.emulator.buddy.db.dao.BuddyDbFactory;
import br.com.gunbound.emulator.buddy.db.dao.BuddyListDao;
import br.com.gunbound.emulator.buddy.db.services.BuddyListService;
import br.com.gunbound.emulator.buddy.entities.BuddyList;

public class BuddyListServiceImpl implements BuddyListService {
	private final BuddyListDao dao;

	// Construtor usa factory, mas pode ser DI.
	public BuddyListServiceImpl() {
		this.dao = BuddyDbFactory.CreateBuddyDao();
	}

	@Override
	public List<BuddyList> findBuddiesByUserId(String userId) {
		return dao.findBuddiesByUserId(userId);
	}

	@Override
	public int addBuddy(String userId, String buddyId, String category) {
		return dao.addBuddy(userId, buddyId, category);
	}

	@Override
	public boolean deleteBuddy(String userId, String buddyId) {
		return dao.deleteBuddy(userId, buddyId);
	}

	@Override
	public boolean updateBuddyGroup(String userId, String buddyId, String newCategory) {
		return dao.updateBuddyGroup(userId, buddyId, newCategory);
	}
}