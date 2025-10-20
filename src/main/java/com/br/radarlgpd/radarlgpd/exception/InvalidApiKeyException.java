package com.br.radarlgpd.radarlgpd.exception;

/**
 * Exceção lançada quando a API Key é inválida ou ausente.
 * Resulta em HTTP 401 Unauthorized.
 */
public class InvalidApiKeyException extends RuntimeException {
    
    public InvalidApiKeyException(String message) {
        super(message);
    }
}
