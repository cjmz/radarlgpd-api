package com.br.radarlgpd.radarlgpd.dto;

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
public class Environment {

    @NotBlank(message = "wp_version é obrigatório")
    private String wpVersion;

    @NotBlank(message = "php_version é obrigatório")
    private String phpVersion;
}
