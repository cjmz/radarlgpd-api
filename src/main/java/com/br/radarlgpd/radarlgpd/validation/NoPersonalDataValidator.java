package com.br.radarlgpd.radarlgpd.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador para detectar e rejeitar possíveis dados pessoais em campos de texto.
 * Implementa verificações de padrões comuns de dados sensíveis.
 */
public class NoPersonalDataValidator implements ConstraintValidator<NoPersonalData, String> {

    // Padrões de dados pessoais que NÃO devem estar presentes
    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Outros validadores lidam com nulo/vazio
        }

        // Se encontrar qualquer padrão de dado pessoal, invalida
        if (CPF_PATTERN.matcher(value).find()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Campo contém possível CPF")
                   .addConstraintViolation();
            return false;
        }

        if (EMAIL_PATTERN.matcher(value).find()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Campo contém possível email")
                   .addConstraintViolation();
            return false;
        }

        if (PHONE_PATTERN.matcher(value).find()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Campo contém possível telefone")
                   .addConstraintViolation();
            return false;
        }

        if (CREDIT_CARD_PATTERN.matcher(value).find()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Campo contém possível número de cartão")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}
