package com.br.radarlgpd.radarlgpd.exception;

import com.br.radarlgpd.radarlgpd.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler global de exceções para todos os controllers.
 * Garante respostas de erro padronizadas e seguras (sem stack traces).
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handler para validações de Bean Validation (campos inválidos).
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(ErrorResponse.FieldError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .build());
        }
        
        log.warn("Erro de validação no path {}: {} erros", request.getRequestURI(), fieldErrors.size());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(getCurrentTimestamp())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message("Payload contém erros de validação")
            .path(request.getRequestURI())
            .errors(fieldErrors)
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handler para campos extras não reconhecidos no JSON.
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnrecognizedProperty(
        HttpMessageNotReadableException ex,
        HttpServletRequest request
    ) {
        String message = "Payload inválido ou contém campos não reconhecidos";
        
        if (ex.getCause() instanceof UnrecognizedPropertyException unrecognizedEx) {
            String fieldName = unrecognizedEx.getPropertyName();
            message = String.format("Campo não reconhecido: '%s'. Apenas campos do schema aprovado são permitidos", fieldName);
            log.warn("Campo extra detectado: {} no path {}", fieldName, request.getRequestURI());
        } else {
            log.warn("Erro ao processar JSON no path {}: {}", request.getRequestURI(), ex.getMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(getCurrentTimestamp())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Invalid Payload")
            .message(message)
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handler para consentimento não concedido.
     * HTTP 403 Forbidden
     */
    @ExceptionHandler(ConsentNotGivenException.class)
    public ResponseEntity<ErrorResponse> handleConsentNotGiven(
        ConsentNotGivenException ex,
        HttpServletRequest request
    ) {
        log.warn("Tentativa sem consentimento no path {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(getCurrentTimestamp())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Consent Required")
            .message("Consentimento não concedido. Dados não podem ser processados conforme LGPD Art. 7º")
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handler para API Key inválida.
     * HTTP 401 Unauthorized
     */
    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidApiKey(
        InvalidApiKeyException ex,
        HttpServletRequest request
    ) {
        log.warn("Tentativa de acesso não autorizado no path {}", request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(getCurrentTimestamp())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Unauthorized")
            .message("API Key inválida ou ausente")
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handler para rate limit excedido.
     * HTTP 429 Too Many Requests
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
        RateLimitExceededException ex,
        HttpServletRequest request
    ) {
        log.warn("Rate limit excedido para IP {} no path {}", 
            request.getRemoteAddr(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(getCurrentTimestamp())
            .status(HttpStatus.TOO_MANY_REQUESTS.value())
            .error("Rate Limit Exceeded")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .header("Retry-After", ex.getRetryAfter())
            .header("X-RateLimit-Limit", "100")
            .body(errorResponse);
    }

    /**
     * Handler genérico para erros não mapeados.
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error("Erro interno no path {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(getCurrentTimestamp())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("Erro interno do servidor. Por favor, tente novamente mais tarde")
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String getCurrentTimestamp() {
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
