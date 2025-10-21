package com.br.radarlgpd.radarlgpd.dto;

import com.br.radarlgpd.radarlgpd.validation.ValidScanId;
import com.br.radarlgpd.radarlgpd.validation.ValidSiteId;
import com.br.radarlgpd.radarlgpd.validation.ValidTimestampUTC;
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
public class ScanResultRequest {

    @NotNull(message = "scan_id é obrigatório")
    @ValidScanId
    private String scanId;

    @NotBlank(message = "site_id é obrigatório")
    @ValidSiteId
    private String siteId;

    /**
     * Consentimento do usuário (LGPD Art. 7º).
     * Não usa @NotNull aqui para permitir que o controller retorne 403 (Forbidden)
     * ao invés de 400 (Bad Request) quando ausente.
     */
    private Boolean consentGiven;

    @NotBlank(message = "scan_timestamp_utc é obrigatório")
    @ValidTimestampUTC
    private String scanTimestampUtc;

    @NotNull(message = "scan_duration_ms é obrigatório")
    @Min(value = 0, message = "scan_duration_ms deve ser >= 0")
    private Integer scanDurationMs;

    @NotBlank(message = "scanner_version é obrigatório")
    @Pattern(
        regexp = "^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.-]+)?$",
        message = "scanner_version deve seguir SemVer (ex: 1.0.0-mvp)"
    )
    private String scannerVersion;

    @Valid
    @NotNull(message = "environment é obrigatório")
    private Environment environment;

    @Valid
    @NotEmpty(message = "results não pode ser vazio")
    private List<DataResult> results;
}
