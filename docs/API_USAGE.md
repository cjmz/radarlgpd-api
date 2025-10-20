# Guia de Uso da API - Radar LGPD

## 🚀 Começando

### 1. Configurar API Key

Defina a variável de ambiente com sua API Key:

```bash
export RADARLGPD_API_KEY="sua-api-key-secreta-aqui"
```

Ou configure no arquivo `application-local.properties`:

```properties
radarlgpd.api.key=sua-api-key-secreta-aqui
```

### 2. Iniciar a Aplicação

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

A API estará disponível em: `http://localhost:8080`

## 📡 Endpoint Principal

### POST /v1/telemetry/scan-result

Recebe resultados agregados de scan do plugin WordPress.

#### Headers Obrigatórios

```http
Authorization: Bearer {sua-api-key}
Content-Type: application/json
```

#### Exemplo de Requisição

```bash
curl -X POST http://localhost:8080/v1/telemetry/scan-result \
  -H "Authorization: Bearer sua-api-key-secreta-aqui" \
  -H "Content-Type: application/json" \
  -d '{
    "scan_id": "550e8400-e29b-41d4-a716-446655440000",
    "site_id": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "consent_given": true,
    "scan_timestamp_utc": "2025-10-20T14:30:01Z",
    "scan_duration_ms": 4580,
    "scanner_version": "1.0.0-mvp",
    "environment": {
      "wp_version": "6.4.1",
      "php_version": "8.1"
    },
    "results": [
      {
        "data_type": "CPF",
        "source_location": "wp_comments.comment_content",
        "count": 152
      },
      {
        "data_type": "EMAIL",
        "source_location": "wp_users.user_email",
        "count": 310
      },
      {
        "data_type": "TELEFONE",
        "source_location": "wp_postmeta.meta_value",
        "count": 89
      }
    ]
  }'
```

#### Resposta de Sucesso (200 OK)

```json
{
  "scanId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCESS",
  "message": "Scan processado com sucesso",
  "receivedAt": "2025-10-20T14:30:05-03:00"
}
```

## 🔒 Requisitos Não Funcionais Implementados

### NFR-API-001: Autenticação via API Key

**Obrigatório**: Header `Authorization: Bearer {api-key}`

#### ❌ Erro 401 - API Key Ausente

```bash
curl -X POST http://localhost:8080/v1/telemetry/scan-result \
  -H "Content-Type: application/json" \
  -d '{...}'
```

Resposta:
```json
{
  "timestamp": "2025-10-20T14:30:01-03:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "API Key ausente. Use header 'Authorization: Bearer {api-key}'",
  "path": "/v1/telemetry/scan-result"
}
```

#### ❌ Erro 401 - API Key Inválida

```bash
curl -X POST http://localhost:8080/v1/telemetry/scan-result \
  -H "Authorization: Bearer invalid-key" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

Resposta:
```json
{
  "timestamp": "2025-10-20T14:30:01-03:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "API Key inválida",
  "path": "/v1/telemetry/scan-result"
}
```

---

### NFR-API-002: Rate Limiting

**Limite**: 100 requisições por hora por IP

#### Headers Informativos

Toda resposta bem-sucedida inclui:

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 47
```

#### ❌ Erro 429 - Rate Limit Excedido

```json
{
  "timestamp": "2025-10-20T15:30:01-03:00",
  "status": 429,
  "error": "Rate Limit Exceeded",
  "message": "Limite de 100 requisições por hora excedido. Tente novamente após: 2025-10-20T16:30:01-03:00",
  "path": "/v1/telemetry/scan-result"
}
```

Headers adicionais:
```http
Retry-After: 3600
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
```

---

### NFR-API-003: Validação de Consentimento

**Obrigatório**: `consent_given` deve ser `true`

#### ❌ Erro 403 - Consentimento Não Dado

```bash
curl -X POST http://localhost:8080/v1/telemetry/scan-result \
  -H "Authorization: Bearer sua-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "scan_id": "550e8400-e29b-41d4-a716-446655440000",
    "site_id": "e3b0c44...",
    "consent_given": false,
    ...
  }'
```

Resposta:
```json
{
  "timestamp": "2025-10-20T14:30:01-03:00",
  "status": 403,
  "error": "Consent Required",
  "message": "Consentimento não concedido. Dados não podem ser processados conforme LGPD Art. 7º",
  "path": "/v1/telemetry/scan-result"
}
```

---

### NFR-API-004: Validação de Anonimização

**Regras**:
- ✅ Apenas contagens agregadas
- ❌ NUNCA dados pessoais reais
- ✅ Schema estrito (campos extras são rejeitados)

#### ❌ Erro 400 - Validação de Campos

```json
{
  "timestamp": "2025-10-20T14:30:01-03:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Payload contém erros de validação",
  "path": "/v1/telemetry/scan-result",
  "errors": [
    {
      "field": "scanId",
      "message": "scan_id deve ser um UUID válido (RFC 4122)"
    },
    {
      "field": "scanTimestampUtc",
      "message": "Timestamp deve estar em formato ISO 8601 UTC"
    },
    {
      "field": "results[0].count",
      "message": "count deve ser >= 0"
    }
  ]
}
```

#### ❌ Erro 400 - Campo Não Reconhecido

```bash
curl -X POST http://localhost:8080/v1/telemetry/scan-result \
  -H "Authorization: Bearer sua-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "scan_id": "550e8400-e29b-41d4-a716-446655440000",
    "campo_extra": "valor não permitido",
    ...
  }'
```

Resposta:
```json
{
  "timestamp": "2025-10-20T14:30:01-03:00",
  "status": 400,
  "error": "Invalid Payload",
  "message": "Campo não reconhecido: 'campo_extra'. Apenas campos do schema aprovado são permitidos",
  "path": "/v1/telemetry/scan-result"
}
```

#### ❌ Erro 400 - Possível Dado Pessoal Detectado

```bash
curl -X POST http://localhost:8080/v1/telemetry/scan-result \
  -H "Authorization: Bearer sua-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    ...
    "results": [
      {
        "data_type": "CPF",
        "source_location": "123.456.789-00",  // ❌ CPF real!
        "count": 1
      }
    ]
  }'
```

Resposta:
```json
{
  "timestamp": "2025-10-20T14:30:01-03:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Payload contém erros de validação",
  "path": "/v1/telemetry/scan-result",
  "errors": [
    {
      "field": "results[0].sourceLocation",
      "message": "Campo contém possível CPF"
    }
  ]
}
```

## 📋 Tipos de Dados Permitidos

Os seguintes valores são aceitos para `data_type`:

- `CPF`
- `EMAIL`
- `TELEFONE`
- `RG`
- `CNH`
- `NOME_COMPLETO`
- `ENDERECO`
- `DATA_NASCIMENTO`
- `CARTAO_CREDITO`

## 🧪 Testando Localmente

### Health Check (sem autenticação)

```bash
curl http://localhost:8080/health
```

Resposta:
```json
{
  "status": "UP",
  "message": "Radar LGPD API está funcionando"
}
```

### Teste Completo

```bash
# 1. Define API Key
export API_KEY="dev-api-key-change-in-production"

# 2. Envia scan válido
curl -X POST http://localhost:8080/v1/telemetry/scan-result \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d @test-data/valid-scan.json

# 3. Verifica rate limit headers
curl -i -X POST http://localhost:8080/v1/telemetry/scan-result \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d @test-data/valid-scan.json
```

## 🔐 Boas Práticas de Segurança

### ✅ DO

- Use HTTPS em produção
- Rotacione API Keys periodicamente
- Armazene API Keys em variáveis de ambiente
- Monitore logs de tentativas de acesso não autorizado
- Implemente alertas para rate limiting frequente

### ❌ DON'T

- Nunca commite API Keys no código
- Nunca envie dados pessoais reais
- Nunca desabilite validações de consentimento
- Nunca exponha stack traces aos clientes
- Nunca ignore erros de validação

## 📊 Monitoramento

### Logs Importantes

```bash
# Requisições bem-sucedidas
INFO  - Recebendo scan result - scan_id: 550e8400-...
INFO  - Scan processado - scan_id: 550e8400-..., status: SUCCESS

# Tentativas sem consentimento
WARN  - Tentativa de envio sem consentimento - scan_id: 550e8400-..., site_id: e3b0c44...

# Rate limit excedido
WARN  - Rate limit excedido para IP 192.168.1.100 no path /v1/telemetry/scan-result

# Tentativas não autorizadas
WARN  - Tentativa de acesso não autorizado no path /v1/telemetry/scan-result
```

## 🆘 Troubleshooting

### Problema: "API Key inválida"

**Solução**: Verifique se a API Key no header `Authorization` corresponde ao valor configurado em `application-local.properties` ou na variável de ambiente `RADARLGPD_API_KEY`.

### Problema: "Rate limit excedido"

**Solução**: Aguarde 1 hora ou use um IP diferente. Em desenvolvimento, você pode aumentar o limite em `application-local.properties`:

```properties
radarlgpd.rate-limit.requests-per-hour=1000
```

### Problema: "Campo não reconhecido"

**Solução**: Verifique se todos os campos do JSON seguem exatamente o schema documentado. Campos extras não são permitidos por segurança.

### Problema: "Consentimento não concedido"

**Solução**: Certifique-se de que `consent_given: true` está presente no payload. Sem consentimento explícito, a API rejeita a requisição conforme LGPD.

---

**Versão da API**: 1.0.0-MVP  
**Última atualização**: 2025-10-20
