# Radar LGPD - API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 📋 Sobre o Projeto

O **Radar LGPD** é uma solução desenvolvida para auxiliar micro e pequenos empreendedores que utilizam plataformas como WordPress, WooCommerce e similares a verificarem se seus sites estão em conformidade com a **Lei Geral de Proteção de Dados (LGPD)** brasileira.

Esta API Spring Boot serve como backend para receber e persistir os resultados de varreduras realizadas por um plugin WordPress (desenvolvido separadamente), que analisa o banco de dados do cliente em busca de possíveis dados pessoais sensíveis.

### 🎯 Objetivo

Fornecer visibilidade sobre onde e quantos dados pessoais (CPF, e-mail, telefone, etc.) estão armazenados no banco de dados WordPress, permitindo que os proprietários de sites tomem ações corretivas para garantir compliance com a LGPD.

## 🏗️ Arquitetura

### Stack Tecnológico

- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.6** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Autenticação e autorização
- **Spring Validation** - Validação de dados
- **PostgreSQL** - Banco de dados relacional
- **Lombok** - Redução de boilerplate
- **Maven** - Gerenciamento de dependências

### Fluxo de Dados

```
Plugin WordPress → Varredura DB → API Radar LGPD → PostgreSQL
                  (cliente)       (POST /v1/telemetry/scan-result)
```

## 🚀 Funcionalidades (MVP)

### Endpoint Principal

**POST /v1/telemetry/scan-result**

Recebe os resultados agregados e anonimizados de uma varredura realizada pelo scanner WordPress.

#### Exemplo de Payload

```json
{
  "scan_id": "uuid-a45b-44f5-91f1-c841e7e4813c",
  "site_id": "sha256-do-dominio-com-salt-anonimizado",
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
      "source_location": "wp_comments.comment_author_email",
      "count": 310
    }
  ]
}
```

#### Campos do Payload

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `scan_id` | String (UUID) | Identificador único da varredura |
| `site_id` | String (Hash) | Hash SHA256 do domínio + salt (anonimizado) |
| `consent_given` | Boolean | Indica se o usuário concedeu opt-in |
| `scan_timestamp_utc` | String (ISO 8601) | Data/hora UTC da conclusão da varredura |
| `scan_duration_ms` | Integer | Duração da varredura em milissegundos |
| `scanner_version` | String | Versão do plugin scanner |
| `environment.wp_version` | String | Versão do WordPress |
| `environment.php_version` | String | Versão do PHP |
| `results` | Array | Lista de dados agregados encontrados |
| `results[].data_type` | String | Tipo de dado (CPF, EMAIL, TELEFONE, etc.) |
| `results[].source_location` | String | Localização (tabela.coluna) |
| `results[].count` | Integer | Quantidade de ocorrências |

## 🔒 Requisitos Não Funcionais

### NFR-API-001: Autenticação
- Todas as requisições devem incluir uma `API-Key` no header
- Header: `Authorization: Bearer {api-key}`
- Requisições sem chave ou com chave inválida: **HTTP 401 Unauthorized**

### NFR-API-002: Rate Limiting
- Limite: **100 requisições por hora por IP**
- Excesso de requisições: **HTTP 429 Too Many Requests**

### NFR-API-003: Validação de Consentimento
- Campo `consent_given` deve ser `true`
- Se `false` ou `null`: **HTTP 403 Forbidden**
- **Não coletamos dados sem opt-in explícito**

### NFR-API-004: Validação de Anonimização
- Payload deve seguir estritamente o schema aprovado
- Campos não previstos ou dados brutos: **HTTP 400 Bad Request**
- **Aceitamos apenas contagens agregadas, nunca dados pessoais**

## 📦 Instalação e Configuração

### Pré-requisitos

- Java 21 ou superior
- Maven 3.8+
- Docker e Docker Compose (recomendado) **OU** PostgreSQL 14+
- Git

### 1. Clone o Repositório

```bash
git clone https://github.com/seu-usuario/radarlgpd-api.git
cd radarlgpd-api
```

### 2. Configure o Banco de Dados

#### 🐳 Opção A: Usando Docker (Recomendado)

```bash
# Iniciar PostgreSQL com Docker Compose
docker compose up -d postgres

# Verificar se está rodando
docker compose ps

# OU usar o script helper
./scripts/db-helper.sh start
```

Pronto! O banco já está configurado e rodando em `localhost:5432`.

📚 **Para mais detalhes, veja**: [docs/SETUP_DATABASE.md](docs/SETUP_DATABASE.md)

#### 🔧 Opção B: PostgreSQL Local (Manual)

Se preferir instalar PostgreSQL manualmente:

```sql
CREATE DATABASE radarlgpd;
CREATE USER radarlgpd_user WITH PASSWORD 'sua_senha_segura';
GRANT ALL PRIVILEGES ON DATABASE radarlgpd TO radarlgpd_user;
```

### 3. Configure as Variáveis de Ambiente

Copie o arquivo de exemplo:

```bash
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
```

Se estiver usando Docker, o arquivo já está pré-configurado! ✅

Se estiver usando PostgreSQL local, edite o arquivo e ajuste a senha:

```properties
spring.datasource.password=sua_senha_segura
```

### 4. Compile e Execute

```bash
# Compilar
./mvnw clean install

# Executar com profile local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

A API estará disponível em: `http://localhost:8080`

## 🧪 Testando a API

### Teste com cURL

```bash
curl -X POST http://localhost:8080/v1/telemetry/scan-result \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer sua-api-key-super-secreta-aqui" \
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
      }
    ]
  }'
```

### Respostas Esperadas

- **200 OK**: Scan recebido e armazenado com sucesso
- **400 Bad Request**: Payload inválido ou campos não permitidos
- **401 Unauthorized**: API Key ausente ou inválida
- **403 Forbidden**: Consentimento não concedido (`consent_given: false`)
- **429 Too Many Requests**: Rate limit excedido

## 📁 Estrutura do Projeto

```
radarlgpd/
├── src/
│   ├── main/
│   │   ├── java/com/br/radarlgpd/radarlgpd/
│   │   │   ├── config/           # Configurações (Security, Rate Limit)
│   │   │   ├── controller/       # Controllers REST
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entity/           # Entidades JPA
│   │   │   ├── exception/        # Exceções customizadas
│   │   │   ├── repository/       # Repositories JPA
│   │   │   ├── service/          # Lógica de negócio
│   │   │   └── RadarlgpdApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-local.properties
│   └── test/
│       └── java/com/br/radarlgpd/radarlgpd/
├── .github/
│   └── copilot-instructions.md   # Instruções para GitHub Copilot
├── pom.xml
└── README.md
```

## 🔐 Segurança e Privacidade (LGPD Compliance)

### Princípios Seguidos

1. **Minimização de Dados**: Coletamos apenas contagens agregadas, nunca dados pessoais
2. **Anonimização**: IDs de sites são hasheados com salt
3. **Consentimento**: Dados só são aceitos com opt-in explícito
4. **Transparência**: Todos os dados coletados estão documentados
5. **Finalidade**: Dados usados exclusivamente para análise de compliance

### Dados que NÃO coletamos

- ❌ CPFs, CNPJs, e-mails, telefones reais
- ❌ Nomes de usuários ou clientes
- ❌ Domínios em texto claro
- ❌ Conteúdo de posts ou comentários
- ❌ IPs de visitantes do site do cliente

### Dados que coletamos

- ✅ Contagens agregadas de tipos de dados
- ✅ Metadados técnicos (versão WP/PHP)
- ✅ Hash anonimizado do site
- ✅ Timestamp das varreduras

## 🗺️ Roadmap

- [x] MVP: Endpoint de telemetria básico
- [ ] Dashboard web para visualização dos dados
- [ ] Análise de tendências e recomendações
- [ ] Integração com outros CMSs (Joomla, Drupal)
- [ ] Relatórios de compliance automatizados
- [ ] Alertas proativos de riscos

## 🚀 Deploy

### Render (Recomendado)

A aplicação está pronta para deploy no [Render](https://render.com) com suporte a deploy automático via Blueprint.

**Guia completo**: Consulte [`docs/DEPLOY_RENDER.md`](docs/DEPLOY_RENDER.md)

**Checklist**: Veja [`DEPLOY_CHECKLIST.md`](DEPLOY_CHECKLIST.md)

**Quick Start:**
```bash
# 1. Gere uma API Key forte
openssl rand -hex 32

# 2. Push para GitHub
git push origin main

# 3. No Render Dashboard:
#    - New → Blueprint
#    - Conecte o repositório
#    - Apply
```

### Docker Local

Teste o build Docker localmente antes do deploy:

```bash
# Executar script de teste
./scripts/test-docker-build.sh

# Ou manualmente:
docker build -t radarlgpd-api .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e RADARLGPD_API_KEY=sua-chave \
  radarlgpd-api
```

## 🤝 Contribuindo

Contribuições são bem-vindas! Por favor:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 📞 Contato

Para dúvidas ou sugestões:

- Email: contato@radarlgpd.com.br
- Issues: [GitHub Issues](https://github.com/seu-usuario/radarlgpd-api/issues)

## 🙏 Agradecimentos

Desenvolvido com ❤️ para ajudar pequenos negócios a estarem em conformidade com a LGPD.

---

**Nota**: Este é um projeto MVP. Funcionalidades adicionais serão adicionadas conforme feedback e necessidades do mercado.
