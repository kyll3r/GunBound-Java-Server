package br.com.gunbound.emulator.buddy.db.dao;

import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;

public interface BuddyUserDAO {
	public BuddyUserDTO getUserByUserId(String userIdQuery);

	public BuddyUserDTO getUserByNickname(String NickNameIdQuery);

}
