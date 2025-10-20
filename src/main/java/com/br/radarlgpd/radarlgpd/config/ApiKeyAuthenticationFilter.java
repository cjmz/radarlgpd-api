package com.br.radarlgpd.radarlgpd.config;

import com.br.radarlgpd.radarlgpd.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Filtro de autenticação via API Key.
 * NFR-API-001: Verifica presença e validade da API Key no header Authorization.
 */
@Component
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final String validApiKey;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthenticationFilter(
        @Value("${radarlgpd.api.key}") String validApiKey,
        ObjectMapper objectMapper
    ) {
        this.validApiKey = validApiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Permite health check sem autenticação
        if (request.getRequestURI().equals("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        
        // Verifica se header está presente
        if (authHeader == null || authHeader.isBlank()) {
            log.warn("Requisição sem API Key no path: {}", request.getRequestURI());
            handleUnauthorized(response, request.getRequestURI(), 
                "API Key ausente. Use header 'Authorization: Bearer {api-key}'");
            return;
        }

        // Verifica formato Bearer
        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Formato inválido de API Key no path: {}", request.getRequestURI());
            handleUnauthorized(response, request.getRequestURI(), 
                "Formato inválido. Use 'Authorization: Bearer {api-key}'");
            return;
        }

        // Extrai e valida a key
        String apiKey = authHeader.substring(7).trim();
        
        if (!validApiKey.equals(apiKey)) {
            log.warn("API Key inválida tentando acessar: {} - IP: {}", 
                request.getRequestURI(), request.getRemoteAddr());
            handleUnauthorized(response, request.getRequestURI(), 
                "API Key inválida");
            return;
        }

        // API Key válida, continua a cadeia
        log.debug("API Key válida para path: {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }

    /**
     * Envia resposta HTTP 401 Unauthorized com JSON estruturado.
     */
    private void handleUnauthorized(
        HttpServletResponse response, 
        String path, 
        String message
    ) throws IOException {
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Unauthorized")
            .message(message)
            .path(path)
            .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
