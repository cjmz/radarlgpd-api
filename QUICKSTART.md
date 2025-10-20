# 🚀 Quick Start - Radar LGPD

Guia rápido para colocar o projeto rodando em **menos de 5 minutos**!

## Pré-requisitos

Certifique-se de ter instalado:

- ✅ Java 21
- ✅ Maven (ou use o `./mvnw` incluído)
- ✅ Docker e Docker Compose

## Passo a Passo

### 1️⃣ Iniciar o Banco de Dados

```bash
# Inicia o PostgreSQL em Docker
docker compose up -d postgres

# Confirma que está rodando
docker compose ps
```

Você deve ver:
```
NAME                  STATUS
radarlgpd-postgres    Up
```

### 2️⃣ Configurar Variáveis de Ambiente

```bash
# Copia o arquivo de configuração de exemplo
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
```

✅ **Nada mais precisa ser alterado!** O arquivo já vem configurado para Docker.

### 3️⃣ Rodar a Aplicação

```bash
# Compila e executa
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 4️⃣ Testar

A API estará rodando em: **http://localhost:8080**

Teste o health check:

```bash
curl http://localhost:8080/actuator/health
```

Resposta esperada:
```json
{"status":"UP"}
```

## 🎯 Teste Completo (Opcional)

Teste o endpoint principal:

```bash
curl -X POST http://localhost:8080/v1/telemetry/scan-result \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer dev-api-key-change-in-production" \
  -d '{
    "scan_id": "a45b1234-44f5-91f1-c841e7e4813c",
    "site_id": "abc123def456",
    "consent_given": true,
    "scan_timestamp_utc": "2025-01-20T10:30:00Z",
    "scan_duration_ms": 15000,
    "scanner_version": "1.0.0",
    "environment": {
      "wp_version": "6.4.2",
      "php_version": "8.2.0",
      "db_engine": "MySQL",
      "db_version": "8.0.35"
    },
    "results": [
      {
        "data_type": "CPF",
        "source_location": "wp_users.user_meta",
        "count": 152
      },
      {
        "data_type": "EMAIL",
        "source_location": "wp_comments.comment_author_email",
        "count": 3421
      }
    ]
  }'
```

## 📊 Ferramentas Úteis

### Ver logs do banco de dados

```bash
./scripts/db-helper.sh logs
```

### Conectar ao PostgreSQL

```bash
./scripts/db-helper.sh connect
```

### Parar tudo

```bash
# Para o banco (mantém os dados)
docker compose stop

# Para a aplicação
Ctrl + C no terminal onde está rodando
```

### Recomeçar

```bash
# Reinicia o banco
docker compose start postgres

# Reinicia a aplicação
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## 🐛 Problemas Comuns

### "Porta 5432 já está em uso"

Você já tem PostgreSQL instalado? Pare-o primeiro:

```bash
# Linux
sudo systemctl stop postgresql

# macOS
brew services stop postgresql
```

### "Connection refused" ao tentar conectar no banco

Aguarde alguns segundos para o container inicializar:

```bash
# Veja se está pronto
docker exec -it radarlgpd-postgres pg_isready
```

### Resetar tudo do zero

```bash
# CUIDADO: Apaga TODOS os dados!
docker compose down -v
docker compose up -d postgres
```

## 📚 Próximos Passos

- 📖 Leia a [documentação completa do banco](docs/SETUP_DATABASE.md)
- 📋 Veja o [README principal](README.md) para mais detalhes da API
- 🔧 Consulte as [instruções do Copilot](.github/copilot-instructions.md)

## 🆘 Ajuda

Todos os comandos disponíveis do helper:

```bash
./scripts/db-helper.sh
```

---

**Tudo funcionando?** 🎉 Agora você está pronto para desenvolver!
