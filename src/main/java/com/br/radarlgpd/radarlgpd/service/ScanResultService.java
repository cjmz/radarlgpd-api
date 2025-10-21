package com.br.radarlgpd.radarlgpd.service;

import com.br.radarlgpd.radarlgpd.dto.ScanResultRequest;
import com.br.radarlgpd.radarlgpd.dto.ScanResultResponse;
import com.br.radarlgpd.radarlgpd.entity.DataResultEntity;
import com.br.radarlgpd.radarlgpd.entity.ScanResult;
import com.br.radarlgpd.radarlgpd.exception.ConsentNotGivenException;
import com.br.radarlgpd.radarlgpd.repository.DataResultRepository;
import com.br.radarlgpd.radarlgpd.repository.ScanResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service para processar e persistir scan results.
 * Implementa validação de consentimento e lógica de negócio LGPD.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScanResultService {

    private final ScanResultRepository scanResultRepository;
    private final DataResultRepository dataResultRepository;

    /**
     * Processa um scan result associado a uma instância específica.
     * Método principal usado pelos fluxos autenticado e de registro (RF-API-2.2, RF-API-3.2).
     * 
     * @param request dados do scan
     * @param instanceId ID da instância (obtido por validação de token ou registro)
     * @return true se o scan foi processado, false se foi ignorado (duplicado)
     * @throws ConsentNotGivenException se consentimento não foi dado
     */
    @Transactional
    public boolean processScanForInstance(ScanResultRequest request, Long instanceId) {
        log.info("Processando scan_id: {} para instance_id: {}", request.getScanId(), instanceId);
        
        // RF-API-1.1: Validação de Consentimento (obrigatória em ambos os fluxos)
        validateConsent(request);
        
        // Verifica se já existe scan com este ID (idempotência)
        if (scanResultRepository.existsByScanId(request.getScanId())) {
            log.warn("Scan duplicado detectado: {}", request.getScanId());
            return false; // Idempotente: não gera erro, apenas ignora
        }
        
        // Converte e persiste
        ScanResult scanResult = convertToEntity(request, instanceId);
        scanResult = scanResultRepository.save(scanResult);
        
        // Persiste os resultados detalhados
        saveDataResults(scanResult, request);
        
        log.info("Scan processado com sucesso: {} - instance_id: {}, {} resultados encontrados", 
            request.getScanId(), instanceId, request.getResults().size());
        
        return true;
    }

    /**
     * Processa um scan result recebido do plugin WordPress (MÉTODO LEGADO).
     * Mantido para compatibilidade, mas deve ser substituído por processScanForInstance.
     * 
     * @deprecated Use {@link #processScanForInstance(ScanResultRequest, Long)} ao invés
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @Transactional
    public ScanResultResponse processScan(ScanResultRequest request) {
        log.info("Processando scan_id: {}", request.getScanId());
        
        // NFR-API-003: Validação de Consentimento
        validateConsent(request);
        
        // Verifica se já existe scan com este ID (idempotência)
        if (scanResultRepository.existsByScanId(request.getScanId())) {
            log.warn("Scan duplicado detectado: {}", request.getScanId());
            return buildResponse(request.getScanId(), "DUPLICATE", 
                "Scan já foi processado anteriormente");
        }
        
        // Converte e persiste
        ScanResult scanResult = convertToEntity(request);
        scanResult = scanResultRepository.save(scanResult);
        
        // Persiste os resultados detalhados
        saveDataResults(scanResult, request);
        
        log.info("Scan processado com sucesso: {} - {} resultados encontrados", 
            request.getScanId(), request.getResults().size());
        
        return buildResponse(request.getScanId(), "SUCCESS", "Scan processado com sucesso");
    }

    /**
     * Valida se o consentimento foi concedido.
     * Para compliance com LGPD Art. 7º, não processamos dados sem opt-in explícito.
     * 
     * @throws ConsentNotGivenException se consent_given for false ou null
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

    /**
     * Converte DTO para entidade JPA.
     */
    private ScanResult convertToEntity(ScanResultRequest request, Long instanceId) {
        return ScanResult.builder()
            .scanId(request.getScanId())
            .instanceId(instanceId)
            .siteId(request.getSiteId())
            .consentGiven(request.getConsentGiven())
            .scanTimestampUtc(OffsetDateTime.parse(request.getScanTimestampUtc(), 
                DateTimeFormatter.ISO_DATE_TIME))
            .scanDurationMs(request.getScanDurationMs())
            .scannerVersion(request.getScannerVersion())
            .wpVersion(request.getEnvironment().getWpVersion())
            .phpVersion(request.getEnvironment().getPhpVersion())
            .receivedAt(OffsetDateTime.now())
            .build();
    }

    /**
     * Converte DTO para entidade JPA (MÉTODO LEGADO sem instanceId).
     * 
     * @deprecated Use {@link #convertToEntity(ScanResultRequest, Long)} ao invés
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    private ScanResult convertToEntity(ScanResultRequest request) {
        return convertToEntity(request, null);
    }

    /**
     * Persiste os resultados detalhados de dados encontrados.
     */
    private void saveDataResults(ScanResult scanResult, ScanResultRequest request) {
        List<DataResultEntity> entities = new ArrayList<>();
        
        for (var result : request.getResults()) {
            DataResultEntity entity = DataResultEntity.builder()
                .scanResult(scanResult)
                .dataType(result.getDataType())
                .sourceLocation(result.getSourceLocation())
                .count(result.getCount())
                .build();
            entities.add(entity);
        }
        
        dataResultRepository.saveAll(entities);
        log.debug("Persistidos {} data results para scan_id: {}", 
            entities.size(), scanResult.getScanId());
    }

    /**
     * Constrói resposta padronizada (MÉTODO LEGADO).
     * 
     * @deprecated Não mais utilizado na nova arquitetura (RF-API-2.3, RF-API-3.3)
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    private ScanResultResponse buildResponse(String scanId, String status, String message) {
        return ScanResultResponse.builder()
            .status(status)
            .message(message)
            .build();
    }
}
