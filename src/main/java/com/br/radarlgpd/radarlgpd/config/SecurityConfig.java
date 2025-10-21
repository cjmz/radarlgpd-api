package com.br.radarlgpd.radarlgpd.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de segurança da aplicação Radar LGPD.
 * NFR-API-001: Implementa autenticação via API Key.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final RateLimitInterceptor rateLimitInterceptor;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs REST stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                // Permite acesso público ao health check
                .requestMatchers("/health", "/actuator/health").permitAll()
                // Permite acesso público ao Swagger UI e OpenAPI docs
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // ÉPICO 1.1: Permite /v1/telemetry/scan-result sem autenticação
                // (RF-API-3.0: Fluxo de Registro de Nova Instância)
                // O controller decide o fluxo baseado na presença do header Authorization
                .requestMatchers("/v1/telemetry/scan-result").permitAll()
                // Todos os outros endpoints devem ser autenticados
                .anyRequest().authenticated()
            )
            // Adiciona filtro de rate limiting primeiro
            .addFilterBefore(rateLimitInterceptor, UsernamePasswordAuthenticationFilter.class)
            // Depois adiciona filtro de API Key
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
