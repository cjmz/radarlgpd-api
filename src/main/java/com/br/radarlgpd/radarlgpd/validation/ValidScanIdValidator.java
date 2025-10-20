package com.br.radarlgpd.radarlgpd.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador para garantir que o scan_id seja um UUID válido (RFC 4122).
 */
public class ValidScanIdValidator implements ConstraintValidator<ValidScanId, String> {

    // Padrão UUID RFC 4122: 8-4-4-4-12 caracteres hexadecimais
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return UUID_PATTERN.matcher(value).matches();
    }
}
