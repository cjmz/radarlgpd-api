package com.br.radarlgpd.radarlgpd.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validação customizada para detectar e rejeitar dados pessoais em campos de texto.
 * Segurança adicional contra envio acidental de dados sensíveis.
 */
@Documented
@Constraint(validatedBy = NoPersonalDataValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoPersonalData {
    
    String message() default "Campo contém possível dado pessoal. Apenas dados agregados são permitidos";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
