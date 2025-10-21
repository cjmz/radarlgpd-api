package com.br.radarlgpd.radarlgpd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO padrão para respostas de erro da API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta padrão de erro da API")
public class ErrorResponse {
    
    @Schema(description = "Timestamp do erro (ISO 8601)", example = "2025-10-20T21:20:00Z")
    private String timestamp;
    
    @Schema(description = "Código HTTP do erro", example = "400")
    private Integer status;
    
    @Schema(description = "Nome do erro", example = "Bad Request")
    private String error;
    
    @Schema(description = "Mensagem descritiva do erro", example = "Payload inválido")
    private String message;
    
    @Schema(description = "Path da requisição", example = "/v1/telemetry/scan-result")
    private String path;
    
    @Schema(description = "Lista de erros de validação de campos (se aplicável)", nullable = true)
    private List<FieldError> errors;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Erro de validação de campo")
    public static class FieldError {
        @Schema(description = "Nome do campo com erro", example = "scanId")
        private String field;
        
        @Schema(description = "Mensagem de erro do campo", example = "scan_id deve ser um UUID válido")
        private String message;
    }
}
