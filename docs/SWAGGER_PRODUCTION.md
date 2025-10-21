# 🔧 Configuração Swagger para Produção

## 🚨 Decisão: Manter ou Desabilitar?

### ✅ Opção 1: Manter Habilitado (Recomendado)

**Vantagens:**
- Frontend pode testar a API facilmente
- Documentação sempre atualizada
- Reduz chamados de suporte
- Facilita onboarding de novos devs

**Desvantagens:**
- Expõe estrutura da API publicamente
- Pode ser usado para reconnaissance por atacantes

**Mitigação:**
- Rate limiting já implementado (100 req/hora)
- Não expõe dados sensíveis (apenas schemas)
- Autenticação obrigatória para usar endpoints

### ❌ Opção 2: Desabilitar Completamente

**Vantagens:**
- Segurança extra (obscurity)
- Reduz superfície de ataque

**Desvantagens:**
- Frontend precisa de documentação externa
- Aumenta curva de aprendizado
- Mais tickets de suporte

---

## 🛡️ Opção Recomendada: Segurança por Autenticação

Mantenha o Swagger habilitado, mas adicione proteção extra:

### 1. Proteção por IP (Cloudflare/Nginx)

Permita acesso ao Swagger apenas de IPs conhecidos:

```nginx
# nginx.conf
location /swagger-ui {
    allow 203.0.113.0/24;  # IPs do frontend team
    deny all;
    proxy_pass http://backend:8080;
}

location /v3/api-docs {
    allow 203.0.113.0/24;
    deny all;
    proxy_pass http://backend:8080;
}
```

### 2. Autenticação Básica HTTP (Simples)

Adicione autenticação básica apenas para o Swagger:

```java
// SecurityConfig.java
@Bean
public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated()
        )
        .httpBasic(Customizer.withDefaults());
    
    return http.build();
}
```

Credenciais em `application-prod.properties`:
```properties
spring.security.user.name=${SWAGGER_USER:swagger-admin}
spring.security.user.password=${SWAGGER_PASSWORD:change-me}
```

### 3. Desabilitar Parcialmente

Mantenha o JSON OpenAPI, mas desabilite a UI:

```properties
# application-prod.properties
springdoc.swagger-ui.enabled=false  # ❌ UI desabilitada
springdoc.api-docs.enabled=true     # ✅ JSON acessível
```

Frontend pode gerar client do JSON sem acessar a UI.

---

## 📋 Checklist de Configuração Produção

### ✅ Manter Habilitado (Atual)

- [x] Rate limiting configurado (100 req/h)
- [x] CORS configurado corretamente
- [x] Autenticação JWT obrigatória para endpoints
- [x] Exemplos não contêm dados reais
- [ ] **Considerar**: Autenticação básica HTTP para Swagger UI
- [ ] **Considerar**: Whitelist de IPs

### ❌ Desabilitar Completamente

Adicione em `application-prod.properties`:

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

---

## 🌐 URLs em Produção

Com Swagger habilitado:

- **Swagger UI**: https://api.radarlgpd.com.br/swagger-ui.html
- **OpenAPI JSON**: https://api.radarlgpd.com.br/v3/api-docs
- **OpenAPI YAML**: https://api.radarlgpd.com.br/v3/api-docs.yaml

---

## 🔐 Variáveis de Ambiente (Render/Docker)

Adicione no Render.com ou docker-compose:

```bash
# Obrigatórias (já existentes)
RADARLGPD_API_KEY=sua-api-key-producao
RADARLGPD_DB_URL=jdbc:postgresql://...
RADARLGPD_DB_USERNAME=radarlgpd_user
RADARLGPD_DB_PASSWORD=senha-segura

# Opcionais (proteção extra Swagger)
SWAGGER_USER=admin-swagger
SWAGGER_PASSWORD=senha-super-secreta-swagger
```

---

## 🧪 Testando em Produção

### 1. Verificar se Swagger está acessível

```bash
curl https://api.radarlgpd.com.br/v3/api-docs
```

**Esperado**: JSON com a especificação OpenAPI

### 2. Testar UI

Acesse: https://api.radarlgpd.com.br/swagger-ui.html

**Esperado**: Interface do Swagger carregada

### 3. Testar Autenticação

1. Clique em **Authorize**
2. Cole um `instance_token` válido
3. Execute uma requisição
4. **Esperado**: HTTP 200

---

## 📊 Monitoramento

### Logs de Acesso ao Swagger

Adicione em `application-prod.properties`:

```properties
logging.level.springdoc=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg - [%X{user}] [%X{ip}]%n
```

Monitore acessos suspeitos:
```bash
grep "swagger-ui" /var/log/radarlgpd/application.log
```

---

## 🚀 Deploy com Swagger Habilitado

### Dockerfile (nenhuma mudança necessária)

O Swagger é empacotado automaticamente no JAR:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY target/radarlgpd-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### docker-compose.yml

```yaml
services:
  api:
    image: radarlgpd-api:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - RADARLGPD_API_KEY=${RADARLGPD_API_KEY}
      # Swagger fica habilitado por padrão
    ports:
      - "8080:8080"
```

### Render.com

No `render.yaml`:

```yaml
services:
  - type: web
    name: radarlgpd-api
    env: java
    buildCommand: ./mvnw clean package -DskipTests
    startCommand: java -jar target/radarlgpd-0.0.1-SNAPSHOT.jar
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: RADARLGPD_API_KEY
        sync: false  # Configurar no dashboard
```

**Swagger estará acessível automaticamente!**

---

## 📝 Decisão Final

### Nossa Recomendação: **✅ Manter Habilitado**

**Por quê?**
1. API é pública (não é um sistema interno)
2. Frontend precisa de documentação atualizada
3. Rate limiting já protege contra abuso
4. Não expõe dados sensíveis (apenas schemas)
5. Facilita debugging e onboarding

**Proteções adicionais** (opcionais):
- Autenticação básica HTTP
- Whitelist de IPs
- Monitoramento de acessos

---

## 🆘 Reverter se Necessário

Se encontrar problemas em produção, desabilite rapidamente via env var:

```bash
# Render.com Dashboard
SPRINGDOC_API_DOCS_ENABLED=false
SPRINGDOC_SWAGGER_UI_ENABLED=false
```

**Restart** → Swagger desabilitado instantaneamente! ✅

---

**Escolha feita**: Manter habilitado com rate limiting e monitoramento 🚀
