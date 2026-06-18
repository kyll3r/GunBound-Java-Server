package br.com.gunbound.emulator.services.excepion;

/**
 * Exceção lançada quando ocorre uma falha no processo de autenticação.
 */
public class AuthorityException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthorityException(String message) {
        super(message);
    }
    
    public AuthorityException(String message, Throwable cause) {
        super(message, cause);
    }
}