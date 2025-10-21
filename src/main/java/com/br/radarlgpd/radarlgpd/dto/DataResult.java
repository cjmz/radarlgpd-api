package com.br.radarlgpd.radarlgpd.dto;

import com.br.radarlgpd.radarlgpd.validation.ValidDataType;
import com.br.radarlgpd.radarlgpd.validation.NoPersonalData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resultados agregados de dados encontrados.
 * IMPORTANTE: Apenas contagens agregadas, NUNCA dados pessoais reais.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado agregado de dados pessoais encontrados (apenas contagens)")
public class DataResult {

    @NotBlank(message = "data_type é obrigatório")
    @ValidDataType
    @Schema(
        description = "Tipo de dado pessoal detectado",
        example = "CPF",
        allowableValues = {"CPF", "EMAIL", "PHONE", "RG", "CNH", "CREDIT_CARD", "IP_ADDRESS", "OTHER"},
        required = true
    )
    private String dataType;

    @NotBlank(message = "source_location é obrigatório")
    @NoPersonalData
    @Schema(
        description = "Localização no banco de dados (formato: tabela.coluna)",
        example = "wp_comments.comment_content",
        required = true
    )
    private String sourceLocation;

    @NotNull(message = "count é obrigatório")
    @Min(value = 0, message = "count deve ser >= 0")
    @Schema(
        description = "Quantidade de ocorrências encontradas (apenas contagem, NUNCA os dados reais)",
        example = "152",
        minimum = "0",
        required = true
    )
    private Integer count;
}
