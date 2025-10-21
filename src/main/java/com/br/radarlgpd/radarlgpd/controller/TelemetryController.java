package com.br.radarlgpd.radarlgpd.controller;

import com.br.radarlgpd.radarlgpd.dto.ErrorResponse;
import com.br.radarlgpd.radarlgpd.dto.ScanResultRequest;
import com.br.radarlgpd.radarlgpd.dto.ScanResultResponse;
import com.br.radarlgpd.radarlgpd.entity.Instance;
import com.br.radarlgpd.radarlgpd.exception.ConsentNotGivenException;
import com.br.radarlgpd.radarlgpd.service.InstanceService;
import com.br.radarlgpd.radarlgpd.service.ScanResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para receber telemetria de scans do plugin WordPress.
 * Endpoint principal: POST /v1/telemetry/scan-result
 * 
 * Implementa o Épico 1.1 com dois fluxos:
 * - Cenário A (RF-API-2.0): Plugin já autenticado (header Authorization presente)
 * - Cenário B (RF-API-3.0): Novo plugin (header Authorization ausente) -> Registro
 */
@RestController
@RequestMapping("/v1/telemetry")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Telemetria", 
    description = "Endpoints para recebimento de dados de scan do plugin WordPress (apenas dados agregados e anonimizados)"
)
public class TelemetryController {

    private final ScanResultService scanResultService;
    private final InstanceService instanceService;

    /**
     * Recebe e processa resultados de scan do plugin WordPress.
     * 
     * RF-API-1.0: Roteamento de Lógica (Detecção de Autenticação)
     * - Se Authorization presente -> Fluxo Autenticado (RF-API-2.0)
     * - Se Authorization ausente -> Fluxo de Registro (RF-API-3.0)
     * 
     * RF-API-1.1: Validação de Consentimento (SEMPRE obrigatória)
     * RF-API-1.2: Validação de Schema (via @Valid)
     * 
     * @param request dados agregados do scan (NUNCA dados pessoais)
     * @param authHeader header Authorization (opcional, mas indica fluxo)
     * @return resposta com status + token (se registro) ou apenas status (se autenticado)
     */
    @PostMapping("/scan-result")
    @Transactional // RNF-API-PERF-1.0: Atomicidade (registro + telemetria juntos)
    @Operation(
        summary = "Recebe dados de scan do plugin WordPress",
        description = """
            **Endpoint principal para telemetria de scans LGPD**
            
            Este endpoint suporta **dois fluxos**:
            
            ### 🔐 Cenário A: Plugin Autenticado (Retorna instância existente)
            - ✅ Envia header `Authorization: Bearer {instance_token}`
            - ✅ Resposta: `{ "status": "received" }`
            
            ### 🆕 Cenário B: Novo Plugin (Registro)
            - ✅ **NÃO** envia header `Authorization`
            - ✅ Resposta: `{ "status": "registered", "instance_token": "uuid..." }`
            - ⚠️ **Guarde o instance_token** para usar em scans futuros!
            
            ---
            
            ### ⚠️ Regras LGPD Obrigatórias
            
            1. **Consentimento**: `consent_given` DEVE ser `true`
            2. **Anonimização**: Apenas contagens agregadas (NUNCA dados pessoais)
            3. **Rate Limit**: 100 requisições/hora por IP
            
            ---
            
            ### 📊 Exemplo de Payload
            
            ```json
            {
              "scanId": "123e4567-e89b-12d3-a456-426614174000",
              "siteId": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
              "consentGiven": true,
              "scanTimestampUtc": "2025-10-20T21:20:00Z",
              "scanDurationMs": 1500,
              "scannerVersion": "1.0.0-mvp",
              "environment": {
                "wpVersion": "6.4.0",
                "phpVersion": "8.2.0"
              },
              "results": [
                {
                  "dataType": "CPF",
                  "sourceLocation": "wp_comments.comment_content",
                  "count": 152
                }
              ]
            }
            ```
            """,
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ Scan processado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScanResultResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Cenário A: Plugin Autenticado",
                        description = "Resposta quando o plugin já possui um instance_token",
                        value = """
                            {
                              "status": "received"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Cenário B: Novo Plugin (Registro)",
                        description = "Resposta no primeiro scan (sem header Authorization). GUARDE o instance_token!",
                        value = """
                            {
                              "status": "registered",
                              "instance_token": "9f8c7b6a-5d4e-3c2b-1a0f-9e8d7c6b5a4f"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ Payload inválido (validação de schema falhou)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Erro de Validação",
                    value = """
                        {
                          "timestamp": "2025-10-20T21:20:00Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Erro de validação",
                          "path": "/v1/telemetry/scan-result",
                          "errors": [
                            {
                              "field": "scanId",
                              "message": "scan_id deve ser um UUID válido"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ Token de autenticação inválido ou expirado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Token Inválido",
                    value = """
                        {
                          "timestamp": "2025-10-20T21:20:00Z",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Token de instância inválido ou expirado",
                          "path": "/v1/telemetry/scan-result"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "❌ Consentimento não concedido (LGPD Art. 7º)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Sem Consentimento",
                    value = """
                        {
                          "timestamp": "2025-10-20T21:20:00Z",
                          "status": 403,
                          "error": "Forbidden",
                          "message": "Consentimento não concedido. Dados não podem ser processados conforme LGPD Art. 7º",
                          "path": "/v1/telemetry/scan-result"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "❌ Rate limit excedido (máx. 100 requisições/hora)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Rate Limit Excedido",
                    value = """
                        {
                          "timestamp": "2025-10-20T21:20:00Z",
                          "status": 429,
                          "error": "Too Many Requests",
                          "message": "Rate limit excedido. Máximo de 100 requisições por hora.",
                          "path": "/v1/telemetry/scan-result"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "❌ Erro interno do servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<ScanResultResponse> receiveScanResult(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados agregados do scan (apenas contagens, NUNCA dados pessoais)",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScanResultRequest.class),
                examples = @ExampleObject(
                    name = "Exemplo Completo",
                    value = """
                        {
                          "scanId": "123e4567-e89b-12d3-a456-426614174000",
                          "siteId": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                          "consentGiven": true,
                          "scanTimestampUtc": "2025-10-20T21:20:00Z",
                          "scanDurationMs": 1500,
                          "scannerVersion": "1.0.0-mvp",
                          "environment": {
                            "wpVersion": "6.4.0",
                            "phpVersion": "8.2.0"
                          },
                          "results": [
                            {
                              "dataType": "CPF",
                              "sourceLocation": "wp_comments.comment_content",
                              "count": 152
                            },
                            {
                              "dataType": "EMAIL",
                              "sourceLocation": "wp_users.user_email",
                              "count": 250
                            }
                          ]
                        }
                        """
                )
            )
        )
        @Valid @RequestBody ScanResultRequest request,
        @Parameter(
            description = """
                Token de autenticação da instância (opcional).
                
                - **Com token**: Fluxo autenticado (retorna status: received)
                - **Sem token**: Fluxo de registro (retorna status: registered + instance_token)
                """,
            example = "Bearer 9f8c7b6a-5d4e-3c2b-1a0f-9e8d7c6b5a4f"
        )
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("Recebendo scan result - scan_id: {}, auth_presente: {}", 
            request.getScanId(), authHeader != null);

        // RF-API-1.1: Validação de Consentimento (obrigatória em AMBOS os fluxos)
        validateConsent(request);

        // RF-API-1.0: Roteamento de Lógica
        if (authHeader != null && !authHeader.isBlank()) {
            // Cenário A: Fluxo Autenticado (RF-API-2.0)
            return handleAuthenticatedFlow(request, authHeader);
        } else {
            // Cenário B: Fluxo de Registro (RF-API-3.0)
            return handleRegistrationFlow(request);
        }
    }

    /**
     * RF-API-2.0: Fluxo Autenticado (Cenário A)
     * 
     * 1. RF-API-2.1: Valida token e obtém instância
     * 2. RF-API-2.2: Persiste telemetria associada à instância
     * 3. RF-API-2.3: Retorna HTTP 200 com { "status": "received" }
     */
    private ResponseEntity<ScanResultResponse> handleAuthenticatedFlow(
        ScanResultRequest request, 
        String authHeader
    ) {
        log.debug("Processando fluxo autenticado - scan_id: {}", request.getScanId());

        // RF-API-2.1: Validação de Token
        Instance instance = instanceService.validateAndGetInstance(authHeader);

        // RF-API-2.2: Persistência de Telemetria
        boolean scanWasProcessed = scanResultService.processScanForInstance(request, instance.getId());

        // Atualiza métricas de atividade da instância (apenas se scan foi realmente salvo)
        if (scanWasProcessed) {
            instanceService.recordScanActivity(instance);
        }

        log.info("Scan recebido de instância existente - scan_id: {}, instance_id: {}, processado: {}, total_scans: {}", 
            request.getScanId(), instance.getId(), scanWasProcessed, instance.getScanCount());

        // RF-API-2.3: Resposta (Sucesso)
        ScanResultResponse response = ScanResultResponse.builder()
            .status("received")
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * RF-API-3.0: Fluxo de Registro (Cenário B)
     * 
     * 1. RF-API-3.1: Gera novo instance_token e cria registro em Instancias
     * 2. RF-API-3.2: Persiste telemetria do primeiro scan
     * 3. RF-API-3.3: Retorna HTTP 200 com { "status": "registered", "instance_token": "uuid..." }
     * 
     * RNF-API-PERF-1.0: Transação atômica garante que ambos (instância + scan) sejam salvos juntos.
     */
    private ResponseEntity<ScanResultResponse> handleRegistrationFlow(ScanResultRequest request) {
        log.info("Processando fluxo de registro - scan_id: {}, site_id: {}", 
            request.getScanId(), request.getSiteId());

        // RF-API-3.1: Geração de Instância
        Instance newInstance = instanceService.registerNewInstance(
            request.getSiteId(), 
            request.getScannerVersion()
        );

        // RF-API-3.2: Persistência de Telemetria (Primeiro Scan)
        boolean scanWasProcessed = scanResultService.processScanForInstance(request, newInstance.getId());

        // Atualiza métricas (apenas se scan foi realmente salvo)
        if (scanWasProcessed) {
            instanceService.recordScanActivity(newInstance);
        }

        log.info("Nova instância registrada - instance_id: {}, instance_token: {}, scan_id: {}, scan_processado: {}", 
            newInstance.getId(), newInstance.getInstanceToken(), request.getScanId(), scanWasProcessed);

        // RF-API-3.3: Resposta (Sucesso no Registro)
        ScanResultResponse response = ScanResultResponse.builder()
            .status("registered")
            .instanceToken(newInstance.getInstanceToken())
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * RF-API-1.1: Validação de Consentimento (Requisito LGPD Mandatório)
     * 
     * Esta é nossa verificação de base legal para garantir que não estamos 
     * processando dados sem opt-in explícito do usuário (LGPD Art. 7º).
     * 
     * @throws ConsentNotGivenException se consent_given for false ou null -> HTTP 403
     */
    private void validateConsent(ScanResultRequest request) {
        if (request.getConsentGiven() == null || !request.getConsentGiven()) {
            log.warn("Tentativa de envio sem consentimento - scan_id: {}, site_id: {}", 
                request.getScanId(), request.getSiteId());
            throw new ConsentNotGivenException(
                "Consentimento não concedido. Dados não podem ser processados conforme LGPD Art. 7º"
            );
        }
    }
}
