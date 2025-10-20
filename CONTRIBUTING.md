# Guia de Contribuição - Radar LGPD

Obrigado por considerar contribuir para o Radar LGPD! Este documento fornece diretrizes para contribuições ao projeto.

## 📋 Índice

- [Código de Conduta](#código-de-conduta)
- [Como Posso Contribuir?](#como-posso-contribuir)
- [Configuração do Ambiente](#configuração-do-ambiente)
- [Padrões de Código](#padrões-de-código)
- [Processo de Pull Request](#processo-de-pull-request)
- [Reportando Bugs](#reportando-bugs)
- [Sugerindo Melhorias](#sugerindo-melhorias)

## 📜 Código de Conduta

Este projeto segue um Código de Conduta. Ao participar, você concorda em manter um ambiente respeitoso e inclusivo.

### Comportamentos Esperados

- Use linguagem acolhedora e inclusiva
- Respeite diferentes pontos de vista e experiências
- Aceite críticas construtivas com graça
- Foque no que é melhor para a comunidade
- Mostre empatia com outros membros da comunidade

### Comportamentos Inaceitáveis

- Uso de linguagem ou imagens sexualizadas
- Trolling, comentários insultuosos ou depreciativos
- Assédio público ou privado
- Publicar informações privadas de terceiros sem permissão
- Outras condutas consideradas inapropriadas em ambiente profissional

## 🤝 Como Posso Contribuir?

### Tipos de Contribuição

1. **Reportar Bugs**: Encontrou um problema? Abra uma issue!
2. **Sugerir Melhorias**: Tem ideias para novas funcionalidades? Compartilhe!
3. **Corrigir Bugs**: Veja issues marcadas com `bug` ou `good first issue`
4. **Implementar Features**: Escolha issues marcadas com `enhancement`
5. **Melhorar Documentação**: Sempre há espaço para docs melhores
6. **Revisar Pull Requests**: Ajude a revisar código de outros contribuidores

## 🛠️ Configuração do Ambiente

### Pré-requisitos

- Java 21 ou superior
- Maven 3.8+
- PostgreSQL 14+
- Git
- IDE de sua preferência (IntelliJ IDEA recomendado)

### Setup Passo a Passo

1. **Fork o repositório**
   ```bash
   # Clique em "Fork" no GitHub
   ```

2. **Clone seu fork**
   ```bash
   git clone https://github.com/SEU-USUARIO/radarlgpd-api.git
   cd radarlgpd-api
   ```

3. **Configure o remote upstream**
   ```bash
   git remote add upstream https://github.com/ORIGINAL/radarlgpd-api.git
   ```

4. **Configure o banco de dados**
   ```sql
   CREATE DATABASE radarlgpd_dev;
   CREATE USER radarlgpd_dev WITH PASSWORD 'dev_password';
   GRANT ALL PRIVILEGES ON DATABASE radarlgpd_dev TO radarlgpd_dev;
   ```

5. **Copie o arquivo de configuração**
   ```bash
   cp src/main/resources/application-local.properties.example \
      src/main/resources/application-local.properties
   ```

6. **Edite as configurações**
   ```bash
   # Edite application-local.properties com suas credenciais
   ```

7. **Execute os testes**
   ```bash
   ./mvnw clean test
   ```

8. **Inicie a aplicação**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

## 🎨 Padrões de Código

### Estilo de Código Java

Seguimos as convenções do Google Java Style Guide com algumas adaptações:

- **Indentação**: 4 espaços (não tabs)
- **Comprimento de linha**: máximo 120 caracteres
- **Nomenclatura**:
  - Classes: `PascalCase`
  - Métodos/Variáveis: `camelCase`
  - Constantes: `UPPER_SNAKE_CASE`
  - Pacotes: `lowercase`

### Uso de Lombok

Use anotações Lombok para reduzir boilerplate:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinhaClasse {
    private String campo;
}
```

### Validação

Use Bean Validation em DTOs:

```java
@NotBlank(message = "Campo obrigatório")
@Size(min = 5, max = 50, message = "Deve ter entre 5 e 50 caracteres")
private String campo;
```

### Logging

Use SLF4J com Lombok:

```java
@Slf4j
public class MinhaClasse {
    public void metodo() {
        log.info("Mensagem informativa");
        log.error("Erro: {}", mensagem);
    }
}
```

### Testes

- **Nomenclatura**: `deveComportamentoEsperadoQuandoCondicao()`
- **Coverage mínimo**: 80% para controllers, 90% para services
- **Estrutura**: Arrange, Act, Assert

```java
@Test
void deveRetornar200QuandoPayloadValido() {
    // Arrange
    ScanResultRequest request = createValidRequest();
    
    // Act
    ResponseEntity<?> response = controller.receiveScanResult(request);
    
    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
}
```

### Commits Semânticos (Conventional Commits)

Use o formato:

```
tipo(escopo): mensagem curta

Descrição detalhada (opcional)

Refs: #123 (issue relacionada)
```

**Tipos permitidos:**
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Mudanças na documentação
- `style`: Formatação, ponto e vírgula, etc
- `refactor`: Refatoração de código
- `test`: Adição ou correção de testes
- `chore`: Tarefas de manutenção

**Exemplos:**
```
feat(telemetry): adiciona endpoint de scan result

Implementa endpoint POST /v1/telemetry/scan-result
para receber resultados de varreduras do plugin WordPress.

Refs: #12
```

```
fix(validation): corrige validação de UUID

UUID estava aceitando formato inválido.
Ajustado regex para RFC 4122.

Refs: #45
```

## 🔄 Processo de Pull Request

### Antes de Criar um PR

1. **Sincronize com upstream**
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```

2. **Crie uma branch**
   ```bash
   git checkout -b feature/minha-feature
   # ou
   git checkout -b fix/meu-bug
   ```

3. **Faça suas alterações**
   - Escreva código limpo e documentado
   - Adicione testes para novas funcionalidades
   - Atualize documentação se necessário

4. **Execute testes localmente**
   ```bash
   ./mvnw clean verify
   ```

5. **Commit suas mudanças**
   ```bash
   git add .
   git commit -m "feat(escopo): descrição clara"
   ```

6. **Push para seu fork**
   ```bash
   git push origin feature/minha-feature
   ```

### Criando o Pull Request

1. Vá até o repositório original no GitHub
2. Clique em "New Pull Request"
3. Selecione sua branch
4. Preencha o template:

```markdown
## Descrição
Breve descrição das mudanças

## Tipo de Mudança
- [ ] Bug fix
- [ ] Nova funcionalidade
- [ ] Breaking change
- [ ] Documentação

## Como Testar
1. Passo 1
2. Passo 2

## Checklist
- [ ] Testes passando localmente
- [ ] Código segue os padrões do projeto
- [ ] Documentação atualizada
- [ ] Sem warnings de build
- [ ] Commits seguem Conventional Commits
```

### Durante a Revisão

- Responda aos comentários prontamente
- Faça alterações solicitadas
- Marque conversas como resolvidas após ajustes
- Mantenha a descrição do PR atualizada

### Após Aprovação

- Aguarde um maintainer fazer o merge
- Não faça force push após aprovação
- Delete sua branch após o merge

## 🐛 Reportando Bugs

### Antes de Reportar

1. Verifique se o bug já foi reportado nas Issues
2. Teste na versão mais recente
3. Colete informações do ambiente

### Template de Bug Report

```markdown
**Descrição do Bug**
Descrição clara e concisa do problema.

**Para Reproduzir**
1. Faça requisição para '...'
2. Com payload '...'
3. Veja erro

**Comportamento Esperado**
O que deveria acontecer.

**Screenshots**
Se aplicável, adicione screenshots.

**Ambiente:**
 - OS: [ex: Ubuntu 22.04]
 - Java: [ex: 21]
 - Spring Boot: [ex: 3.5.6]
 - Versão: [ex: 1.0.0]

**Logs**
```
Cole logs relevantes aqui
```

**Contexto Adicional**
Qualquer outra informação relevante.
```

## 💡 Sugerindo Melhorias

### Template de Feature Request

```markdown
**O problema**
Descrição clara do problema que a feature resolveria.

**Solução Proposta**
Como você imagina a solução.

**Alternativas Consideradas**
Outras abordagens que você considerou.

**Contexto Adicional**
Screenshots, mockups, links, etc.
```

## 🔐 Considerações de Segurança

### Reportando Vulnerabilidades

**NÃO** abra issues públicas para vulnerabilidades de segurança!

Em vez disso:
1. Envie email para: seguranca@radarlgpd.com.br
2. Inclua detalhes da vulnerabilidade
3. Aguarde resposta em até 48 horas
4. Trabalhe conosco para uma solução antes de disclosure público

### Dados Sensíveis

- **NUNCA** commite dados pessoais reais
- **NUNCA** commite API keys ou senhas
- **NUNCA** logue dados sensíveis
- Use apenas dados anonimizados em exemplos

## 📚 Recursos Adicionais

- [README.md](../README.md) - Visão geral do projeto
- [.github/copilot-instructions.md](.github/copilot-instructions.md) - Instruções para desenvolvimento
- [.github/code-examples.md](.github/code-examples.md) - Exemplos de código
- [LGPD - Lei 13.709/2018](http://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709.htm)

## ❓ Dúvidas?

- Abra uma [Discussion](../../discussions) no GitHub
- Entre em contato: contato@radarlgpd.com.br

---

Obrigado por contribuir com o Radar LGPD! 🎉
