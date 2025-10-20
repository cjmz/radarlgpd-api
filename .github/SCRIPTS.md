# Scripts de Desenvolvimento - Radar LGPD

Este diretório contém scripts úteis para desenvolvimento e deploy.

## Scripts Disponíveis

### setup-local.sh
Script para configurar o ambiente local de desenvolvimento pela primeira vez.

```bash
#!/bin/bash

echo "🚀 Configurando ambiente local do Radar LGPD..."

# Verificar se Java 21 está instalado
if ! command -v java &> /dev/null; then
    echo "❌ Java não encontrado. Instale Java 21 primeiro."
    exit 1
fi

java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$java_version" -lt "21" ]; then
    echo "❌ Java 21 ou superior é necessário. Versão atual: $java_version"
    exit 1
fi

echo "✅ Java $java_version encontrado"

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven não encontrado. Instale Maven 3.8+ primeiro."
    exit 1
fi

echo "✅ Maven encontrado"

# Verificar PostgreSQL
if ! command -v psql &> /dev/null; then
    echo "⚠️  PostgreSQL não encontrado. Certifique-se de instalá-lo."
fi

# Criar arquivo de configuração local
if [ ! -f "src/main/resources/application-local.properties" ]; then
    echo "📝 Criando application-local.properties..."
    cp src/main/resources/application-local.properties.example \
       src/main/resources/application-local.properties
    echo "✅ Arquivo criado. Edite-o com suas credenciais antes de executar."
else
    echo "ℹ️  application-local.properties já existe."
fi

# Limpar e instalar dependências
echo "📦 Instalando dependências..."
./mvnw clean install -DskipTests

echo ""
echo "✅ Setup concluído!"
echo ""
echo "Próximos passos:"
echo "1. Configure o PostgreSQL e crie o banco 'radarlgpd'"
echo "2. Edite src/main/resources/application-local.properties"
echo "3. Execute: ./mvnw spring-boot:run -Dspring-boot.run.profiles=local"
```

### run-tests.sh
Executa todos os testes com coverage.

```bash
#!/bin/bash

echo "🧪 Executando testes do Radar LGPD..."

./mvnw clean test

if [ $? -eq 0 ]; then
    echo "✅ Todos os testes passaram!"
else
    echo "❌ Alguns testes falharam. Verifique os logs acima."
    exit 1
fi
```

### build-and-run.sh
Builda e executa a aplicação localmente.

```bash
#!/bin/bash

echo "🏗️  Building Radar LGPD..."

./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build falhou!"
    exit 1
fi

echo "✅ Build concluído!"
echo "🚀 Iniciando aplicação..."

java -jar -Dspring.profiles.active=local target/radarlgpd-*.jar
```

### create-db.sh
Cria o banco de dados PostgreSQL.

```bash
#!/bin/bash

echo "🗄️  Criando banco de dados Radar LGPD..."

# Configurações (ajuste conforme necessário)
DB_NAME="radarlgpd"
DB_USER="radarlgpd_user"
DB_PASSWORD="dev_password"

# Criar banco de dados
psql -U postgres <<EOF
CREATE DATABASE $DB_NAME;
CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
\c $DB_NAME
GRANT ALL ON SCHEMA public TO $DB_USER;
EOF

if [ $? -eq 0 ]; then
    echo "✅ Banco de dados criado com sucesso!"
    echo "   Nome: $DB_NAME"
    echo "   Usuário: $DB_USER"
    echo "   Senha: $DB_PASSWORD"
else
    echo "❌ Erro ao criar banco de dados."
    exit 1
fi
```

### docker-build.sh
Builda a imagem Docker da aplicação.

```bash
#!/bin/bash

echo "🐳 Building Docker image..."

# Nome da imagem
IMAGE_NAME="radarlgpd-api"
IMAGE_TAG="latest"

# Build
docker build -t $IMAGE_NAME:$IMAGE_TAG .

if [ $? -eq 0 ]; then
    echo "✅ Imagem Docker criada: $IMAGE_NAME:$IMAGE_TAG"
    echo ""
    echo "Para executar:"
    echo "docker run -p 8080:8080 \\"
    echo "  -e RADARLGPD_DB_URL=jdbc:postgresql://host.docker.internal:5432/radarlgpd \\"
    echo "  -e RADARLGPD_DB_USERNAME=radarlgpd_user \\"
    echo "  -e RADARLGPD_DB_PASSWORD=sua_senha \\"
    echo "  -e RADARLGPD_API_KEY=sua-api-key \\"
    echo "  $IMAGE_NAME:$IMAGE_TAG"
else
    echo "❌ Erro ao criar imagem Docker."
    exit 1
fi
```

### check-style.sh
Verifica o estilo do código.

```bash
#!/bin/bash

echo "🎨 Verificando estilo do código..."

./mvnw checkstyle:check

if [ $? -eq 0 ]; then
    echo "✅ Código está de acordo com o style guide!"
else
    echo "❌ Problemas de estilo encontrados. Corrija antes de commitar."
    exit 1
fi
```

## Como Usar

1. Torne os scripts executáveis:
```bash
chmod +x scripts/*.sh
```

2. Execute o script desejado:
```bash
./scripts/setup-local.sh
./scripts/run-tests.sh
```

## Hooks do Git

Você pode configurar hooks para executar automaticamente:

### pre-commit hook
```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "Executando testes antes do commit..."
./scripts/run-tests.sh

if [ $? -ne 0 ]; then
    echo "❌ Commit abortado: testes falharam"
    exit 1
fi
```

### pre-push hook
```bash
#!/bin/bash
# .git/hooks/pre-push

echo "Verificando estilo do código antes do push..."
./scripts/check-style.sh

if [ $? -ne 0 ]; then
    echo "❌ Push abortado: problemas de estilo"
    exit 1
fi
```

## Notas

- Todos os scripts assumem que você está no diretório raiz do projeto
- Ajuste variáveis de ambiente conforme necessário
- Para Windows, considere usar Git Bash ou WSL
