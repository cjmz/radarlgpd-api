package com.br.radarlgpd.radarlgpd.controller;

import com.br.radarlgpd.radarlgpd.dto.ScanResultRequest;
import com.br.radarlgpd.radarlgpd.dto.ScanResultResponse;
import com.br.radarlgpd.radarlgpd.entity.Instance;
import com.br.radarlgpd.radarlgpd.exception.ConsentNotGivenException;
import com.br.radarlgpd.radarlgpd.service.InstanceService;
import com.br.radarlgpd.radarlgpd.service.ScanResultService;
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
    public ResponseEntity<ScanResultResponse> receiveScanResult(
        @Valid @RequestBody ScanResultRequest request,
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
