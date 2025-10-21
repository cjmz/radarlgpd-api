# 📚 Documentação Swagger - Radar LGPD API

## 🚀 Quick Start

### Acesso Local

Após iniciar a aplicação (`./mvnw spring-boot:run`), acesse:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

### Acesso em Produção

- **Swagger UI**: https://api.radarlgpd.com.br/swagger-ui.html
- **OpenAPI JSON**: https://api.radarlgpd.com.br/v3/api-docs

---

## 🎯 O que é o Swagger UI?

O **Swagger UI** é uma interface web interativa que:

✅ Documenta automaticamente todos os endpoints da API  
✅ Permite testar requisições diretamente no navegador  
✅ Mostra exemplos de payloads e respostas  
✅ Exibe schemas JSON detalhados  
✅ Suporta autenticação (Bearer Token)  

---

## 🔑 Como Testar com Autenticação

### Cenário 1: Novo Plugin (Registro)

1. Acesse o Swagger UI
2. Clique em `POST /v1/telemetry/scan-result`
3. Clique em **"Try it out"**
4. **NÃO preencha** o campo `Authorization`
5. Cole o payload de exemplo
6. Clique em **"Execute"**
7. ✅ Você receberá um `instance_token` na resposta

```json
{
  "status": "registered",
  "instance_token": "9f8c7b6a-5d4e-3c2b-1a0f-9e8d7c6b5a4f"
}
```

### Cenário 2: Plugin Autenticado

1. Copie o `instance_token` recebido no Cenário 1
2. No topo da página, clique no botão **🔓 Authorize**
3. Cole o token (apenas o UUID, sem "Bearer")
4. Clique em **"Authorize"** e depois **"Close"**
5. Agora faça uma nova requisição em `POST /v1/telemetry/scan-result`
6. ✅ Você receberá `{ "status": "received" }`

---

## 📝 Exemplos de Payloads

### Payload Básico (Mínimo)

```json
{
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
}
```

### Payload Completo (Múltiplos Resultados)

```json
{
  "scanId": "550e8400-e29b-41d4-a716-446655440000",
  "siteId": "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a",
  "consentGiven": true,
  "scanTimestampUtc": "2025-10-20T22:30:15Z",
  "scanDurationMs": 3200,
  "scannerVersion": "1.0.0",
  "environment": {
    "wpVersion": "6.4.0",
    "phpVersion": "8.2.0"
  },
  "results": [
    {
      "dataType": "CPF",
      "sourceLocation": "wp_comments.comment_content",
      "count": 152
    },
    {
      "dataType": "EMAIL",
      "sourceLocation": "wp_users.user_email",
      "count": 250
    },
    {
      "dataType": "PHONE",
      "sourceLocation": "wp_postmeta.meta_value",
      "count": 89
    },
    {
      "dataType": "IP_ADDRESS",
      "sourceLocation": "wp_comments.comment_author_IP",
      "count": 450
    }
  ]
}
```

---

## ⚠️ Validações Importantes

### ✅ Campos Obrigatórios

| Campo | Formato | Exemplo |
|-------|---------|---------|
| `scanId` | UUIDv4 | `123e4567-e89b-12d3-a456-426614174000` |
| `siteId` | SHA-256 (64 chars hex) | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| `consentGiven` | boolean (DEVE ser `true`) | `true` |
| `scanTimestampUtc` | ISO 8601 UTC | `2025-10-20T21:20:00Z` |
| `scanDurationMs` | integer >= 0 | `1500` |
| `scannerVersion` | SemVer | `1.0.0-mvp` |
| `environment.wpVersion` | string | `6.4.0` |
| `environment.phpVersion` | string | `8.2.0` |
| `results` | array (min 1 item) | `[{...}]` |

### ❌ Erros Comuns

1. **UUID Inválido**
   ```json
   {
     "scanId": "123-abc" // ❌ Não é UUIDv4
   }
   ```
   ✅ Correto: `"123e4567-e89b-12d3-a456-426614174000"`

2. **Timestamp sem "Z"**
   ```json
   {
     "scanTimestampUtc": "2025-10-20T21:20:00" // ❌ Falta o Z
   }
   ```
   ✅ Correto: `"2025-10-20T21:20:00Z"`

3. **Consentimento Falso**
   ```json
   {
     "consentGiven": false // ❌ Retorna HTTP 403
   }
   ```
   ✅ Correto: `"consentGiven": true`

4. **Versão Inválida**
   ```json
   {
     "scannerVersion": "v1.0" // ❌ Não é SemVer
   }
   ```
   ✅ Correto: `"1.0.0-mvp"`

---

## 🎨 Recursos da UI

### 1. Try It Out
Clique no botão **"Try it out"** para:
- Editar o payload de exemplo
- Adicionar headers personalizados
- Executar requisições reais

### 2. Schema
Clique em **"Schema"** para ver:
- Tipos de dados de cada campo
- Campos obrigatórios vs opcionais
- Padrões de validação (regex)

### 3. Examples
Clique em **"Examples"** para:
- Ver exemplos prontos de payloads
- Copiar e colar rapidamente

### 4. Responses
Veja todas as respostas possíveis:
- **200**: Sucesso (received ou registered)
- **400**: Payload inválido
- **401**: Token inválido
- **403**: Sem consentimento
- **429**: Rate limit excedido
- **500**: Erro no servidor

---

## 🔧 Para Desenvolvedores Frontend

### Gerando Client Automaticamente

Use o OpenAPI JSON para gerar clients automaticamente:

#### TypeScript (usando openapi-generator)

```bash
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-axios \
  -o ./src/api/radarlgpd
```

#### JavaScript (usando swagger-js)

```javascript
import SwaggerClient from 'swagger-js';

const client = await SwaggerClient('http://localhost:8080/v3/api-docs');

// Fazer requisição
const response = await client.apis.Telemetria.receiveScanResult({
  requestBody: {
    scanId: '123e4567-e89b-12d3-a456-426614174000',
    siteId: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    consentGiven: true,
    // ... resto do payload
  }
});
```

#### Python (usando openapi-python-client)

```bash
openapi-python-client generate \
  --url http://localhost:8080/v3/api-docs \
  --output-path ./radarlgpd_client
```

---

## 🧪 Testando Rate Limiting

O Swagger UI permite testar o rate limit:

1. Configure o token de autenticação
2. Execute 101 requisições seguidas (pode usar scripts)
3. A 101ª requisição retornará **HTTP 429**

```json
{
  "timestamp": "2025-10-20T21:20:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit excedido. Máximo de 100 requisições por hora.",
  "path": "/v1/telemetry/scan-result"
}
```

---

## 📊 Exportando a Documentação

### JSON
```bash
curl http://localhost:8080/v3/api-docs > openapi.json
```

### YAML
```bash
curl http://localhost:8080/v3/api-docs.yaml > openapi.yaml
```

### Importar no Postman

1. Abra o Postman
2. File → Import
3. Cole a URL: `http://localhost:8080/v3/api-docs`
4. ✅ Toda a API será importada automaticamente!

---

## 🛡️ Segurança

### ⚠️ IMPORTANTE: Desabilitar Swagger em Produção (Opcional)

Se você quiser **desabilitar o Swagger em produção**, adicione em `application-prod.properties`:

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

**Recomendação**: Mantenha habilitado, mas proteja com autenticação adicional se necessário.

---

## 🆘 Troubleshooting

### Swagger UI não carrega

**Problema**: Erro 404 ao acessar `/swagger-ui.html`

**Solução**:
1. Verifique se a aplicação está rodando
2. Tente acessar `/swagger-ui/index.html`
3. Verifique os logs: `logging.level.org.springdoc=DEBUG`

### "Failed to fetch" nos exemplos

**Problema**: CORS bloqueando requisições

**Solução**: Verifique a configuração CORS em `CorsConfig.java`

### Autenticação não funciona

**Problema**: Token não é enviado

**Solução**:
1. Clique em **🔓 Authorize** no topo
2. Cole o token **sem** o prefixo "Bearer"
3. Clique em "Authorize"
4. Verifique se aparece **🔒 (authorized)** ao lado do método

---

## 📚 Referências

- [SpringDoc OpenAPI 3](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

---

## 💡 Dicas Extras

### 1. Validação em Tempo Real

O Swagger UI valida o payload antes de enviar:
- Campos obrigatórios em vermelho
- Formatos inválidos destacados
- Sugestões de correção

### 2. Copiar cURL

Clique em **"Copy as cURL"** para obter o comando curl completo:

```bash
curl -X 'POST' \
  'http://localhost:8080/v1/telemetry/scan-result' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer 9f8c7b6a-5d4e-3c2b-1a0f-9e8d7c6b5a4f' \
  -H 'Content-Type: application/json' \
  -d '{
  "scanId": "123e4567-e89b-12d3-a456-426614174000",
  ...
}'
```

### 3. Persistência de Autenticação

O Swagger UI lembra do seu token entre reloads (configurado via `persist-authorization=true`).

---

**Boa integração! 🚀**
