# ✅ Checklist de Deploy - Render

## 📦 Arquivos Criados

- [x] `Dockerfile` - Configuração Docker multi-stage para produção
- [x] `render.yaml` - Blueprint para deploy automático
- [x] `application-prod.properties` - Configurações de produção
- [x] `docs/DEPLOY_RENDER.md` - Guia completo de deploy

## 🔍 Pré-Deploy

### Código
- [x] Application compila sem erros (`mvn clean package`)
- [x] Testes passam (`mvn test`)
- [x] Health check endpoint funciona (`/health`)
- [x] API endpoint funciona (`/v1/telemetry/scan-result`)
- [x] Validações estão implementadas
- [x] Autenticação via API Key funciona
- [x] Rate limiting está implementado

### Configuração
- [x] `application-prod.properties` usa variáveis de ambiente
- [x] Nenhuma senha hardcoded no código
- [x] Profile `prod` configurado
- [x] PORT dinâmica suportada (`${PORT}`)
- [x] Logging otimizado para produção

### Docker
- [x] `Dockerfile` multi-stage para build otimizado
- [x] Healthcheck configurado
- [x] Usuário não-root para segurança
- [x] `.dockerignore` otimizado

## 🚀 Deploy no Render

### Via Blueprint (Recomendado)

1. [ ] Commit e push de todos os arquivos:
   ```bash
   git add .
   git commit -m "feat: adiciona configuração para deploy no Render"
   git push origin main
   ```

2. [ ] Acesse [Render Dashboard](https://dashboard.render.com)

3. [ ] New → Blueprint

4. [ ] Conecte o repositório GitHub

5. [ ] Render detecta `render.yaml` automaticamente

6. [ ] Clique em "Apply"

7. [ ] Aguarde provisioning (~5-10 minutos):
   - PostgreSQL Database será criado
   - Web Service será buildado e deployado

8. [ ] Gere uma API Key forte:
   ```bash
   openssl rand -hex 32
   ```

9. [ ] Configure a variável `RADARLGPD_API_KEY` no Render:
   - Vá em Web Service → Environment
   - Edite `RADARLGPD_API_KEY`
   - Cole a key gerada
   - Salve e redeploy

10. [ ] Anote a URL do serviço:
    ```
    https://radarlgpd-api.onrender.com
    ```

### Via Manual

Siga o guia detalhado em `docs/DEPLOY_RENDER.md`

## ✅ Pós-Deploy

### Testes Básicos

1. [ ] Health check funciona:
   ```bash
   curl https://radarlgpd-api.onrender.com/health
   ```
   Deve retornar: `{"status": "UP", ...}`

2. [ ] Endpoint raiz funciona:
   ```bash
   curl https://radarlgpd-api.onrender.com/
   ```

3. [ ] Autenticação funciona:
   ```bash
   # Sem API Key - deve retornar 401
   curl -X POST https://radarlgpd-api.onrender.com/v1/telemetry/scan-result
   
   # Com API Key - deve processar
   curl -X POST https://radarlgpd-api.onrender.com/v1/telemetry/scan-result \
     -H "Authorization: Bearer SUA-API-KEY" \
     -H "Content-Type: application/json" \
     -d @test-payload.json
   ```

4. [ ] Banco de dados persiste dados:
   - Envie um scan result
   - Verifique logs para confirmar persistência

### Monitoramento

5. [ ] Configure alertas no Render:
   - Settings → Notifications
   - Email on deploy failures
   - Email on health check failures

6. [ ] Verifique métricas:
   - CPU usage
   - Memory usage
   - Response time

7. [ ] Monitore logs:
   - Erros inesperados
   - Conexões ao banco
   - Requests processados

## 🔐 Segurança

- [ ] API Key é forte e única (32+ caracteres)
- [ ] API Key está armazenada como Secret no Render
- [ ] Nenhuma senha no repositório Git
- [ ] CORS configurado apropriadamente
- [ ] Rate limiting ativo

## 📊 Configuração do Plugin WordPress

- [ ] Configure URL da API no plugin: `https://radarlgpd-api.onrender.com`
- [ ] Configure API Key no plugin
- [ ] Teste conexão do plugin com a API
- [ ] Execute uma varredura de teste

## 🎯 Otimizações Futuras

- [ ] Configure custom domain (ex: `api.radarlgpd.com`)
- [ ] Upgrade para Starter plan se necessário (sem sleep)
- [ ] Configure backup automático do PostgreSQL
- [ ] Implemente logs estruturados (JSON)
- [ ] Configure APM (Application Performance Monitoring)
- [ ] Adicione testes E2E automatizados

## 📚 Documentação

- [ ] Atualize README.md com URL de produção
- [ ] Documente variáveis de ambiente necessárias
- [ ] Crie guia para desenvolvedores
- [ ] Documente processo de rollback

## 🐛 Troubleshooting

Se algo der errado, consulte:
- `docs/DEPLOY_RENDER.md` - Seção Troubleshooting
- Logs do Render (Dashboard → Logs)
- Status do Render: https://status.render.com

## 💰 Custos

**Primeiros 90 dias:**
- Web Service (Free): $0
- PostgreSQL (Free trial): $0
- **Total**: $0

**Após 90 dias:**
- Web Service (Free): $0 (750h/mês)
- PostgreSQL: $7/mês
- **Total**: $7/mês

**Produção:**
- Web Service (Starter): $7/mês
- PostgreSQL: $7/mês
- **Total**: $14/mês

---

## 🎉 Status Final

**O projeto ESTÁ PRONTO para deploy no Render!** ✅

Todos os arquivos necessários foram criados e configurados corretamente.

**Próximo passo**: Execute o deploy seguindo o guia em `docs/DEPLOY_RENDER.md`
