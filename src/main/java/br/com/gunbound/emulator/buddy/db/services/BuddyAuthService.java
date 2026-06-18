package br.com.gunbound.emulator.buddy.db.services;

import br.com.gunbound.emulator.buddy.entities.dto.BuddyUserDTO;
import br.com.gunbound.emulator.services.excepion.AuthenticationException;

/**
 * Define o contrato para os serviços de autenticação de jogadores, encapsulando
 * toda a lógica de negócio relacionada à verificação de credenciais e login.
 */
public interface BuddyAuthService {

    /**
     * Tenta autenticar um usuário usando o processo de descriptografia dinâmica
     * específico do GunBound, que requer a senha do banco de dados para gerar a chave de descriptografia.
     *
     * @param username O nome de usuário, previamente descriptografado estaticamente.
     * @param encryptedBlock O bloco de bytes do payload que contém a senha criptografada dinamicamente.
     * @param authToken O token de autenticação da sessão atual, usado como parte da chave de descriptografia.
     * @return Um objeto UserDTO com os dados do usuário se a autenticação for bem-sucedida.
     * @throws AuthenticationException Se a autenticação falhar por qualquer motivo (usuário não encontrado,
     * senha incorreta, falha na descriptografia).
     */
    BuddyUserDTO loginWithToken(String username, byte[] encryptedBlock, byte[] authToken) throws AuthenticationException;

}
