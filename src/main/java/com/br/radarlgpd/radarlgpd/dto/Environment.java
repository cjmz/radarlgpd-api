package com.br.radarlgpd.radarlgpd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para informações sobre o ambiente do site escaneado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informações sobre o ambiente WordPress do site")
public class Environment {

    @NotBlank(message = "wp_version é obrigatório")
    @Schema(
        description = "Versão do WordPress instalada",
        example = "6.4.0",
        required = true
    )
    private String wpVersion;

    @NotBlank(message = "php_version é obrigatório")
    @Schema(
        description = "Versão do PHP do servidor",
        example = "8.2.0",
        required = true
    )
    private String phpVersion;
}
