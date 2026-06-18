package br.com.gunbound.emulator.buddy.db.services.impl;

import br.com.gunbound.emulator.buddy.db.dao.BuddyDbFactory;
import br.com.gunbound.emulator.buddy.db.dao.BuddyUserDAO;
import br.com.gunbound.emulator.buddy.db.services.BuddyUserService;
import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;

public class BuddyUserServiceImpl implements BuddyUserService {

	private final BuddyUserDAO userDAO;

	public BuddyUserServiceImpl() {
		this.userDAO = BuddyDbFactory.createBuddyUserDao();
	}
	
	@Override
	public BuddyUserDTO findUserByUserId(String id) {
		// 1. Busca o usuário no banco de dados.
		BuddyUserDTO user = userDAO.getUserByUserId(id);
		if (user == null) {
			return null;
		}
		return user;
	}

	@Override
	public BuddyUserDTO findUserByNickName(String nickName) {
		// 1. Busca o usuário no banco de dados.
		BuddyUserDTO user = userDAO.getUserByNickname(nickName);
		if (user == null) {
			return null;
		}
		return user;
	}
}
