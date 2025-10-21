# 🐛 Bug Fix: 403 Forbidden com Body Vazio

**Data**: 21 de outubro de 2025  
**Status**: ✅ RESOLVIDO  
**Severidade**: 🔴 CRÍTICA (bloqueava fluxo de registro)

---

## 📋 Sumário Executivo

A API estava retornando **HTTP 403 Forbidden** com body vazio ao receber requisições válidas no endpoint `/v1/telemetry/scan-result` sem o header `Authorization`. 

Este comportamento impedia o fluxo de registro de novas instâncias (RF-API-3.0 do Épico 1.1).

---

## 🔍 Sintomas

### Comportamento Observado

```bash
# Request (válido)
curl -X POST https://radarlgpd-api.onrender.com/v1/telemetry/scan-result \
  -H "Content-Type: application/json" \
  -d '{
    "scanId": "123e4567-e89b-12d3-a456-426614174000",
    "siteId": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "consentGiven": true,
    "scanTimestampUtc": "2025-10-20T21:20:00Z",
    ...
  }'

# Response (incorreta)
HTTP/1.1 403 Forbidden
Content-Length: 0
```

### O que estava errado

- ✅ O JSON estava válido
- ✅ `consentGiven` era `true`
- ❌ API retornava **403 sem mensagem de erro**
- ❌ Header `X-RateLimit-*` mostrava rate limit OK (997 requisições restantes)
- ❌ Não havia logs de erro no servidor

---

## 🕵️ Análise da Causa Raiz

### Fluxo Quebrado

1. **Request chega**: `POST /v1/telemetry/scan-result` (sem `Authorization` header)

2. **RateLimitInterceptor** ✅: Passa (dentro do limite)

3. **ApiKeyAuthenticationFilter** ✅: Permite passar (linha 75-88)
   ```java
   // ÉPICO 1.1: Permite /v1/telemetry/scan-result sem Authorization
   if (requestPath.equals("/v1/telemetry/scan-result")) {
       if (authHeader == null || authHeader.isBlank()) {
           log.debug("Fluxo de registro de instância (sem Authorization header)");
           filterChain.doFilter(request, response);  // ✅ PASSA
           return;
       }
   }
   ```

4. **SecurityFilterChain** ❌: **BLOQUEIA AQUI!**
   ```java
   // SecurityConfig.java (ANTES DA CORREÇÃO)
   .authorizeHttpRequests(authorize -> authorize
       .requestMatchers("/health", "/actuator/health").permitAll()
       .requestMatchers("/swagger-ui/**", ...).permitAll()
       .anyRequest().authenticated()  // ❌ EXIGE AUTENTICAÇÃO
   )
   ```

5. **Resultado**: Spring Security retorna **403 Forbidden** vazio (padrão quando não há authentication context)

6. **Controller NUNCA é executado** ❌

7. **GlobalExceptionHandler NUNCA é acionado** ❌

### Por que o Body estava vazio?

O Spring Security, por padrão, retorna **403 Forbidden** sem body quando:
- Um endpoint está protegido por `.authenticated()`
- Não há contexto de autenticação (SecurityContext vazio)
- Não há exception handler específico configurado para `AccessDeniedException`

---

## ✅ Solução Implementada

### Arquivo Modificado

**`src/main/java/com/br/radarlgpd/radarlgpd/config/SecurityConfig.java`**

### Mudança

```diff
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
+    // ÉPICO 1.1: Permite /v1/telemetry/scan-result sem autenticação
+    // (RF-API-3.0: Fluxo de Registro de Nova Instância)
+    // O controller decide o fluxo baseado na presença do header Authorization
+    .requestMatchers("/v1/telemetry/scan-result").permitAll()
     // Todos os outros endpoints devem ser autenticados
     .anyRequest().authenticated()
 )
```

### Justificativa

1. **Separação de Responsabilidades**:
   - `SecurityConfig` → Define **quem pode acessar** o endpoint
   - `TelemetryController` → Define **qual fluxo seguir** (autenticado vs. registro)

2. **Alinhamento com RF-API-3.0**:
   - Fluxo de registro (Cenário B) REQUER ausência de `Authorization`
   - Se o `SecurityConfig` bloqueia antes, o controller nunca decide

3. **Consistência**:
   - Mesmo padrão usado em `/health` e endpoints do Swagger
   - `.permitAll()` não significa "sem validação", significa "sem exigir authentication context"

---

## 🧪 Validação

### Teste 1: Fluxo de Registro (sem Authorization)

```bash
curl -X POST https://radarlgpd-api.onrender.com/v1/telemetry/scan-result \
  -H "Content-Type: application/json" \
  -d '{
    "scanId": "123e4567-e89b-12d3-a456-426614174000",
    "siteId": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "consentGiven": true,
    "scanTimestampUtc": "2025-10-20T21:20:00Z",
    "scanDurationMs": 1500,
    "scannerVersion": "1.0.0-mvp",
    "environment": {
      "wpVersion": "6.4.0",
      "phpVersion": "8.2.0"
    },
    "results": [
      {
        "dataType": "CPF",
        "sourceLocation": "wp_comments.comment_content",
        "count": 152
      }
    ]
  }'
```

**Resultado Esperado**:
```json
HTTP/1.1 200 OK
{
  "status": "registered",
  "instance_token": "9f8c7b6a-5d4e-3c2b-1a0f-9e8d7c6b5a4f"
}
```

### Teste 2: Fluxo Autenticado (com Authorization)

```bash
curl -X POST https://radarlgpd-api.onrender.com/v1/telemetry/scan-result \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer 9f8c7b6a-5d4e-3c2b-1a0f-9e8d7c6b5a4f" \
  -d '{ ... }'
```

**Resultado Esperado**:
```json
HTTP/1.1 200 OK
{
  "status": "received"
}
```

### Teste 3: Consentimento Negado (deve retornar 403 COM body agora)

```bash
curl -X POST https://radarlgpd-api.onrender.com/v1/telemetry/scan-result \
  -H "Content-Type: application/json" \
  -d '{
    "scanId": "123e4567-e89b-12d3-a456-426614174000",
    "siteId": "abc123",
    "consentGiven": false,  # ❌ FALSO
    ...
  }'
```

**Resultado Esperado**:
```json
HTTP/1.1 403 Forbidden
{
  "timestamp": "2025-10-21T04:02:21Z",
  "status": 403,
  "error": "Consent Required",
  "message": "Consentimento não concedido. Dados não podem ser processados conforme LGPD Art. 7º",
  "path": "/v1/telemetry/scan-result"
}
```

---

## 📊 Impacto

### ✅ Benefícios

1. **Fluxo de Registro Funcional**:
   - Novos plugins conseguem se registrar automaticamente
   - RF-API-3.0 implementado corretamente

2. **Mensagens de Erro Claras**:
   - Agora `ConsentNotGivenException` é tratada corretamente
   - Cliente recebe feedback útil

3. **Compatibilidade com Épico 1.1**:
   - Roteamento inteligente (autenticado vs. registro) funciona

4. **Developer Experience Melhorada**:
   - Logs aparecem corretamente
   - Debugging facilitado

### ⚠️ Sem Impacto em Segurança

- **Validação de Consentimento** ainda é obrigatória (controller)
- **Rate Limiting** ainda funciona (interceptor antes do SecurityFilterChain)
- **Validação de Schema** ainda é executada (@Valid no controller)
- **Fluxo autenticado** ainda exige token válido (controller valida)

---

## 🎓 Lições Aprendidas

### 1. Ordem dos Filtros Importa

```
Request → RateLimitInterceptor → ApiKeyAuthenticationFilter → SecurityFilterChain → Controller
```

Mesmo que um filtro permita passar, o `SecurityFilterChain` pode bloquear depois.

### 2. `.permitAll()` vs. Filter Logic

- **`.permitAll()`**: Necessário quando o endpoint não deve exigir authentication context
- **Filter Logic**: Útil para lógica customizada, mas não substitui SecurityFilterChain

### 3. Spring Security Defaults

- Por padrão, `403 Forbidden` não tem body quando causado por falta de authentication
- Para customizar, é preciso adicionar `AccessDeniedHandler` ou permitir o endpoint

### 4. Debugging de Security Issues

**Checklist**:
1. ✅ Verificar logs dos filtros
2. ✅ Verificar se request chega no controller
3. ✅ Verificar configuração do `SecurityFilterChain`
4. ✅ Verificar ordem dos filtros (`@Order` ou `addFilterBefore`)
5. ✅ Testar com curl/Postman (não só browser)

---

## 🚀 Próximos Passos

1. ✅ Deploy da correção em produção
2. ✅ Validar com teste end-to-end
3. ✅ Atualizar documentação da API
4. ✅ Notificar desenvolvedores do plugin WordPress

---

## 📚 Referências

- [Épico 1.1 - Fluxo de Registro Automático](./EPIC_1.1_IMPLEMENTATION.md)
- [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture)
- [SecurityFilterChain Documentation](https://docs.spring.io/spring-security/reference/servlet/configuration/java.html)

---

**Autor**: GitHub Copilot  
**Revisor**: @clamelo  
**Commit**: `fix: adiciona /v1/telemetry/scan-result ao SecurityConfig.permitAll()`
