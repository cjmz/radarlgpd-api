# Exemplos de Código - Radar LGPD

Este arquivo contém exemplos de código para referência rápida ao desenvolver a API.

## 1. DTOs (Data Transfer Objects)

### ScanResultRequest.java

```java
package com.br.radarlgpd.radarlgpd.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultRequest {
    
    @NotBlank(message = "scan_id é obrigatório")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", 
             message = "scan_id deve ser um UUID válido")
    private String scanId;
    
    @NotBlank(message = "site_id é obrigatório")
    @Size(min = 64, max = 64, message = "site_id deve ter 64 caracteres (SHA256)")
    private String siteId;
    
    @NotNull(message = "consent_given é obrigatório")
    private Boolean consentGiven;
    
    @NotBlank(message = "scan_timestamp_utc é obrigatório")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$", 
             message = "scan_timestamp_utc deve estar no formato ISO 8601 UTC")
    private String scanTimestampUtc;
    
    @NotNull(message = "scan_duration_ms é obrigatório")
    @Min(value = 0, message = "scan_duration_ms deve ser maior ou igual a 0")
    private Integer scanDurationMs;
    
    @NotBlank(message = "scanner_version é obrigatório")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?$", 
             message = "scanner_version deve seguir SemVer (ex: 1.0.0-mvp)")
    private String scannerVersion;
    
    @Valid
    @NotNull(message = "environment é obrigatório")
    private Environment environment;
    
    @Valid
    @NotEmpty(message = "results não pode estar vazio")
    private List<DataResult> results;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Environment {
        
        @NotBlank(message = "wp_version é obrigatório")
        private String wpVersion;
        
        @NotBlank(message = "php_version é obrigatório")
        private String phpVersion;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataResult {
        
        @NotBlank(message = "data_type é obrigatório")
        @Pattern(regexp = "^(CPF|CNPJ|EMAIL|TELEFONE|RG|CEP)$", 
                 message = "data_type deve ser um dos tipos permitidos")
        private String dataType;
        
        @NotBlank(message = "source_location é obrigatório")
        @Pattern(regexp = "^[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+$", 
                 message = "source_location deve estar no formato tabela.coluna")
        private String sourceLocation;
        
        @NotNull(message = "count é obrigatório")
        @Min(value = 0, message = "count deve ser maior ou igual a 0")
        private Integer count;
    }
}
```

### ScanResultResponse.java

```java
package com.br.radarlgpd.radarlgpd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultResponse {
    
    private String scanId;
    private String status;
    private String message;
    private LocalDateTime receivedAt;
    
    public static ScanResultResponse success(String scanId) {
        return ScanResultResponse.builder()
                .scanId(scanId)
                .status("ACCEPTED")
                .message("Scan result recebido e processado com sucesso")
                .receivedAt(LocalDateTime.now())
                .build();
    }
}
```

## 2. Entidades JPA

### ScanResult.java

```java
package com.br.radarlgpd.radarlgpd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "scan_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 36)
    private String scanId;
    
    @Column(nullable = false, length = 64)
    private String siteId;
    
    @Column(nullable = false)
    private Boolean consentGiven;
    
    @Column(nullable = false)
    private LocalDateTime scanTimestamp;
    
    @Column(nullable = false)
    private Integer scanDurationMs;
    
    @Column(nullable = false, length = 20)
    private String scannerVersion;
    
    @Column(nullable = false, length = 20)
    private String wpVersion;
    
    @Column(nullable = false, length = 20)
    private String phpVersion;
    
    @Column(nullable = false)
    private LocalDateTime receivedAt;
    
    @OneToMany(mappedBy = "scanResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DataResultEntity> dataResults;
    
    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}
```

### DataResultEntity.java

```java
package com.br.radarlgpd.radarlgpd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "data_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResultEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String dataType;
    
    @Column(nullable = false, length = 100)
    private String sourceLocation;
    
    @Column(nullable = false)
    private Integer count;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_result_id", nullable = false)
    private ScanResult scanResult;
}
```

## 3. Repository

```java
package com.br.radarlgpd.radarlgpd.repository;

import com.br.radarlgpd.radarlgpd.entity.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScanResultRepository extends JpaRepository<ScanResult, Long> {
    
    Optional<ScanResult> findByScanId(String scanId);
    
    boolean existsByScanId(String scanId);
}
```

## 4. Service

```java
package com.br.radarlgpd.radarlgpd.service;

import com.br.radarlgpd.radarlgpd.dto.ScanResultRequest;
import com.br.radarlgpd.radarlgpd.dto.ScanResultResponse;
import com.br.radarlgpd.radarlgpd.entity.DataResultEntity;
import com.br.radarlgpd.radarlgpd.entity.ScanResult;
import com.br.radarlgpd.radarlgpd.exception.ConsentNotGivenException;
import com.br.radarlgpd.radarlgpd.exception.DuplicateScanException;
import com.br.radarlgpd.radarlgpd.repository.ScanResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScanResultService {
    
    private final ScanResultRepository scanResultRepository;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    @Transactional
    public ScanResultResponse processScan(ScanResultRequest request) {
        log.info("Processando scan_id: {}", request.getScanId());
        
        // Validar consentimento
        validateConsent(request);
        
        // Verificar duplicatas
        checkDuplicate(request.getScanId());
        
        // Converter e salvar
        ScanResult scanResult = convertToEntity(request);
        scanResultRepository.save(scanResult);
        
        log.info("Scan processado com sucesso: {}", request.getScanId());
        return ScanResultResponse.success(request.getScanId());
    }
    
    private void validateConsent(ScanResultRequest request) {
        if (!Boolean.TRUE.equals(request.getConsentGiven())) {
            log.warn("Tentativa de envio sem consentimento para scan_id: {}", request.getScanId());
            throw new ConsentNotGivenException("Consentimento não foi concedido");
        }
    }
    
    private void checkDuplicate(String scanId) {
        if (scanResultRepository.existsByScanId(scanId)) {
            log.warn("Scan duplicado detectado: {}", scanId);
            throw new DuplicateScanException("Scan já foi processado anteriormente");
        }
    }
    
    private ScanResult convertToEntity(ScanResultRequest request) {
        ScanResult scanResult = ScanResult.builder()
                .scanId(request.getScanId())
                .siteId(request.getSiteId())
                .consentGiven(request.getConsentGiven())
                .scanTimestamp(LocalDateTime.parse(request.getScanTimestampUtc(), ISO_FORMATTER))
                .scanDurationMs(request.getScanDurationMs())
                .scannerVersion(request.getScannerVersion())
                .wpVersion(request.getEnvironment().getWpVersion())
                .phpVersion(request.getEnvironment().getPhpVersion())
                .build();
        
        List<DataResultEntity> dataResults = request.getResults().stream()
                .map(dr -> DataResultEntity.builder()
                        .dataType(dr.getDataType())
                        .sourceLocation(dr.getSourceLocation())
                        .count(dr.getCount())
                        .scanResult(scanResult)
                        .build())
                .collect(Collectors.toList());
        
        scanResult.setDataResults(dataResults);
        
        return scanResult;
    }
}
```

## 5. Controller

```java
package com.br.radarlgpd.radarlgpd.controller;

import com.br.radarlgpd.radarlgpd.dto.ScanResultRequest;
import com.br.radarlgpd.radarlgpd.dto.ScanResultResponse;
import com.br.radarlgpd.radarlgpd.service.ScanResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/telemetry")
@Slf4j
@RequiredArgsConstructor
public class TelemetryController {
    
    private final ScanResultService scanResultService;
    
    @PostMapping("/scan-result")
    public ResponseEntity<ScanResultResponse> receiveScanResult(
            @Valid @RequestBody ScanResultRequest request
    ) {
        log.info("Recebendo scan result para scan_id: {}", request.getScanId());
        
        ScanResultResponse response = scanResultService.processScan(request);
        
        return ResponseEntity.ok(response);
    }
}
```

## 6. Exception Handling

### Exceções Customizadas

```java
package com.br.radarlgpd.radarlgpd.exception;

public class ConsentNotGivenException extends RuntimeException {
    public ConsentNotGivenException(String message) {
        super(message);
    }
}
```

```java
package com.br.radarlgpd.radarlgpd.exception;

public class DuplicateScanException extends RuntimeException {
    public DuplicateScanException(String message) {
        super(message);
    }
}
```

```java
package com.br.radarlgpd.radarlgpd.exception;

public class InvalidApiKeyException extends RuntimeException {
    public InvalidApiKeyException(String message) {
        super(message);
    }
}
```

### Global Exception Handler

```java
package com.br.radarlgpd.radarlgpd.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ConsentNotGivenException.class)
    public ResponseEntity<ErrorResponse> handleConsentNotGiven(ConsentNotGivenException ex) {
        log.warn("Consentimento não concedido: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidApiKey(InvalidApiKeyException ex) {
        log.warn("API Key inválida");
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("API Key inválida ou ausente")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(DuplicateScanException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateScan(DuplicateScanException ex) {
        log.warn("Scan duplicado: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Erro de validação")
                .validationErrors(errors)
                .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro interno: ", ex);
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ocorreu um erro interno no servidor")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### ErrorResponse DTO

```java
package com.br.radarlgpd.radarlgpd.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private Map<String, String> validationErrors;
}
```

## 7. Security Configuration

```java
package com.br.radarlgpd.radarlgpd.config;

import com.br.radarlgpd.radarlgpd.security.ApiKeyAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final ApiKeyAuthFilter apiKeyAuthFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### API Key Filter

```java
package com.br.radarlgpd.radarlgpd.security;

import com.br.radarlgpd.radarlgpd.exception.InvalidApiKeyException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    
    @Value("${radarlgpd.api.key}")
    private String validApiKey;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Permitir health check sem autenticação
        if (request.getRequestURI().equals("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidApiKeyException("API Key ausente");
        }
        
        String apiKey = authHeader.substring(7);
        
        if (!validApiKey.equals(apiKey)) {
            throw new InvalidApiKeyException("API Key inválida");
        }
        
        filterChain.doFilter(request, response);
    }
}
```

## 8. Testes

```java
package com.br.radarlgpd.radarlgpd.controller;

import com.br.radarlgpd.radarlgpd.dto.ScanResultRequest;
import com.br.radarlgpd.radarlgpd.service.ScanResultService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TelemetryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ScanResultService scanResultService;
    
    @Test
    void deveRejeitarRequisicaoSemApiKey() throws Exception {
        ScanResultRequest request = createValidRequest();
        
        mockMvc.perform(post("/v1/telemetry/scan-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void deveRejeitarRequisicaoSemConsentimento() throws Exception {
        ScanResultRequest request = createValidRequest();
        request.setConsentGiven(false);
        
        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer valid-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    private ScanResultRequest createValidRequest() {
        return ScanResultRequest.builder()
                .scanId("550e8400-e29b-41d4-a716-446655440000")
                .siteId("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .consentGiven(true)
                .scanTimestampUtc("2025-10-20T14:30:01Z")
                .scanDurationMs(4580)
                .scannerVersion("1.0.0-mvp")
                .environment(ScanResultRequest.Environment.builder()
                        .wpVersion("6.4.1")
                        .phpVersion("8.1")
                        .build())
                .results(List.of(
                        ScanResultRequest.DataResult.builder()
                                .dataType("CPF")
                                .sourceLocation("wp_comments.comment_content")
                                .count(152)
                                .build()
                ))
                .build();
    }
}
```

---

Use estes exemplos como referência ao desenvolver a API Radar LGPD.
