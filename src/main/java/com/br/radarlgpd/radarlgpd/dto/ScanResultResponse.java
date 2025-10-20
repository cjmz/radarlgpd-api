package com.br.radarlgpd.radarlgpd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta para o endpoint de telemetria.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultResponse {

    private String scanId;
    
    private String status;
    
    private String message;
    
    private String receivedAt;
}
