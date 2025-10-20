package com.br.radarlgpd.radarlgpd.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validador para garantir que o data_type seja um valor permitido.
 */
public class ValidDataTypeValidator implements ConstraintValidator<ValidDataType, String> {

    private static final Set<String> ALLOWED_DATA_TYPES = Set.of(
        "CPF",
        "EMAIL", 
        "TELEFONE",
        "RG",
        "CNH",
        "NOME_COMPLETO",
        "ENDERECO",
        "DATA_NASCIMENTO",
        "CARTAO_CREDITO"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return ALLOWED_DATA_TYPES.contains(value.toUpperCase());
    }
}
