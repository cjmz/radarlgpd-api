package com.br.radarlgpd.radarlgpd.service;

import com.br.radarlgpd.radarlgpd.entity.Instance;
import com.br.radarlgpd.radarlgpd.exception.InvalidInstanceTokenException;
import com.br.radarlgpd.radarlgpd.repository.InstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service para gerenciar instâncias do plugin WordPress.
 * Responsável por:
 * - Criar novas instâncias (RF-API-3.1)
 * - Validar tokens existentes (RF-API-2.1)
 * - Atualizar métricas de atividade
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstanceService {

    private final InstanceRepository instanceRepository;

    /**
     * Valida um token de instância e retorna a instância correspondente.
     * Usado no Fluxo Autenticado (RF-API-2.1).
     * 
     * @param token token recebido no header Authorization
     * @return instância válida
     * @throws InvalidInstanceTokenException se token inválido, inexistente ou banido
     */
    @Transactional(readOnly = true)
    public Instance validateAndGetInstance(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidInstanceTokenException("Token de instância não informado");
        }

        // Remove prefixo "Bearer " se presente
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token;

        Instance instance = instanceRepository.findByInstanceToken(cleanToken)
            .orElseThrow(() -> new InvalidInstanceTokenException(
                "Token de instância não encontrado ou inválido"
            ));

        // Verificação de status (RF-API-2.1)
        if (instance.isBanned()) {
            log.warn("Tentativa de uso de instância banida - instance_id: {}, site_id: {}", 
                instance.getId(), instance.getSiteId());
            throw new InvalidInstanceTokenException("Instância banida por comportamento suspeito");
        }

        if (!instance.isActive()) {
            log.warn("Tentativa de uso de instância inativa - instance_id: {}, status: {}", 
                instance.getId(), instance.getStatus());
            throw new InvalidInstanceTokenException("Instância inativa ou desativada");
        }

        log.debug("Token validado com sucesso - instance_id: {}, scan_count: {}", 
            instance.getId(), instance.getScanCount());

        return instance;
    }

    /**
     * Registra uma nova instância gerando um token único.
     * Usado no Fluxo de Registro (RF-API-3.1).
     * 
     * @param siteId hash SHA256 do domínio recebido do payload
     * @param scannerVersion versão do plugin no momento do registro
     * @return nova instância criada e persistida
     */
    @Transactional
    public Instance registerNewInstance(String siteId, String scannerVersion) {
        String instanceToken = generateUniqueToken();

        Instance instance = Instance.builder()
            .instanceToken(instanceToken)
            .siteId(siteId)
            .scannerVersionAtRegistration(scannerVersion)
            .status("active")
            .scanCount(0)
            .build();

        instance = instanceRepository.save(instance);

        log.info("Nova instância registrada - instance_id: {}, site_id: {}, scanner_version: {}", 
            instance.getId(), siteId, scannerVersion);

        return instance;
    }

    /**
     * Atualiza as métricas de atividade de uma instância.
     * Incrementa contador de scans e atualiza timestamp de última atividade.
     * 
     * @param instance instância a atualizar
     */
    @Transactional
    public void recordScanActivity(Instance instance) {
        instance.recordScan();
        instanceRepository.save(instance);

        log.debug("Atividade registrada - instance_id: {}, total_scans: {}", 
            instance.getId(), instance.getScanCount());
    }

    /**
     * Gera um token único (UUIDv4) garantindo que não existe duplicação.
     * Implementa retry em caso de colisão (extremamente improvável).
     * 
     * @return token único no formato UUIDv4
     */
    private String generateUniqueToken() {
        String token;
        int attempts = 0;
        final int maxAttempts = 5;

        do {
            token = UUID.randomUUID().toString();
            attempts++;

            if (attempts >= maxAttempts) {
                log.error("Falha ao gerar token único após {} tentativas", maxAttempts);
                throw new RuntimeException("Não foi possível gerar um token único");
            }

        } while (instanceRepository.existsByInstanceToken(token));

        log.debug("Token único gerado em {} tentativa(s)", attempts);
        return token;
    }

    /**
     * Bane uma instância por comportamento suspeito.
     * Útil para proteção contra abuso (ex: rate limit excessivo).
     * 
     * @param instanceId ID da instância a banir
     */
    @Transactional
    public void banInstance(Long instanceId) {
        Instance instance = instanceRepository.findById(instanceId)
            .orElseThrow(() -> new IllegalArgumentException("Instância não encontrada: " + instanceId));

        instance.setStatus("banned");
        instanceRepository.save(instance);

        log.warn("Instância banida - instance_id: {}, site_id: {}", 
            instanceId, instance.getSiteId());
    }
}
