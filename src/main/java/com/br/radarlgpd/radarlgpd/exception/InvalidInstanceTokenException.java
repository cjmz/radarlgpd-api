package com.br.radarlgpd.radarlgpd.exception;

/**
 * Exceção lançada quando um token de instância é inválido, expirado ou banido.
 * Retorna HTTP 401 Unauthorized (RF-API-2.1).
 */
public class InvalidInstanceTokenException extends RuntimeException {
    
    public InvalidInstanceTokenException(String message) {
        super(message);
    }
    
    public InvalidInstanceTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
