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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para o endpoint de telemetria.
 * Valida todos os requisitos não funcionais (NFR-API-001 a NFR-API-004).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TelemetryControllerIntegrationTest {

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

    @Value("${radarlgpd.api.key}")
    private String validApiKey;

    @BeforeEach
    void setUp() {
        dataResultRepository.deleteAll();
        scanResultRepository.deleteAll();
        instanceRepository.deleteAll();
    }

    @Test
    @DisplayName("RF-API-3.0: Sem Authorization deve registrar nova instância (fluxo de registro)")
    void devRegistrarNovaInstanciaSemAuthorization() throws Exception {
        ScanResultRequest request = createValidRequest();

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("registered"))
            .andExpect(jsonPath("$.instance_token").exists());
    }

    @Test
    @DisplayName("NFR-API-001: Deve retornar 401 quando API Key é inválida")
    void deveRetornar401QuandoApiKeyInvalida() throws Exception {
        ScanResultRequest request = createValidRequest();

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer invalid-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("NFR-API-003: Deve retornar 403 quando consentimento não foi dado")
    void deveRetornar403QuandoConsentimentoNaoDado() throws Exception {
        ScanResultRequest request = createValidRequest();
        request.setConsentGiven(false);

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + validApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Consent Required"));
    }

    @Test
    @DisplayName("NFR-API-004: Deve retornar 400 para UUID inválido")
    void deveRetornar400ParaUuidInvalido() throws Exception {
        ScanResultRequest request = createValidRequest();
        request.setScanId("invalid-uuid");

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + validApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("NFR-API-004: Deve retornar 400 para timestamp inválido")
    void deveRetornar400ParaTimestampInvalido() throws Exception {
        ScanResultRequest request = createValidRequest();
        request.setScanTimestampUtc("2025-10-20 14:30:01"); // Formato errado

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + validApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("NFR-API-004: Deve retornar 400 para site_id inválido")
    void deveRetornar400ParaSiteIdInvalido() throws Exception {
        ScanResultRequest request = createValidRequest();
        request.setSiteId("abc123"); // Não é SHA256

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + validApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RF-API-2.0: Deve processar scan válido de instância autenticada com sucesso")
    void deveProcessarScanValidoComSucesso() throws Exception {
        // Cria instância pré-existente
        Instance instance = Instance.builder()
            .instanceToken(UUID.randomUUID().toString())
            .siteId("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
            .status("active")
            .scannerVersionAtRegistration("1.0.0")
            .scanCount(0)
            .build();
        instance = instanceRepository.save(instance);

        ScanResultRequest request = createValidRequest();

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + instance.getInstanceToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("received"))
            .andExpect(jsonPath("$.instance_token").doesNotExist()); // Não retorna token no fluxo autenticado
    }

    @Test
    @DisplayName("NFR-API-004: Deve rejeitar count negativo")
    void deveRejeitarCountNegativo() throws Exception {
        ScanResultRequest request = createValidRequest();
        request.getResults().get(0).setCount(-1);

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + validApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("NFR-API-004: Deve rejeitar data_type inválido")
    void deveRejeitarDataTypeInvalido() throws Exception {
        ScanResultRequest request = createValidRequest();
        request.getResults().get(0).setDataType("INVALID_TYPE");

        mockMvc.perform(post("/v1/telemetry/scan-result")
                .header("Authorization", "Bearer " + validApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Helper para criar uma requisição válida.
     */
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
                    .build(),
                DataResult.builder()
                    .dataType("EMAIL")
                    .sourceLocation("wp_users.user_email")
                    .count(310)
                    .build()
            ))
            .build();
    }
}
