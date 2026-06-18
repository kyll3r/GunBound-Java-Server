package br.com.gunbound.emulator.services.excepion;

/**
 * Exceção lançada quando ocorre uma falha no processo de autenticação.
 */
public class AuthenticationException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}