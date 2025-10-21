package com.br.radarlgpd.radarlgpd.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ScanResultResponse {

    /**
     * Status do processamento:
     * - "received": Telemetria recebida de instância já registrada (RF-API-2.3)
     * - "registered": Nova instância registrada com sucesso (RF-API-3.3)
     */
    private String status;
    
    /**
     * Token da instância (presente APENAS no fluxo de registro - RF-API-3.3).
     * UUIDv4 gerado pela API para autenticação futura.
     * 
     * @JsonInclude(NON_NULL) garante que este campo só aparece quando preenchido.
     */
    @JsonProperty("instance_token")
    private String instanceToken;
    
    /**
     * Mensagem adicional (opcional).
     */
    private String message;
}
