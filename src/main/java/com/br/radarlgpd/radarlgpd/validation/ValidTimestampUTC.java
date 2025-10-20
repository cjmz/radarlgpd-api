package com.br.radarlgpd.radarlgpd.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validação customizada para garantir que o timestamp esteja em formato ISO 8601 UTC.
 */
@Documented
@Constraint(validatedBy = ValidTimestampUTCValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimestampUTC {
    
    String message() default "Timestamp deve estar em formato ISO 8601 UTC (ex: 2025-10-20T14:30:01Z)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
