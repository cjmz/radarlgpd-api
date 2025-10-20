# 🐘 Setup do PostgreSQL com Docker

## Pré-requisitos

- Docker instalado ([Docker Desktop](https://www.docker.com/products/docker-desktop/) ou Docker Engine)
- Docker Compose (já vem com Docker Desktop)

## 🚀 Quick Start

### 1. Iniciar o banco de dados

```bash
# Usando docker compose diretamente
docker compose up -d postgres

# OU usando o script helper
./scripts/db-helper.sh start
```

### 2. Verificar se está rodando

```bash
docker compose ps
# OU
./scripts/db-helper.sh status
```

Você deve ver:

```
NAME                  STATUS    PORTS
radarlgpd-postgres    Up        0.0.0.0:5432->5432/tcp
```

### 3. Configurar application-local.properties

Copie o arquivo de exemplo:

```bash
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
```

O arquivo já está configurado para conectar ao Docker:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/radarlgpd
spring.datasource.username=radarlgpd_user
spring.datasource.password=radarlgpd_dev_password
```

### 4. Rodar a aplicação Spring Boot

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## 🛠️ Comandos Úteis

### Usando o Script Helper

```bash
# Iniciar banco
./scripts/db-helper.sh start

# Ver logs em tempo real
./scripts/db-helper.sh logs

# Conectar via psql
./scripts/db-helper.sh connect

# Parar banco (mantém dados)
./scripts/db-helper.sh stop

# Reiniciar banco
./scripts/db-helper.sh restart

# Criar backup
./scripts/db-helper.sh backup

# Restaurar backup
./scripts/db-helper.sh restore ./backups/radarlgpd_backup_20250120_143022.sql

# Reset completo (CUIDADO: apaga tudo!)
./scripts/db-helper.sh reset
```

### Usando Docker Compose diretamente

```bash
# Iniciar todos os serviços (PostgreSQL + PgAdmin)
docker compose up -d

# Iniciar apenas o PostgreSQL
docker compose up -d postgres

# Ver logs
docker compose logs -f postgres

# Parar tudo
docker compose down

# Parar e remover volumes (APAGA OS DADOS!)
docker compose down -v

# Ver status
docker compose ps
```

## 🖥️ PgAdmin (Interface Gráfica)

O `docker compose.yml` inclui o PgAdmin para visualizar o banco graficamente.

### Iniciar PgAdmin

```bash
docker compose up -d pgadmin
```

### Acessar PgAdmin

1. Abra o navegador em: http://localhost:5050
2. Login:
   - Email: `admin@radarlgpd.local`
   - Senha: `admin`

### Conectar ao PostgreSQL no PgAdmin

1. Clique em "Add New Server"
2. **General Tab**:
   - Name: `Radar LGPD Local`
3. **Connection Tab**:
   - Host: `postgres` (nome do serviço no Docker)
   - Port: `5432`
   - Database: `radarlgpd`
   - Username: `radarlgpd_user`
   - Password: `radarlgpd_dev_password`
4. Clique em "Save"

## 📊 Conectar via ferramentas externas

### DBeaver / DataGrip / IntelliJ Database

- **Host**: `localhost`
- **Port**: `5432`
- **Database**: `radarlgpd`
- **Username**: `radarlgpd_user`
- **Password**: `radarlgpd_dev_password`

### Linha de comando (psql)

```bash
# Se você tem psql instalado localmente
psql -h localhost -p 5432 -U radarlgpd_user -d radarlgpd

# OU usando Docker
docker exec -it radarlgpd-postgres psql -U radarlgpd_user -d radarlgpd
```

## 🔧 Troubleshooting

### Porta 5432 já está em uso

Se você já tem PostgreSQL instalado localmente:

**Opção 1**: Pare o PostgreSQL local

```bash
# Linux
sudo systemctl stop postgresql

# macOS
brew services stop postgresql
```

**Opção 2**: Mude a porta no `docker compose.yml`

```yaml
ports:
  - "5433:5432"  # Usa porta 5433 no host
```

E atualize o `application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/radarlgpd
```

### Container não inicia

```bash
# Ver logs detalhados
docker compose logs postgres

# Verificar se o container existe
docker ps -a | grep radarlgpd

# Remover e recriar
docker compose down -v
docker compose up -d postgres
```

### Erro de conexão da aplicação Spring Boot

1. Verifique se o container está rodando:
   ```bash
   docker compose ps
   ```

2. Teste a conexão:
   ```bash
   docker exec -it radarlgpd-postgres pg_isready -U radarlgpd_user -d radarlgpd
   ```

3. Verifique as credenciais no `application-local.properties`

### Resetar completamente o banco

```bash
# Remove containers, redes E volumes (APAGA TODOS OS DADOS!)
docker compose down -v

# Reinicia do zero
docker compose up -d postgres
```

## 💾 Backup e Restore

### Backup manual

```bash
# Criar backup
docker exec -t radarlgpd-postgres pg_dump -U radarlgpd_user -d radarlgpd > backup.sql

# OU usando o helper
./scripts/db-helper.sh backup
```

### Restore manual

```bash
# Restaurar backup
docker exec -i radarlgpd-postgres psql -U radarlgpd_user -d radarlgpd < backup.sql

# OU usando o helper
./scripts/db-helper.sh restore backup.sql
```

## 🔒 Segurança

⚠️ **IMPORTANTE**: As credenciais no `docker compose.yml` são apenas para **desenvolvimento local**.

**NUNCA use essas credenciais em produção!**

Para produção:

1. Use variáveis de ambiente
2. Use secrets do Docker Swarm/Kubernetes
3. Use senhas fortes e únicas
4. Habilite SSL/TLS
5. Restrinja acesso por rede

## 📁 Estrutura de Volumes

Os dados do PostgreSQL são persistidos em um volume Docker:

```bash
# Ver volumes
docker volume ls | grep radarlgpd

# Inspecionar volume
docker volume inspect radarlgpd_postgres_data
```

Isso significa que seus dados sobrevivem quando você para/reinicia os containers!

## 🧪 Ambiente de Testes

Para rodar testes de integração com banco limpo:

```bash
# Script para CI/CD (criar em scripts/test-db.sh)
docker compose up -d postgres
sleep 5  # Aguardar banco inicializar
./mvnw clean test
docker compose down -v
```

## 📚 Referências

- [PostgreSQL Docker Official Image](https://hub.docker.com/_/postgres)
- [PgAdmin Docker](https://www.pgadmin.org/download/pgadmin-4-container/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot with PostgreSQL](https://spring.io/guides/gs/accessing-data-jpa/)

---

**Dúvidas?** Consulte a documentação ou abra uma issue no repositório.
