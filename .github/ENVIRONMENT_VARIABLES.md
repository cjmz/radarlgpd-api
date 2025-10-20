# Variáveis de Ambiente - Radar LGPD

Este arquivo documenta as variáveis de ambiente necessárias para executar a aplicação em diferentes ambientes.

## Desenvolvimento Local

Crie um arquivo `application-local.properties` em `src/main/resources/`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/radarlgpd
spring.datasource.username=radarlgpd_user
spring.datasource.password=sua_senha_local

# Security
radarlgpd.api.key=dev-api-key-insegura-so-para-dev

# Rate Limiting
radarlgpd.rate-limit.requests-per-hour=100
```

Execute com:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Produção

### Variáveis de Ambiente Obrigatórias

```bash
# Database
export RADARLGPD_DB_URL="jdbc:postgresql://seu-host:5432/radarlgpd"
export RADARLGPD_DB_USERNAME="radarlgpd_user"
export RADARLGPD_DB_PASSWORD="senha_super_segura"

# Security
export RADARLGPD_API_KEY="api-key-complexa-e-segura-gerada-com-uuid"

# Rate Limiting
export RADARLGPD_RATE_LIMIT_REQUESTS_PER_HOUR="100"

# Spring Profile
export SPRING_PROFILES_ACTIVE="prod"
```

### Usando Docker

```bash
docker run -d \
  -e RADARLGPD_DB_URL="jdbc:postgresql://db:5432/radarlgpd" \
  -e RADARLGPD_DB_USERNAME="radarlgpd_user" \
  -e RADARLGPD_DB_PASSWORD="senha" \
  -e RADARLGPD_API_KEY="sua-api-key" \
  -e SPRING_PROFILES_ACTIVE="prod" \
  -p 8080:8080 \
  radarlgpd-api:latest
```

### Usando Kubernetes (ConfigMap/Secret)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: radarlgpd-secrets
type: Opaque
stringData:
  db-password: "senha_segura"
  api-key: "api-key-super-secreta"
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: radarlgpd-config
data:
  DB_URL: "jdbc:postgresql://postgres-service:5432/radarlgpd"
  DB_USERNAME: "radarlgpd_user"
  RATE_LIMIT: "100"
```

## Testes

Para testes automatizados, use um profile específico:

```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
radarlgpd.api.key=test-api-key
```

Execute testes com:
```bash
./mvnw test -Dspring.profiles.active=test
```

## CI/CD (GitHub Actions)

Exemplo de configuração no `.github/workflows/deploy.yml`:

```yaml
env:
  RADARLGPD_DB_URL: ${{ secrets.DB_URL }}
  RADARLGPD_DB_USERNAME: ${{ secrets.DB_USERNAME }}
  RADARLGPD_DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
  RADARLGPD_API_KEY: ${{ secrets.API_KEY }}
```

## Geração de API Key Segura

Use um destes métodos para gerar uma API Key segura:

```bash
# Método 1: OpenSSL
openssl rand -base64 32

# Método 2: UUID v4
uuidgen

# Método 3: Python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

## Checklist de Segurança

- [ ] Nunca commite senhas ou API keys no Git
- [ ] Use `.gitignore` para excluir `application-local.properties`
- [ ] Rotacione API keys periodicamente
- [ ] Use secrets managers em produção (AWS Secrets Manager, HashiCorp Vault, etc.)
- [ ] Limite permissões de banco de dados ao mínimo necessário
- [ ] Use HTTPS/TLS em produção
- [ ] Configure firewall para limitar acesso ao banco de dados

## Variáveis Opcionais

```bash
# Logging
export LOGGING_LEVEL_ROOT="INFO"
export LOGGING_LEVEL_APP="DEBUG"

# Server
export SERVER_PORT="8080"

# CORS (se necessário)
export RADARLGPD_CORS_ORIGINS="https://seudominio.com.br"
```

## Troubleshooting

### Erro de conexão com banco

Verifique se as variáveis estão corretas:
```bash
echo $RADARLGPD_DB_URL
echo $RADARLGPD_DB_USERNAME
# Não faça echo da senha em produção!
```

### API Key não funciona

Certifique-se de que está usando o formato correto:
```
Authorization: Bearer sua-api-key-aqui
```

### Rate limit muito restritivo

Ajuste a variável:
```bash
export RADARLGPD_RATE_LIMIT_REQUESTS_PER_HOUR="500"
```
