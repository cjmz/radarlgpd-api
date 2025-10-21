package com.br.radarlgpd.radarlgpd.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta para o endpoint de telemetria.
 * Suporta dois cenários (RF-API-2.3 e RF-API-3.3):
 * 
 * Cenário A (Autenticado): { "status": "received" }
 * Cenário B (Registro): { "status": "registered", "instance_token": "uuid..." }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta do endpoint de telemetria (varia conforme fluxo: autenticado ou registro)")
public class ScanResultResponse {

    /**
     * Status do processamento:
     * - "received": Telemetria recebida de instância já registrada (RF-API-2.3)
     * - "registered": Nova instância registrada com sucesso (RF-API-3.3)
     */
    @Schema(
        description = "Status do processamento",
        example = "received",
        allowableValues = {"received", "registered"},
        required = true
    )
    private String status;
    
    /**
     * Token da instância (presente APENAS no fluxo de registro - RF-API-3.3).
     * UUIDv4 gerado pela API para autenticação futura.
     * 
     * @JsonInclude(NON_NULL) garante que este campo só aparece quando preenchido.
     */
    @JsonProperty("instance_token")
    @Schema(
        description = "Token único da instância (UUIDv4). Presente APENAS no primeiro scan (registro). Use este token no header Authorization para scans futuros.",
        example = "9f8c7b6a-5d4e-3c2b-1a0f-9e8d7c6b5a4f",
        format = "uuid",
        nullable = true
    )
    private String instanceToken;
    
    /**
     * Mensagem adicional (opcional).
     */
    @Schema(
        description = "Mensagem adicional (opcional)",
        example = "Scan processado com sucesso",
        nullable = true
    )
    private String message;
}
