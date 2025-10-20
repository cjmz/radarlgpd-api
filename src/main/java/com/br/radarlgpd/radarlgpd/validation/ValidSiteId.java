package com.br.radarlgpd.radarlgpd.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validação customizada para garantir que o site_id seja um hash SHA256 válido.
 */
@Documented
@Constraint(validatedBy = ValidSiteIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSiteId {
    
    String message() default "site_id deve ser um hash SHA256 válido (64 caracteres hexadecimais)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
