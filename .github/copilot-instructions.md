# Instruções GitHub Copilot - Radar LGPD

## Contexto do Projeto

Este é o projeto **Radar LGPD**, uma API Spring Boot desenvolvida para ajudar micro e pequenos empreendedores a verificarem compliance com a Lei Geral de Proteção de Dados (LGPD) brasileira. A API recebe dados telemetria anonimizados de um plugin WordPress que varre bancos de dados em busca de dados pessoais sensíveis.

## Stack Tecnológico

- **Java 21** (LTS)
- **Spring Boot 3.5.6**
- **Spring Data JPA** (Persistência)
- **Spring Security** (Autenticação via API Key)
- **Spring Validation** (Validação de dados)
- **PostgreSQL** (Banco de dados)
- **Lombok** (Redução de boilerplate)
- **Maven** (Build tool)

## Convenções de Código

### Nomenclatura

- **Classes**: PascalCase (ex: `ScanResultController`, `TelemetryService`)
- **Métodos e variáveis**: camelCase (ex: `processScanResult`, `scanDurationMs`)
- **Constantes**: UPPER_SNAKE_CASE (ex: `MAX_REQUESTS_PER_HOUR`)
- **Pacotes**: lowercase (ex: `com.br.radarlgpd.radarlgpd.controller`)

### Estrutura de Pacotes

```
com.br.radarlgpd.radarlgpd/
├── config/           # Configurações (Security, Rate Limit, CORS)
├── controller/       # REST Controllers (endpoints)
├── dto/              # Data Transfer Objects (request/response)
├── entity/           # Entidades JPA (@Entity)
├── exception/        # Exceções customizadas
├── repository/       # Repositories JPA
├── service/          # Lógica de negócio
└── util/             # Classes utilitárias
```

### Padrões de Código

1. **Use Lombok**: Sempre que possível, use anotações Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)

2. **Validação**: Use Bean Validation (Jakarta Validation) nos DTOs:
   ```java
   @NotNull(message = "scan_id é obrigatório")
   @Pattern(regexp = "^[0-9a-f-]{36}$", message = "scan_id deve ser um UUID válido")
   private String scanId;
   ```

3. **Respostas HTTP**: Use `ResponseEntity` nos controllers:
   ```java
   @PostMapping("/v1/telemetry/scan-result")
   public ResponseEntity<ScanResultResponse> receiveScanResult(
       @Valid @RequestBody ScanResultRequest request
   ) {
       // lógica
       return ResponseEntity.ok(response);
   }
   ```

4. **Tratamento de Exceções**: Use `@ControllerAdvice` para exception handlers globais

5. **Logging**: Use SLF4J com Lombok:
   ```java
   @Slf4j
   public class ScanResultService {
       public void process() {
           log.info("Processing scan result...");
       }
   }
   ```

## Regras de Negócio Críticas

### 1. Privacidade e LGPD

**NUNCA** aceite, armazene ou processe dados pessoais reais (CPF, email, telefone, etc.). Apenas contagens agregadas são permitidas.

❌ **Proibido**:
```json
{
  "cpfs_encontrados": ["123.456.789-00", "987.654.321-00"]
}
```

✅ **Correto**:
```json
{
  "data_type": "CPF",
  "source_location": "wp_comments.comment_content",
  "count": 152
}
```

### 2. Consentimento Obrigatório

O campo `consent_given` deve ser `true`. Se for `false` ou `null`, retorne **HTTP 403 Forbidden**.

```java
if (!request.getConsentGiven()) {
    throw new ConsentNotGivenException("Consentimento não concedido");
}
```

### 3. Autenticação

Todas as requisições devem conter:
```
Authorization: Bearer {api-key}
```

Se ausente ou inválida: **HTTP 401 Unauthorized**

### 4. Rate Limiting

- **100 requisições/hora por IP**
- Excesso: **HTTP 429 Too Many Requests**
- Use cache (Redis ou Caffeine) para controlar

### 5. Validação Estrita

Use validações rigorosas:
- UUIDs devem seguir o padrão RFC 4122
- Timestamps devem ser ISO 8601 UTC
- Counts devem ser >= 0
- Versões devem seguir SemVer

## Endpoint Principal

### POST /v1/telemetry/scan-result

#### Request DTO (ScanResultRequest.java)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultRequest {
    
    @NotNull
    @Pattern(regexp = "^[0-9a-f-]{36}$")
    private String scanId;
    
    @NotBlank
    private String siteId;
    
    @NotNull
    private Boolean consentGiven;
    
    @NotNull
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$")
    private String scanTimestampUtc;
    
    @Min(0)
    private Integer scanDurationMs;
    
    @NotBlank
    private String scannerVersion;
    
    @Valid
    @NotNull
    private Environment environment;
    
    @Valid
    @NotEmpty
    private List<DataResult> results;
}
```

#### Responses

- **200 OK**: Scan processado com sucesso
- **400 Bad Request**: Payload inválido
- **401 Unauthorized**: API Key inválida
- **403 Forbidden**: Consentimento não dado
- **429 Too Many Requests**: Rate limit excedido
- **500 Internal Server Error**: Erro no servidor

## Testes

### Nomenclatura de Testes

```java
@Test
void deveAceitarScanComConsentimentoValido() { }

@Test
void deveRejeitarScanSemConsentimento() { }

@Test
void deveValidarFormatoUUID() { }
```

### Coverage Mínimo

- Controllers: 80%
- Services: 90%
- Validators: 95%

### Testes Obrigatórios

1. ✅ Validação de consentimento
2. ✅ Validação de API Key
3. ✅ Validação de payload
4. ✅ Rate limiting
5. ✅ Anonimização (rejeitar dados brutos)

## Segurança

### Checklist de Segurança

- [ ] API Key não deve estar hardcoded
- [ ] Senhas devem usar variáveis de ambiente
- [ ] Logs não devem expor dados sensíveis
- [ ] Validar TODOS os inputs
- [ ] Usar HTTPS em produção
- [ ] Implementar CORS apropriado
- [ ] Sanitizar mensagens de erro

### Exemplo de Log Seguro

❌ **Evite**:
```java
log.error("Erro ao processar CPF: {}", cpf);
```

✅ **Prefira**:
```java
log.error("Erro ao processar scan_id: {}", scanId);
```

## Mensagens de Commit

Siga o padrão Conventional Commits:

- `feat: adiciona endpoint de telemetria`
- `fix: corrige validação de UUID`
- `refactor: melhora estrutura de DTOs`
- `test: adiciona testes de rate limiting`
- `docs: atualiza README com exemplos`
- `chore: atualiza dependências do Spring`

## Dependências Importantes

```xml
<!-- Já incluídas no pom.xml -->
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- postgresql
- lombok
```

## Variáveis de Ambiente

Use o padrão:

```properties
# Database
RADARLGPD_DB_URL=jdbc:postgresql://localhost:5432/radarlgpd
RADARLGPD_DB_USERNAME=radarlgpd_user
RADARLGPD_DB_PASSWORD=sua_senha

# Security
RADARLGPD_API_KEY=sua-api-key-secreta

# Rate Limiting
RADARLGPD_RATE_LIMIT_REQUESTS_PER_HOUR=100
```

## Comentários de Código

### Quando Comentar

- ✅ Regras de negócio complexas relacionadas à LGPD
- ✅ Algoritmos de validação não óbvios
- ✅ TODOs para funcionalidades futuras
- ✅ Javadoc em classes de serviço públicas

### Quando NÃO Comentar

- ❌ Código auto-explicativo
- ❌ Getter/Setters gerados por Lombok
- ❌ Comentários óbvios ("Retorna o ID")

### Exemplo de Comentário Útil

```java
/**
 * Valida se o site_id é um hash válido.
 * Para compliance com LGPD, não aceitamos domínios em texto claro.
 * O site_id deve ser SHA256(domínio + salt).
 * 
 * @throws InvalidSiteIdException se o formato for inválido
 */
private void validateSiteId(String siteId) {
    // ...
}
```

## Erros Comuns a Evitar

1. ❌ **Não** retorne stack traces detalhados ao cliente
2. ❌ **Não** logue dados pessoais (CPF, email, etc.)
3. ❌ **Não** aceite payloads com campos extras não documentados
4. ❌ **Não** use `@RequestMapping` genérico, prefira `@PostMapping`, `@GetMapping`, etc.
5. ❌ **Não** injete dependências com `@Autowired` em campos, use injeção por construtor

## Exemplo de Controller Completo

```java
@RestController
@RequestMapping("/v1/telemetry")
@Slf4j
@RequiredArgsConstructor
public class TelemetryController {
    
    private final ScanResultService scanResultService;
    
    @PostMapping("/scan-result")
    public ResponseEntity<ScanResultResponse> receiveScanResult(
        @Valid @RequestBody ScanResultRequest request,
        @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Recebendo scan result para scan_id: {}", request.getScanId());
        
        ScanResultResponse response = scanResultService.processScan(request);
        
        return ResponseEntity.ok(response);
    }
}
```

## Dúvidas?

Para qualquer dúvida sobre padrões ou implementação:

1. Consulte este arquivo primeiro
2. Verifique o README.md do projeto
3. Revise os requisitos funcionais e não-funcionais
4. Em caso de dúvida sobre LGPD, sempre priorize a privacidade do usuário

---

**Lembre-se**: Este projeto lida com dados sensíveis relacionados à LGPD. Sempre priorize privacidade, segurança e compliance.
