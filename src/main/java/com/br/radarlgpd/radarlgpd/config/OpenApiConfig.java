package com.br.radarlgpd.radarlgpd.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do SpringDoc OpenAPI 3 (Swagger UI).
 * 
 * Acesso: /swagger-ui.html ou /swagger-ui/index.html
 * OpenAPI JSON: /v3/api-docs
 * 
 * Documenta a API para facilitar integração com frontends e plugins WordPress.
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Radar LGPD API}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(apiServers())
            .components(securityComponents())
            .addSecurityItem(securityRequirement());
    }

    private Info apiInfo() {
        return new Info()
            .title("Radar LGPD API")
            .version("1.0.0-mvp")
            .description("""
                **API de Telemetria para Compliance LGPD**
                
                Esta API recebe dados **agregados e anonimizados** de scans realizados pelo plugin WordPress Radar LGPD.
                
                ## Funcionalidades
                
                - ✅ **Registro de Instâncias**: Novos sites recebem token único
                - ✅ **Telemetria Anonimizada**: Apenas contagens, NUNCA dados pessoais
                - ✅ **Rate Limiting**: 100 requisições/hora por IP
                - ✅ **Validação LGPD**: Consentimento obrigatório
                
                ## Fluxos de Autenticação
                
                ### Cenário A: Plugin Autenticado
                ```
                POST /v1/telemetry/scan-result
                Authorization: Bearer {instance_token}
                ```
                Retorna: `{ "status": "received" }`
                
                ### Cenário B: Novo Plugin (Registro)
                ```
                POST /v1/telemetry/scan-result
                (sem header Authorization)
                ```
                Retorna: `{ "status": "registered", "instance_token": "uuid..." }`
                
                ## Compliance
                
                ⚠️ **IMPORTANTE**: Esta API NÃO aceita dados pessoais identificáveis (CPF, email, telefone).
                Apenas contagens agregadas são permitidas para compliance com LGPD Art. 6º.
                
                ## Rate Limiting
                
                - Limite: **100 requisições/hora** por IP
                - Header de resposta: `X-RateLimit-Remaining`
                - Excesso: **HTTP 429 Too Many Requests**
                """)
            .contact(new Contact()
                .name("Radar LGPD Team")
                .url("https://github.com/cjmz/radarlgpd-api")
                .email("support@radarlgpd.com.br"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> apiServers() {
        return List.of(
            new Server()
                .url("http://localhost:8080")
                .description("Desenvolvimento Local"),
            new Server()
                .url("https://api.radarlgpd.com.br")
                .description("Produção")
        );
    }

    private Components securityComponents() {
        return new Components()
            .addSecuritySchemes("BearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("UUID")
                .description("""
                    **Token de Instância (UUIDv4)**
                    
                    Para plugins já registrados, use:
                    ```
                    Authorization: Bearer {instance_token}
                    ```
                    
                    ⚠️ **Primeiro Scan**: Se o plugin ainda não tem token, 
                    envie a requisição SEM o header Authorization. 
                    Você receberá um `instance_token` na resposta.
                    """));
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("BearerAuth");
    }
}
