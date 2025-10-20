package com.br.radarlgpd.radarlgpd.dto;

import com.br.radarlgpd.radarlgpd.validation.ValidDataType;
import com.br.radarlgpd.radarlgpd.validation.NoPersonalData;
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
public class DataResult {

    @NotBlank(message = "data_type é obrigatório")
    @ValidDataType
    private String dataType;

    @NotBlank(message = "source_location é obrigatório")
    @NoPersonalData
    private String sourceLocation;

    @NotNull(message = "count é obrigatório")
    @Min(value = 0, message = "count deve ser >= 0")
    private Integer count;
}
