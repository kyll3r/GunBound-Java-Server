package br.com.gunbound.emulator.services.impl;

import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.ServerConfig;
import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.DAO.UserDAO;
import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.services.AuthenticationService;
import br.com.gunbound.emulator.services.UserService;
import br.com.gunbound.emulator.services.excepion.AuthenticationException;
import br.com.gunbound.emulator.services.excepion.AuthorityException;
import br.com.gunbound.emulator.services.excepion.UserNotFoundException;
import br.com.gunbound.emulator.services.excepion.VersionException;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;

public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserDAO userDAO;
	private UserService userService;

	// carrega as configurações
	ServerConfig serverConfig = ServerConfig.getInstance();

	public AuthenticationServiceImpl() {
		this.userDAO = DAOFactory.CreateUserDao();
		this.userService = new UserServiceImpl();
	}

	@Override
	public UserDTO loginWithToken(String username, byte[] encryptedPasswordBlocks, byte[] authToken, int opcode)
			throws AuthenticationException {
		if (username == null || username.trim().isEmpty()) {
			throw new AuthenticationException("Nome de usuário inválido.");
		}
		if (encryptedPasswordBlocks == null || encryptedPasswordBlocks.length == 0) {
			throw new AuthenticationException("Bloco de senha inválido.");
		}
		if (authToken == null) {
			throw new AuthenticationException("AuthToken não pode ser nulo.");
		}

		UserDTO user;

		try {
			user = userService.getUserById(username);

		} catch (UserNotFoundException e) {
			throw new AuthenticationException("Usuário '" + username + "' não encontrado.");
		}

		Integer minAuthority = serverConfig.getGameServerPassableAuthority();
		minAuthority = minAuthority != null ? minAuthority : 0;

		if (user.getAuthority() < minAuthority || user.getAuthority2() < minAuthority) {

			if (user.getAuthority() == -1) {
				if (Utils.playerIsBanned(user.getRestrictTime())) {
					throw new AuthorityException("Usuário '" + username + "' banido.");
				} else {
					userDAO.updateAuthority(user.getUserId(), 1);
				}
			} else {
				throw new AuthorityException("Usuário '" + username + "' banido.");
			}
		}

		// 2. Tenta descriptografar e validar a senha.
		try {
			byte[] passwordDecryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(encryptedPasswordBlocks,
					user.getUserId(), user.getPassword(), // Usa a senha do DB para gerar a chave de descriptografia
					authToken, opcode);

			String receivedPassword = new String(passwordDecryptedPayload, 0, 12, StandardCharsets.ISO_8859_1).trim();

			if (!receivedPassword.equals(user.getPassword())) {
				throw new AuthenticationException("Senha incorreta para o usuário '" + username + "'.");
			}

		} catch (Exception e) {
			// Se qualquer erro de descriptografia ocorrer, a autenticação falha.
			throw new AuthenticationException("Falha na descriptografia da senha.", e);
		}

		// 3. Se tudo estiver correto, retorna os dados do usuário.
		System.out.println("Login (com token) bem-sucedido para o usuário: " + username);
		return user;
	}

	@Override
	public int checkVersion(String user, String password, byte[] encryptedVersionBlocks, byte[] authToken, int opcode)
			throws VersionException {
		int clientVersion = 0;

		try {
			byte[] dynamicPayload = GunBoundCipher.gunboundDynamicDecrypt(encryptedVersionBlocks, user, password,
					authToken, opcode);
			clientVersion = (dynamicPayload[0x14] & 0xFF) | ((dynamicPayload[0x15] & 0xFF) << 8);
		} catch (Exception e) {
			// Se qualquer erro de descriptografia ocorrer, a autenticação falha.
			throw new VersionException("Falha na descriptografia da versao.", e);
		}

		if (!(clientVersion >= serverConfig.getGameServerVersionFirst()
				&& clientVersion <= serverConfig.getGameServerVersionLast())) {
			throw new VersionException("Versao incorreta.");
		}

		return clientVersion;
	}
}
