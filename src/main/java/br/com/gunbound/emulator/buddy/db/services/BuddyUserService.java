package br.com.gunbound.emulator.buddy.db.services;

import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;

public interface BuddyUserService {
	
	
	/**
	 * Busca um player pelo id (muito util para funcao add/remove na buddy)
	 *
	 * @param Id do player alvo da busca.
	 */
	
	BuddyUserDTO findUserByUserId(String id);
	
	/**
	 * Busca um player pelo nickname (muito util para funcao add/remove na buddy)
	 *
	 * @param nickName o Nick do player alvo da busca.
	 */
	
	BuddyUserDTO findUserByNickName(String nickName);
}
