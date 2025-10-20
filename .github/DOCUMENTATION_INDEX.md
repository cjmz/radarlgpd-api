# 📚 Índice da Documentação - Radar LGPD

Este arquivo serve como guia de navegação para toda a documentação do projeto.

## 📖 Documentação Principal

### [README.md](../README.md)
**O que é**: Documentação principal do projeto
**Quando usar**: Primeira parada para entender o projeto
**Contém**:
- Visão geral do projeto
- Funcionalidades do MVP
- Instalação e configuração
- Exemplos de uso da API
- Estrutura do projeto
- Roadmap

---

## 🤝 Contribuição e Desenvolvimento

### [CONTRIBUTING.md](../CONTRIBUTING.md)
**O que é**: Guia completo de contribuição
**Quando usar**: Antes de criar PRs ou reportar issues
**Contém**:
- Código de conduta
- Como configurar ambiente de dev
- Padrões de código
- Processo de Pull Request
- Como reportar bugs
- Como sugerir melhorias

---

## 🤖 GitHub Copilot

### [.github/copilot-instructions.md](.github/copilot-instructions.md)
**O que é**: Instruções personalizadas para o GitHub Copilot
**Quando usar**: Configuração automática ao usar Copilot no projeto
**Contém**:
- Contexto do projeto para IA
- Stack tecnológico
- Convenções de código
- Regras de negócio críticas (LGPD)
- Padrões de segurança
- Exemplos de implementação

### [.github/code-examples.md](.github/code-examples.md)
**O que é**: Biblioteca de exemplos de código
**Quando usar**: Referência rápida ao implementar funcionalidades
**Contém**:
- DTOs completos
- Entidades JPA
- Services e Controllers
- Exception Handlers
- Security Configuration
- Testes unitários

---

## ⚙️ Configuração

### [.github/ENVIRONMENT_VARIABLES.md](.github/ENVIRONMENT_VARIABLES.md)
**O que é**: Guia completo de variáveis de ambiente
**Quando usar**: Ao configurar dev, staging ou produção
**Contém**:
- Variáveis para desenvolvimento local
- Variáveis para produção
- Como gerar API Keys seguras
- Exemplos para Docker/Kubernetes
- Troubleshooting de configuração

### [src/main/resources/application-local.properties.example](../src/main/resources/application-local.properties.example)
**O que é**: Template de configuração local
**Quando usar**: Primeiro setup do ambiente
**Contém**:
- Configurações de banco de dados
- Configurações de segurança
- Rate limiting
- Logging
- CORS

---

## 🛠️ Scripts e Automação

### [.github/SCRIPTS.md](.github/SCRIPTS.md)
**O que é**: Documentação de scripts de desenvolvimento
**Quando usar**: Para automatizar tarefas comuns
**Contém**:
- Script de setup local
- Script para rodar testes
- Script para build e deploy
- Script para criar banco de dados
- Docker build scripts
- Git hooks

---

## 📄 Outros Arquivos Importantes

### [LICENSE](../LICENSE)
**O que é**: Licença do projeto (MIT)
**Quando usar**: Para entender direitos de uso
**Contém**: Termos da licença MIT

### [.gitignore](../.gitignore)
**O que é**: Arquivos ignorados pelo Git
**Quando usar**: Configuração automática
**Contém**:
- Arquivos temporários
- Configurações locais
- Dados sensíveis
- Build artifacts

### [pom.xml](../pom.xml)
**O que é**: Configuração Maven
**Quando usar**: Gerenciamento de dependências
**Contém**:
- Dependências Spring Boot
- Plugins Maven
- Configuração Java 21

---

## 🎯 Fluxo de Trabalho Recomendado

### Para Novos Contribuidores

1. **Comece aqui**: [README.md](../README.md)
2. **Configure o ambiente**: [CONTRIBUTING.md](../CONTRIBUTING.md) → Seção "Configuração do Ambiente"
3. **Configure variáveis**: [ENVIRONMENT_VARIABLES.md](.github/ENVIRONMENT_VARIABLES.md)
4. **Use os scripts**: [SCRIPTS.md](.github/SCRIPTS.md)
5. **Consulte exemplos**: [code-examples.md](.github/code-examples.md)
6. **Siga os padrões**: [copilot-instructions.md](.github/copilot-instructions.md)

### Para Desenvolvimento de Features

1. **Veja exemplos**: [code-examples.md](.github/code-examples.md)
2. **Siga padrões**: [copilot-instructions.md](.github/copilot-instructions.md)
3. **Faça testes**: [SCRIPTS.md](.github/SCRIPTS.md) → run-tests.sh
4. **Crie PR**: [CONTRIBUTING.md](../CONTRIBUTING.md) → Seção "Pull Request"

### Para Deployment

1. **Configure env vars**: [ENVIRONMENT_VARIABLES.md](.github/ENVIRONMENT_VARIABLES.md)
2. **Build**: [SCRIPTS.md](.github/SCRIPTS.md) → docker-build.sh
3. **Valide configuração**: [application-local.properties.example](../src/main/resources/application-local.properties.example)

### Para Troubleshooting

1. **Erros de configuração**: [ENVIRONMENT_VARIABLES.md](.github/ENVIRONMENT_VARIABLES.md) → Seção "Troubleshooting"
2. **Erros de código**: [code-examples.md](.github/code-examples.md)
3. **Erros de build**: [SCRIPTS.md](.github/SCRIPTS.md)

---

## 🔍 Busca Rápida por Tópico

### Segurança e LGPD
- [copilot-instructions.md](.github/copilot-instructions.md) → Seção "Regras de Negócio Críticas"
- [code-examples.md](.github/code-examples.md) → Seção "Security Configuration"
- [CONTRIBUTING.md](../CONTRIBUTING.md) → Seção "Considerações de Segurança"

### Testes
- [copilot-instructions.md](.github/copilot-instructions.md) → Seção "Testes"
- [code-examples.md](.github/code-examples.md) → Seção "Testes"
- [CONTRIBUTING.md](../CONTRIBUTING.md) → Seção "Padrões de Código"

### API e Endpoints
- [README.md](../README.md) → Seção "Funcionalidades (MVP)"
- [code-examples.md](.github/code-examples.md) → Seção "DTOs" e "Controller"
- [copilot-instructions.md](.github/copilot-instructions.md) → Seção "Endpoint Principal"

### Banco de Dados
- [ENVIRONMENT_VARIABLES.md](.github/ENVIRONMENT_VARIABLES.md) → Seção "Database"
- [code-examples.md](.github/code-examples.md) → Seção "Entidades JPA"
- [SCRIPTS.md](.github/SCRIPTS.md) → create-db.sh

### Docker e Deploy
- [SCRIPTS.md](.github/SCRIPTS.md) → docker-build.sh
- [ENVIRONMENT_VARIABLES.md](.github/ENVIRONMENT_VARIABLES.md) → Seção "Produção"

---

## 📌 Atalhos Úteis

```bash
# Ver estrutura da documentação
tree -L 2 -I 'target|node_modules'

# Buscar em toda documentação
grep -r "palavra-chave" *.md .github/*.md

# Validar links da documentação
find . -name "*.md" -exec grep -H "http" {} \;
```

---

## 🆘 Precisa de Ajuda?

- 📖 **Documentação**: Leia este índice e navegue pelos links
- 💬 **Discussões**: [GitHub Discussions](../../discussions)
- 🐛 **Bugs**: [GitHub Issues](../../issues)
- 📧 **Email**: contato@radarlgpd.com.br

---

## 📝 Manutenção da Documentação

Ao adicionar novos arquivos de documentação:

1. Adicione uma entrada neste índice
2. Atualize o README.md se relevante
3. Atualize links relacionados em outros docs
4. Siga o padrão de formatação existente

---

**Última atualização**: 2025-10-20

**Versão da documentação**: 1.0.0
