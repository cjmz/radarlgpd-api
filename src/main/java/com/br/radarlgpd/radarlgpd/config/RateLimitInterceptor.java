package com.br.radarlgpd.radarlgpd.config;

import com.br.radarlgpd.radarlgpd.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
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
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interceptor para rate limiting usando Bucket4j + Caffeine.
 * NFR-API-002: Limita a 100 requisições por hora por IP.
 */
@Component
@Slf4j
public class RateLimitInterceptor extends OncePerRequestFilter {

    @Value("${radarlgpd.rate-limit.requests-per-hour:100}")
    private int requestsPerHour;

    private final Cache<String, Bucket> bucketCache;
    private final ObjectMapper objectMapper;

    public RateLimitInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        
        // Configura cache Caffeine com TTL de 1 hora
        this.bucketCache = Caffeine.newBuilder()
            .maximumSize(100_000) // Máximo 100k IPs em cache
            .expireAfterWrite(Duration.ofHours(1))
            .build();
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Permite health check sem rate limiting
        if (request.getRequestURI().equals("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        
        // Obtém ou cria bucket para este IP
        Bucket bucket = bucketCache.get(clientIp, key -> createNewBucket());
        
        // Tenta consumir 1 token
        if (bucket.tryConsume(1)) {
            // Token disponível, adiciona headers informativos
            long availableTokens = bucket.getAvailableTokens();
            response.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerHour));
            response.addHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
            
            log.debug("Rate limit OK para IP {} - {} tokens restantes", clientIp, availableTokens);
            filterChain.doFilter(request, response);
        } else {
            // Rate limit excedido
            log.warn("Rate limit excedido para IP {} no path {}", clientIp, request.getRequestURI());
            handleRateLimitExceeded(response, request.getRequestURI());
        }
    }

    /**
     * Extrai o IP real do cliente, considerando proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For pode conter múltiplos IPs, pega o primeiro
            return xForwardedFor.split(",")[0].trim();
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Cria um novo bucket: 100 requisições por hora com refill gradual.
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(requestsPerHour)
            .refillIntervally(requestsPerHour, Duration.ofHours(1))
            .build();
        
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Envia resposta HTTP 429 Too Many Requests.
     */
    private void handleRateLimitExceeded(
        HttpServletResponse response,
        String path
    ) throws IOException {
        
        // Calcula quando o cliente pode tentar novamente (1 hora)
        OffsetDateTime retryAfter = OffsetDateTime.now().plusHours(1);
        String retryAfterStr = retryAfter.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .status(HttpStatus.TOO_MANY_REQUESTS.value())
            .error("Rate Limit Exceeded")
            .message(String.format("Limite de %d requisições por hora excedido. Tente novamente após: %s", 
                requestsPerHour, retryAfterStr))
            .path(path)
            .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Retry-After", String.valueOf(3600)); // 1 hora em segundos
        response.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerHour));
        response.addHeader("X-RateLimit-Remaining", "0");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
