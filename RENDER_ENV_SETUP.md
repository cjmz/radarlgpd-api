# 🚀 Configuração de Variáveis de Ambiente - Render

## ⚠️ ERRO COMUM: APPLICATION FAILED TO START

Se você viu o erro:
```
Failed to configure a DataSource: 'url' attribute is not specified
```

**Causa**: Variáveis de ambiente do banco de dados não foram configuradas.

---

## ✅ Solução: Configurar Variáveis no Render

### Passo a Passo:

1. **Acesse o Render Dashboard**
   - https://dashboard.render.com

2. **Selecione seu Web Service**
   - `radarlgpd-api` (ou nome do seu serviço)

3. **Vá em Environment**
   - Menu lateral → **Environment**

4. **Adicione TODAS estas variáveis:**

---

## 📋 Variáveis Obrigatórias

### 1. SPRING_PROFILES_ACTIVE

```
Key: SPRING_PROFILES_ACTIVE
Value: prod
```

---

### 2. DATABASE_URL

```
Key: DATABASE_URL
Value: jdbc:postgresql://dpg-d3r6ki3e5dus73b2pc5g-a.oregon-postgres.render.com:5432/radarlgpd_db
```

⚠️ **IMPORTANTE**: Use o formato JDBC (`jdbc:postgresql://...`), não o formato nativo do Postgres!

**Conversão:**
- ❌ Errado: `postgresql://user:pass@host/db`
- ✅ Correto: `jdbc:postgresql://host:5432/db`

---

### 3. DB_USERNAME

```
Key: DB_USERNAME
Value: radarlgpd_db_user
```

---

### 4. DB_PASSWORD

```
Key: DB_PASSWORD
Value: mGpaaPrJyVfQwr3iF8mcpd9cEWrRihaY
```

---

### 5. RADARLGPD_API_KEY ⚠️ CRÍTICO

Gere uma chave segura:

```bash
openssl rand -hex 32
```

Exemplo de saída:
```
a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456
```

Configure no Render:
```
Key: RADARLGPD_API_KEY
Value: <cole-a-key-gerada-acima>
```

---

### 6. RADARLGPD_RATE_LIMIT (Opcional)

```
Key: RADARLGPD_RATE_LIMIT
Value: 100
```

---

### 7. ALLOWED_ORIGINS (Opcional)

```
Key: ALLOWED_ORIGINS
Value: *
```

(Ajuste para domínios específicos em produção)

---

## 🎯 Resumo - Copiar e Colar

### Variáveis Completas:

```plaintext
SPRING_PROFILES_ACTIVE=prod

DATABASE_URL=jdbc:postgresql://dpg-d3r6ki3e5dus73b2pc5g-a.oregon-postgres.render.com:5432/radarlgpd_db

DB_USERNAME=radarlgpd_db_user

DB_PASSWORD=mGpaaPrJyVfQwr3iF8mcpd9cEWrRihaY

RADARLGPD_API_KEY=<GERAR COM: openssl rand -hex 32>

RADARLGPD_RATE_LIMIT=100

ALLOWED_ORIGINS=*
```

---

## 🔄 Após Configurar

1. **Salve as mudanças** no Render
2. **Aguarde o redeploy automático** (~5 minutos)
3. **Verifique os logs** para confirmar sucesso

---

## ✅ Logs de Sucesso

Após o deploy, você deve ver:

```
INFO  --- Started RadarlgpdApplication in X.XXX seconds
INFO  --- Tomcat started on port 10000 (http)
```

---

## 🧪 Testar a API

```bash
# 1. Health check
curl https://radarlgpd-api.onrender.com/health

# Resposta esperada:
# {"status":"UP"}

# 2. Teste endpoint principal (substitua {API_KEY})
curl -X POST https://radarlgpd-api.onrender.com/v1/telemetry/scan-result \
  -H "Authorization: Bearer {API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "scan_id": "123e4567-e89b-12d3-a456-426614174000",
    "site_id": "test-site",
    "consent_given": true,
    "scan_timestamp_utc": "2025-10-20T19:00:00Z",
    "scan_duration_ms": 100,
    "scanner_version": "1.0.0",
    "environment": {
      "wordpress_version": "6.4.0",
      "php_version": "8.2.0"
    },
    "results": [
      {
        "data_type": "EMAIL",
        "source_location": "wp_users.user_email",
        "count": 1
      }
    ]
  }'
```

---

## ❓ Troubleshooting

### Erro: "Failed to determine a suitable driver class"

**Solução**: Verifique se `DATABASE_URL` está no formato JDBC correto:
- ✅ `jdbc:postgresql://host:5432/database`
- ❌ `postgresql://host/database`

### Erro: "Access denied for user"

**Solução**: Verifique se `DB_USERNAME` e `DB_PASSWORD` estão corretos.

### Erro: "Could not connect to database"

**Solução**: 
- Verifique se o hostname está correto
- Confirme que o banco PostgreSQL está rodando no Render

### Erro: "API Key inválida"

**Solução**: Gere uma nova key com `openssl rand -hex 32` e configure novamente.

---

## 📝 Checklist Final

Antes de fazer deploy:

- [ ] `SPRING_PROFILES_ACTIVE=prod` configurado
- [ ] `DATABASE_URL` no formato JDBC configurado
- [ ] `DB_USERNAME` configurado
- [ ] `DB_PASSWORD` configurado
- [ ] `RADARLGPD_API_KEY` gerada e configurada
- [ ] Código commitado e pushed para GitHub
- [ ] Redeploy iniciado no Render
- [ ] Logs verificados (sem erros)
- [ ] Health check testado e funcionando

---

**Última atualização**: 20/10/2025
