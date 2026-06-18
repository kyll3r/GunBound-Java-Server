package br.com.gunbound.emulator.buddy.db.exceptions;

/**
 * Exceção lançada quando ocorre uma falha no processo de autenticação.
 */
public class BuddyAuthException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BuddyAuthException(String message) {
        super(message);
    }
    
    public BuddyAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}