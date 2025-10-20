# 🚀 Guia de Deploy no Render

## 📋 Pré-requisitos

- [ ] Conta no [Render](https://render.com) (gratuita)
- [ ] Repositório Git com o código (GitHub, GitLab ou Bitbucket)
- [ ] Código commitado e pushed para o repositório

## 🎯 Opções de Deploy

### Opção 1: Deploy Automático via Blueprint (Recomendado)

O arquivo `render.yaml` configura tudo automaticamente.

#### Passos:

1. **Acesse o Render Dashboard**
   - Vá para https://dashboard.render.com

2. **New → Blueprint**
   - Clique em "New +" no topo
   - Selecione "Blueprint"

3. **Conecte o Repositório**
   - Conecte sua conta GitHub/GitLab
   - Selecione o repositório `radarlgpd-api`
   - Branch: `main`

4. **Render detectará o `render.yaml`**
   - Clique em "Apply" para criar:
     - Web Service (API Spring Boot)
     - PostgreSQL Database

5. **Aguarde o Deploy**
   - Build: ~5-10 minutos
   - Deploy automático após build

6. **Anote a URL**
   - Exemplo: `https://radarlgpd-api.onrender.com`

---

### Opção 2: Deploy Manual (Passo a Passo)

Se preferir controle total:

#### Passo 1: Criar o Banco de Dados

1. **New → PostgreSQL**
2. Configurações:
   - **Name**: `radarlgpd-db`
   - **Database**: `radarlgpd`
   - **User**: `radarlgpd_user`
   - **Region**: Oregon (ou Ohio para menor latência no Brasil)
   - **Plan**: Free (90 dias grátis)
3. Clique em "Create Database"
4. **Aguarde provisioning** (~2 minutos)
5. **Copie as credenciais**:
   - Internal Database URL
   - Username
   - Password

#### Passo 2: Criar o Web Service

1. **New → Web Service**
2. Conecte o repositório GitHub
3. Configurações:

   **Build & Deploy:**
   - **Name**: `radarlgpd-api`
   - **Region**: Oregon (mesmo do DB)
   - **Branch**: `main`
   - **Runtime**: Docker
   - **Dockerfile Path**: `./Dockerfile`

   **Instance Type:**
   - **Plan**: Free (ou Starter $7/mês para produção)

4. **Variáveis de Ambiente** (clique em "Advanced"):

   ```bash
   # Profile
   SPRING_PROFILES_ACTIVE=prod
   
   # Database (use os valores copiados do Passo 1)
   DATABASE_URL=<Internal Database URL do Render>
   DB_USERNAME=radarlgpd_user
   DB_PASSWORD=<password do banco>
   
   # Security (GERE UMA CHAVE FORTE!)
   RADARLGPD_API_KEY=<gere-uma-api-key-forte-aqui>
   
   # Rate Limiting
   RADARLGPD_RATE_LIMIT=100
   
   # CORS (ajuste conforme necessário)
   ALLOWED_ORIGINS=*
   ```

5. **Health Check Path**: `/health`

6. Clique em "Create Web Service"

---

## 🔐 Gerar API Key Segura

Use um dos métodos:

### Método 1: OpenSSL (Linux/Mac)
```bash
openssl rand -hex 32
```

### Método 2: Python
```python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

### Método 3: Node.js
```javascript
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

**⚠️ IMPORTANTE**: Salve esta chave em local seguro! O plugin WordPress precisará dela.

---

## ✅ Verificar Deploy

### 1. Health Check

```bash
curl https://radarlgpd-api.onrender.com/health
```

**Resposta esperada:**
```json
{
  "status": "UP",
  "timestamp": "2025-10-20T18:30:00",
  "service": "Radar LGPD API",
  "version": "0.0.1-SNAPSHOT"
}
```

### 2. Testar Endpoint Principal

```bash
curl -X POST https://radarlgpd-api.onrender.com/v1/telemetry/scan-result \
  -H "Authorization: Bearer SUA-API-KEY-AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "scan_id": "123e4567-e89b-12d3-a456-426614174000",
    "site_id": "site-12345",
    "consent_given": true,
    "scan_timestamp_utc": "2025-10-20T18:30:00Z",
    "scan_duration_ms": 1500,
    "scanner_version": "1.0.0",
    "environment": {
      "wordpress_version": "6.4.0",
      "php_version": "8.2.0",
      "mysql_version": "8.0.35"
    },
    "results": [
      {
        "data_type": "CPF",
        "source_location": "wp_comments.comment_content",
        "count": 10
      }
    ]
  }'
```

**Resposta esperada (200 OK):**
```json
{
  "scan_id": "123e4567-e89b-12d3-a456-426614174000",
  "status": "received",
  "message": "Dados recebidos e armazenados com sucesso"
}
```

---

## 🔍 Monitoramento

### Logs em Tempo Real

No Render Dashboard:
1. Acesse seu Web Service
2. Clique na aba "Logs"
3. Veja logs em tempo real

### Métricas

- **CPU/Memory**: Aba "Metrics"
- **Deploy History**: Aba "Events"
- **Health Checks**: Status na dashboard

---

## ⚙️ Configurações Adicionais

### Auto-Deploy

Render faz deploy automático quando você faz push para a branch `main`:

```bash
git add .
git commit -m "feat: adiciona nova funcionalidade"
git push origin main
# Deploy automático será iniciado
```

### Custom Domain

1. Vá em Settings → Custom Domain
2. Adicione seu domínio (ex: `api.radarlgpd.com`)
3. Configure DNS conforme instruções
4. SSL automático via Let's Encrypt

### Escalar (Upgrade do Free Plan)

Free tier limites:
- 750 horas/mês (suficiente para testes)
- Sleep após 15 min de inatividade
- 512 MB RAM

Para produção, considere:
- **Starter Plan**: $7/mês
  - Sem sleep
  - 512 MB RAM
  - Deploy mais rápido

---

## 🐛 Troubleshooting

### Build Falha

**Erro**: `Failed to build`

**Solução**:
```bash
# Teste o build localmente
docker build -t radarlgpd-api .
docker run -p 8080:8080 radarlgpd-api
```

### Health Check Failing

**Erro**: `Health check failing`

**Soluções**:
1. Verifique se `PORT` está configurada corretamente
2. Verifique logs: "Health check returned status code 404"
3. Teste localmente:
   ```bash
   curl http://localhost:8080/health
   ```

### Database Connection Failed

**Erro**: `Failed to configure DataSource`

**Soluções**:
1. Verifique variáveis `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`
2. Certifique-se que o DB está no mesmo region
3. Verifique IP allowlist do banco (deve estar vazio)

### Out of Memory

**Erro**: `OutOfMemoryError`

**Solução**: Upgrade para Starter plan ou otimize:
```properties
# application-prod.properties
spring.datasource.hikari.maximum-pool-size=3
```

---

## 📊 Custos

### Free Tier
- **Web Service**: 750 horas/mês (suficiente para 1 serviço)
- **PostgreSQL**: 90 dias grátis, depois $7/mês
- **Total primeiros 90 dias**: $0
- **Após 90 dias**: $7/mês (apenas DB)

### Produção Recomendada
- **Web Service Starter**: $7/mês
- **PostgreSQL**: $7/mês
- **Total**: $14/mês

---

## 🔗 Próximos Passos

Após deploy:

1. ✅ Configure o plugin WordPress com a URL do Render
2. ✅ Configure a API Key no plugin
3. ✅ Teste uma varredura completa
4. ✅ Monitore logs e métricas
5. ✅ Configure alertas (Render → Notifications)

---

## 📚 Recursos

- [Render Docs](https://render.com/docs)
- [Blueprint Spec](https://render.com/docs/blueprint-spec)
- [PostgreSQL no Render](https://render.com/docs/databases)
- [Custom Domains](https://render.com/docs/custom-domains)

---

## 🆘 Suporte

- **Render Support**: https://render.com/support
- **Community**: https://community.render.com
- **Status Page**: https://status.render.com

---

**✨ Pronto! Sua API Radar LGPD está no ar! 🚀**
