package com.br.radarlgpd.radarlgpd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança da aplicação Radar LGPD.
 * 
 * Esta configuração inicial permite acesso público ao health check
 * enquanto protege outros endpoints (que serão criados).
 * 
 * TODO: Implementar autenticação via API Key para endpoints de produção
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Permite acesso público ao health check
                .requestMatchers("/actuator/health").permitAll()
                // TODO: Adicionar autenticação via API Key para /v1/telemetry/**
                .anyRequest().permitAll() // Temporário - mudar para authenticated()
            )
            .csrf(csrf -> csrf.disable()); // Desabilita CSRF para APIs REST

        return http.build();
    }
}
