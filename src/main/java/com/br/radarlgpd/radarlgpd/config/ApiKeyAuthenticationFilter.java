package com.br.radarlgpd.radarlgpd.config;

import com.br.radarlgpd.radarlgpd.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Filtro de autenticação via API Key.
 * NFR-API-001: Verifica presença e validade da API Key no header Authorization.
 */
@Component
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final String validApiKey;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    public ApiKeyAuthenticationFilter(
        @Value("${radarlgpd.api.key}") String validApiKey,
        ObjectMapper objectMapper,
        Environment environment
    ) {
        this.validApiKey = validApiKey;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Permite health check sem autenticação
        if (requestPath.equals("/health") || requestPath.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Em ambiente de desenvolvimento (local), permite acesso ao Swagger/OpenAPI sem autenticação
        boolean isLocalEnv = Arrays.asList(environment.getActiveProfiles()).contains("local");
        if (isLocalEnv && isSwaggerOrApiDocsPath(requestPath)) {
            log.debug("Ambiente DEV: permitindo acesso ao Swagger/OpenAPI sem autenticação - path: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        
        // ÉPICO 1.1: Permite /v1/telemetry/scan-result sem Authorization (fluxo de registro)
        // O controller decidirá qual fluxo seguir baseado na presença do header
        if (requestPath.equals("/v1/telemetry/scan-result")) {
            if (authHeader == null || authHeader.isBlank()) {
                // Sem Authorization: fluxo de registro de nova instância
                log.debug("Fluxo de registro de instância (sem Authorization header)");
                filterChain.doFilter(request, response);
                return;
            }
            // Com Authorization: valida como instance token ou API key
            // Deixa o controller decidir se é válido ou não
            log.debug("Fluxo autenticado (com Authorization header)");
            filterChain.doFilter(request, response);
            return;
        }

        // Para outros endpoints, exige Authorization
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
     * Verifica se o path é relacionado ao Swagger ou OpenAPI docs.
     */
    private boolean isSwaggerOrApiDocsPath(String path) {
        return path.startsWith("/swagger-ui") ||
               path.equals("/swagger-ui.html") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars/") ||
               path.equals("/favicon.ico");
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
