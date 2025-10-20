package com.br.radarlgpd.radarlgpd.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validação customizada para garantir que o scan_id seja um UUID válido RFC 4122.
 */
@Documented
@Constraint(validatedBy = ValidScanIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidScanId {
    
    String message() default "scan_id deve ser um UUID válido (RFC 4122)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
