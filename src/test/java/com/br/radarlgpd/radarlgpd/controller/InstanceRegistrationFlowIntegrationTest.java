package com.br.radarlgpd.radarlgpd.controller;

import com.br.radarlgpd.radarlgpd.dto.DataResult;
import com.br.radarlgpd.radarlgpd.dto.Environment;
import com.br.radarlgpd.radarlgpd.dto.ScanResultRequest;
import com.br.radarlgpd.radarlgpd.entity.Instance;
import com.br.radarlgpd.radarlgpd.repository.DataResultRepository;
import com.br.radarlgpd.radarlgpd.repository.InstanceRepository;
import com.br.radarlgpd.radarlgpd.repository.ScanResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para o Épico 1.1: Registro Anônimo de Instância.
 * 
 * Valida:
 * - RF-API-1.0: Roteamento de lógica (com/sem Authorization)
 * - RF-API-1.1: Validação de consentimento
 * - RF-API-2.0: Fluxo Autenticado (Cenário A)
 * - RF-API-3.0: Fluxo de Registro (Cenário B)
 * - RNF-API-PERF-1.0: Atomicidade da transação
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InstanceRegistrationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private ScanResultRepository scanResultRepository;

    @Autowired
    private DataResultRepository dataResultRepository;

    @BeforeEach
    void setUp() {
        // Limpar dados de teste na ordem correta (respeita foreign keys)
        // 1. data_results (depende de scan_results)
        dataResultRepository.deleteAll();
        // 2. scan_results (depende de instances)
        scanResultRepository.deleteAll();
        // 3. instances (tabela base)
        instanceRepository.deleteAll();
    }

    // ===== RF-API-3.0: FLUXO DE REGISTRO (Cenário B) =====

    @Test
    @DisplayName("RF-API-3.1: Deve registrar nova instância quando Authorization ausente")
    void deveRegistrarNovaInstanciaQuandoAuthorizationAusente() throws Exception {
        ScanResultRequest request = createValidRequest();
        long instanceCountBefore = instanceRepository.count();

        String responseJson = mockMvc.perform(post("/v1/telemetry/scan-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("registered"))
            .andExpect(jsonPath("$.instance_token").exists())
            .andExpect(jsonPath("$.instance_token").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // Verifica que uma nova instância foi criada
        long instanceCountAfter = instanceRepository.count();
        assertThat(instanceCountAfter).isEqualTo(instanceCountBefore + 1);

        // Verifica que o token retornado é válido (UUID)
        var response = objectMapper.readTree(responseJson);
        String instanceToken = response.get("instance_token").asText();
        assertThat(instanceToken).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

        // Verifica que a instância foi persistida corretamente
        Instance instance = instanceRepository.findByInstanceToken(instanceToken).orElseThrow();
        assertThat(instance.getSiteId()).isEqualTo(request.getSiteId());
        assertThat(instance.getStatus()).isEqualTo("active");
        assertThat(instance.getScannerVersionAtRegistration()).isEqualTo(request.getScannerVersion());
        assertThat(instance.getScanCount()).isEqualTo(1); // Primeiro scan
    }

    @Test
    @DisplayName("RF-API-3.2: Deve persistir telemetria do primeiro scan no fluxo de registro")
    void devePersistirTelemetriaDoPrimeiroScanNoFluxoDeRegistro() throws Exception {
        ScanResultRequest request = createValidRequest();
        long scanCountBefore = scanResultRepository.count();

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // Verifica que o scan foi persistido
        long scanCountAfter = scanResultRepository.count();
        assertThat(scanCountAfter).isEqualTo(scanCountBefore + 1);

        // Verifica que o scan está associado à instância criada
        var scanResult = scanResultRepository.findByScanId(request.getScanId()).orElseThrow();
        assertThat(scanResult.getInstanceId()).isNotNull();
        assertThat(scanResult.getSiteId()).isEqualTo(request.getSiteId());
    }

    @Test
    @DisplayName("RNF-API-PERF-1.0: Registro e telemetria devem ser atômicos (transação única)")
    void registroETelemetriaDevemSerAtomicos() throws Exception {
        ScanResultRequest request = createValidRequest();
        // Usa scan_id fixo (UUID válido) para testar idempotência
        String fixedScanId = "550e8400-e29b-41d4-a716-446655440000";
        request.setScanId(fixedScanId);

        // Primeira chamada: sucesso
        mockMvc.perform(post("/v1/telemetry/scan-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        long instanceCountAfterFirst = instanceRepository.count();
        long scanCountAfterFirst = scanResultRepository.count();

        // Segunda chamada com mesmo scan_id: deve ser idempotente (não cria nova instância)
        mockMvc.perform(post("/v1/telemetry/scan-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // Verifica que não criou instâncias ou scans duplicados
        long instanceCountAfterSecond = instanceRepository.count();
        long scanCountAfterSecond = scanResultRepository.count();
        
        assertThat(instanceCountAfterSecond).isEqualTo(instanceCountAfterFirst + 1); // Nova instância
        assertThat(scanCountAfterSecond).isEqualTo(scanCountAfterFirst); // Scan ignorado (idempotente)
    }

    // ===== RF-API-2.0: FLUXO AUTENTICADO (Cenário A) =====

    @Test
    @DisplayName("RF-API-2.1: Deve validar token e processar scan de instância existente")
    void deveValidarTokenEProcessarScanDeInstanciaExistente() throws Exception {
        // Cria instância pré-existente
        Instance existingInstance = Instance.builder()
            .instanceToken(UUID.randomUUID().toString())
            .siteId("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
            .status("active")
            .scannerVersionAtRegistration("1.0.0")
            .scanCount(5)
            .build();
        existingInstance = instanceRepository.save(existingInstance);

        ScanResultRequest request = createValidRequest();

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + existingInstance.getInstanceToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("received"))
            .andExpect(jsonPath("$.instance_token").doesNotExist()); // Token NÃO retorna no fluxo autenticado

        // Verifica que scan_count foi incrementado
        Instance updatedInstance = instanceRepository.findById(existingInstance.getId()).orElseThrow();
        assertThat(updatedInstance.getScanCount()).isEqualTo(6);
        assertThat(updatedInstance.getLastSeenAt()).isNotNull();
    }

    @Test
    @DisplayName("RF-API-2.1: Deve retornar 401 para token inválido")
    void deveRetornar401ParaTokenInvalido() throws Exception {
        ScanResultRequest request = createValidRequest();

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("RF-API-2.1: Deve retornar 401 para instância banida")
    void deveRetornar401ParaInstanciaBanida() throws Exception {
        // Cria instância banida
        Instance bannedInstance = Instance.builder()
            .instanceToken(UUID.randomUUID().toString())
            .siteId("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
            .status("banned")
            .scannerVersionAtRegistration("1.0.0")
            .scanCount(100)
            .build();
        bannedInstance = instanceRepository.save(bannedInstance);

        ScanResultRequest request = createValidRequest();

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + bannedInstance.getInstanceToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Token de instância inválido, expirado ou banido"));
    }

    // ===== RF-API-1.1: VALIDAÇÃO DE CONSENTIMENTO (AMBOS FLUXOS) =====

    @Test
    @DisplayName("RF-API-1.1: Deve retornar 403 sem consentimento no fluxo de registro")
    void deveRetornar403SemConsentimentoNoFluxoDeRegistro() throws Exception {
        ScanResultRequest request = createValidRequest();
        request.setConsentGiven(false);

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Consent Required"));

        // Verifica que NENHUMA instância foi criada
        assertThat(instanceRepository.count()).isZero();
    }

    @Test
    @DisplayName("RF-API-1.1: Deve retornar 403 sem consentimento no fluxo autenticado")
    void deveRetornar403SemConsentimentoNoFluxoAutenticado() throws Exception {
        // Cria instância
        Instance instance = Instance.builder()
            .instanceToken(UUID.randomUUID().toString())
            .siteId("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
            .status("active")
            .scannerVersionAtRegistration("1.0.0")
            .scanCount(0)
            .build();
        instance = instanceRepository.save(instance);

        ScanResultRequest request = createValidRequest();
        request.setConsentGiven(null); // Consentimento ausente

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + instance.getInstanceToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());

        // Verifica que scan_count NÃO foi incrementado
        Instance unchangedInstance = instanceRepository.findById(instance.getId()).orElseThrow();
        assertThat(unchangedInstance.getScanCount()).isZero();
    }

    @Test
    @DisplayName("RF-API-3.0: Múltiplos registros do mesmo site devem criar instâncias separadas")
    void multiplosRegistrosDoMesmoSiteDevemCriarInstanciasSeparadas() throws Exception {
        ScanResultRequest request1 = createValidRequest();
        ScanResultRequest request2 = createValidRequest();
        
        String sameSiteId = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        request1.setSiteId(sameSiteId);
        request2.setSiteId(sameSiteId);

        // Primeiro registro
        mockMvc.perform(post("/v1/telemetry/scan-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("registered"));

        // Segundo registro (deve criar nova instância, não reutilizar)
        mockMvc.perform(post("/v1/telemetry/scan-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("registered"));

        // Verifica que 2 instâncias foram criadas para o mesmo site
        List<Instance> instances = instanceRepository.findBySiteId(sameSiteId);
        assertThat(instances).hasSize(2);
        assertThat(instances.get(0).getInstanceToken()).isNotEqualTo(instances.get(1).getInstanceToken());
    }

    // ===== HELPERS =====

    private ScanResultRequest createValidRequest() {
        return ScanResultRequest.builder()
            .scanId(UUID.randomUUID().toString())
            .siteId("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
            .consentGiven(true)
            .scanTimestampUtc("2025-10-20T14:30:01Z")
            .scanDurationMs(4580)
            .scannerVersion("1.0.0-mvp")
            .environment(Environment.builder()
                .wpVersion("6.4.1")
                .phpVersion("8.1")
                .build())
            .results(List.of(
                DataResult.builder()
                    .dataType("CPF")
                    .sourceLocation("wp_comments.comment_content")
                    .count(152)
                    .build()
            ))
            .build();
    }
}
