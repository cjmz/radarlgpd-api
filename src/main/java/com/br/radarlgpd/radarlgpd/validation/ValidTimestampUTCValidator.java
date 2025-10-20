package com.br.radarlgpd.radarlgpd.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validador para garantir que o timestamp esteja em formato ISO 8601 UTC.
 */
public class ValidTimestampUTCValidator implements ConstraintValidator<ValidTimestampUTC, String> {

    // Padrão ISO 8601 básico: yyyy-MM-ddTHH:mm:ssZ
    private static final Pattern ISO_8601_PATTERN = Pattern.compile(
        "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        
        // Valida formato
        if (!ISO_8601_PATTERN.matcher(value).matches()) {
            return false;
        }
        
        // Valida se é uma data válida
        try {
            OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
