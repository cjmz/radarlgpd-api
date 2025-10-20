package com.br.radarlgpd.radarlgpd.exception;

/**
 * Exceção lançada quando o rate limit é excedido.
 * Resulta em HTTP 429 Too Many Requests.
 */
public class RateLimitExceededException extends RuntimeException {
    
    private final String retryAfter;
    
    public RateLimitExceededException(String message, String retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }
    
    public String getRetryAfter() {
        return retryAfter;
    }
}
