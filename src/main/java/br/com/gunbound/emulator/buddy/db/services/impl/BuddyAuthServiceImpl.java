package br.com.gunbound.emulator.buddy.db.services.impl;

import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.buddy.db.dao.BuddyDbFactory;
import br.com.gunbound.emulator.buddy.db.dao.BuddyUserDAO;
import br.com.gunbound.emulator.buddy.db.exceptions.BuddyAuthException;
import br.com.gunbound.emulator.buddy.db.services.BuddyAuthService;
import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;
import br.com.gunbound.emulator.buddy.utils.BuddyCipher;
import br.com.gunbound.emulator.services.excepion.AuthenticationException;

public class BuddyAuthServiceImpl implements BuddyAuthService {

	private final BuddyUserDAO userDAO;

	public BuddyAuthServiceImpl() {
		this.userDAO = BuddyDbFactory.createBuddyUserDao();
	}
	
    @Override
    public BuddyUserDTO loginWithToken(String username, byte[] encryptedBlock, byte[] authToken) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new BuddyAuthException("Nome de usuário inválido.");
        }
        if (encryptedBlock == null || encryptedBlock.length == 0) {
            throw new BuddyAuthException("Bloco de senha inválido.");
        }
        if (authToken == null) {
            throw new BuddyAuthException("AuthToken não pode ser nulo.");
        }

        // 1. Busca o usuário no banco de dados.
        BuddyUserDTO user = userDAO.getUserByUserId(username);
        if (user == null) {
            throw new BuddyAuthException("Usuário '" + username + "' não encontrado.");
        }

        // 2. Tenta descriptografar e validar a senha.
        try {
            byte[] passwordDecryptedPayload = BuddyCipher.gunboundDynamicDecrypt(
                encryptedBlock,
                user.getUserId(),
                user.getPassword(), // Usa a senha do DB para gerar a chave de descriptografia
                authToken,
                0x0000
            );
            
            String receivedPassword = new String(passwordDecryptedPayload, 4, 20, StandardCharsets.ISO_8859_1).trim();

            if (!receivedPassword.equals(user.getPassword())) {
                throw new BuddyAuthException("Senha incorreta para o usuário '" + username + "'.");
            }

        } catch (Exception e) {
            // Se qualquer erro de descriptografia ocorrer, a autenticação falha.
            throw new BuddyAuthException("Falha na descriptografia da senha.", e);
        }

        // 3. Se tudo estiver correto, retorna os dados do usuário.
        System.out.println("Login (com token) bem-sucedido para o usuário: " + username);
        return user;
    }
}

