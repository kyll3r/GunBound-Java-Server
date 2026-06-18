package br.com.gunbound.emulator.services.excepion;

/**
 * Exceção lançada quando ocorre uma falha no processo de autenticação.
 */
public class UserNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}