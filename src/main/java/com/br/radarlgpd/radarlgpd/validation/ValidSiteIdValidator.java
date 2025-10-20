package com.br.radarlgpd.radarlgpd.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador para garantir que o site_id seja um hash SHA256 válido.
 * Para compliance com LGPD, não aceitamos domínios em texto claro.
 */
public class ValidSiteIdValidator implements ConstraintValidator<ValidSiteId, String> {

    // SHA256 hash: exatamente 64 caracteres hexadecimais
    private static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return SHA256_PATTERN.matcher(value).matches();
    }
}
