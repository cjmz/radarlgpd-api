package com.br.radarlgpd.radarlgpd.dto;

import com.br.radarlgpd.radarlgpd.validation.ValidScanId;
import com.br.radarlgpd.radarlgpd.validation.ValidSiteId;
import com.br.radarlgpd.radarlgpd.validation.ValidTimestampUTC;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para receber requisições de scan results do plugin WordPress.
 * Segue estritamente o schema aprovado para compliance com LGPD.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados agregados de scan do plugin WordPress (apenas contagens, NUNCA dados pessoais)")
public class ScanResultRequest {

    @NotNull(message = "scan_id é obrigatório")
    @ValidScanId
    @Schema(
        description = "ID único do scan (UUIDv4 gerado pelo plugin)",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        required = true
    )
    private String scanId;

    @NotBlank(message = "site_id é obrigatório")
    @ValidSiteId
    @Schema(
        description = "Hash SHA-256 do domínio + salt (para anonimização)",
        example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        pattern = "^[a-f0-9]{64}$",
        required = true
    )
    private String siteId;

    /**
     * Consentimento do usuário (LGPD Art. 7º).
     * Não usa @NotNull aqui para permitir que o controller retorne 403 (Forbidden)
     * ao invés de 400 (Bad Request) quando ausente.
     */
    @Schema(
        description = "Consentimento do usuário para envio de telemetria (LGPD Art. 7º). OBRIGATÓRIO = true",
        example = "true",
        required = true
    )
    private Boolean consentGiven;

    @NotBlank(message = "scan_timestamp_utc é obrigatório")
    @ValidTimestampUTC
    @Schema(
        description = "Timestamp UTC do início do scan (ISO 8601)",
        example = "2025-10-20T21:20:00Z",
        pattern = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$",
        required = true
    )
    private String scanTimestampUtc;

    @NotNull(message = "scan_duration_ms é obrigatório")
    @Min(value = 0, message = "scan_duration_ms deve ser >= 0")
    @Schema(
        description = "Duração do scan em milissegundos",
        example = "1500",
        minimum = "0",
        required = true
    )
    private Integer scanDurationMs;

    @NotBlank(message = "scanner_version é obrigatório")
    @Pattern(
        regexp = "^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.-]+)?$",
        message = "scanner_version deve seguir SemVer (ex: 1.0.0-mvp)"
    )
    @Schema(
        description = "Versão do plugin WordPress scanner (Semantic Versioning)",
        example = "1.0.0-mvp",
        pattern = "^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.-]+)?$",
        required = true
    )
    private String scannerVersion;

    @Valid
    @NotNull(message = "environment é obrigatório")
    @Schema(description = "Informações sobre o ambiente WordPress", required = true)
    private Environment environment;

    @Valid
    @NotEmpty(message = "results não pode ser vazio")
    @Schema(
        description = "Resultados agregados do scan (apenas contagens, NUNCA dados pessoais)",
        required = true,
        minLength = 1
    )
    private List<DataResult> results;
}
