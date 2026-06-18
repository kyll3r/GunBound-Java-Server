package br.com.gunbound.emulator.services.excepion;

/**
 * Exceção lançada quando ocorre um erro de negócio relacionado à loja, como
 * saldo insuficiente ou falha ao comprar/vender um item.
 */
public class InsufficientFundsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InsufficientFundsException(String message) {
		super(message);
	}
	
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }

}
