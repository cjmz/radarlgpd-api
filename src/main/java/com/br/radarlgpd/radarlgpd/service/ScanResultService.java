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
     * Processa um scan result recebido do plugin WordPress.
     * 
     * @param request dados do scan
     * @return resposta com status do processamento
     * @throws ConsentNotGivenException se consentimento não foi dado
     */
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
    private ScanResult convertToEntity(ScanResultRequest request) {
        return ScanResult.builder()
            .scanId(request.getScanId())
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
     * Constrói resposta padronizada.
     */
    private ScanResultResponse buildResponse(String scanId, String status, String message) {
        return ScanResultResponse.builder()
            .scanId(scanId)
            .status(status)
            .message(message)
            .receivedAt(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .build();
    }
}
