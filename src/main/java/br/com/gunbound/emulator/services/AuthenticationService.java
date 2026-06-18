package br.com.gunbound.emulator.services;

import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.services.excepion.AuthenticationException;
import br.com.gunbound.emulator.services.excepion.VersionException;

/**
 * Define o contrato para os serviços de autenticação de jogadores, encapsulando
 * toda a lógica de negócio relacionada à verificação de credenciais e login.
 */
public interface AuthenticationService {

	/**
	 * Tenta autenticar um usuário usando o processo de descriptografia dinâmica
	 * específico do GunBound, que requer a senha do banco de dados para gerar a
	 * chave de descriptografia.
	 *
	 * @param username                O nome de usuário, previamente
	 *                                descriptografado estaticamente.
	 * @param encryptedPasswordBlocks O bloco de bytes do payload que contém a senha
	 *                                criptografada dinamicamente.
	 * @param authToken               O token de autenticação da sessão atual, usado
	 *                                como parte da chave de descriptografia.
	 * @param opcode                  O opcode do pacote de login.
	 * @return Um objeto UserDTO com os dados do usuário se a autenticação for
	 *         bem-sucedida.
	 * @throws AuthenticationException Se a autenticação falhar por qualquer motivo
	 *                                 (usuário não encontrado, senha incorreta,
	 *                                 falha na descriptografia).
	 */
	UserDTO loginWithToken(String username, byte[] encryptedPasswordBlocks, byte[] authToken, int opcode)
			throws AuthenticationException;

	int checkVersion(String user, String password, byte[] encryptedVersionBlocks, byte[] authToken, int opcode)
			throws VersionException;
}
