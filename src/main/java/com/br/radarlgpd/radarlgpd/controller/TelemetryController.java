package com.br.radarlgpd.radarlgpd.controller;

import com.br.radarlgpd.radarlgpd.dto.ScanResultRequest;
import com.br.radarlgpd.radarlgpd.dto.ScanResultResponse;
import com.br.radarlgpd.radarlgpd.service.ScanResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para receber telemetria de scans do plugin WordPress.
 * Endpoint principal: POST /v1/telemetry/scan-result
 */
@RestController
@RequestMapping("/v1/telemetry")
@RequiredArgsConstructor
@Slf4j
public class TelemetryController {

    private final ScanResultService scanResultService;

    /**
     * Recebe e processa resultados de scan do plugin WordPress.
     * 
     * @param request dados agregados do scan (NUNCA dados pessoais)
     * @return resposta com status do processamento
     */
    @PostMapping("/scan-result")
    public ResponseEntity<ScanResultResponse> receiveScanResult(
        @Valid @RequestBody ScanResultRequest request
    ) {
        log.info("Recebendo scan result - scan_id: {}", request.getScanId());
        
        ScanResultResponse response = scanResultService.processScan(request);
        
        log.info("Scan processado - scan_id: {}, status: {}", 
            request.getScanId(), response.getStatus());
        
        return ResponseEntity.ok(response);
    }
}
