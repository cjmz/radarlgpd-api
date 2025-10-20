package com.br.radarlgpd.radarlgpd.exception;

/**
 * Exceção lançada quando o consentimento não foi concedido.
 * Resulta em HTTP 403 Forbidden.
 */
public class ConsentNotGivenException extends RuntimeException {
    
    public ConsentNotGivenException(String message) {
        super(message);
    }
}
