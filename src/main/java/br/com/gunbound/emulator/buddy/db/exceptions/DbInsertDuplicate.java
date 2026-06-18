package br.com.gunbound.emulator.buddy.db.exceptions;

public class DbInsertDuplicate extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DbInsertDuplicate(String message) {
        super(message);
    }
    
    public DbInsertDuplicate(String message, Throwable cause) {
        super(message, cause);
    }
}