package br.com.gunbound.emulator.services.excepion;

/**
 * Exceção lançada quando ocorre uma falha no processo de autenticação.
 */
public class VersionException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VersionException(String message) {
        super(message);
    }
    
    public VersionException(String message, Throwable cause) {
        super(message, cause);
    }
}