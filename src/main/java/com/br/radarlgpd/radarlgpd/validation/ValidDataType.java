package com.br.radarlgpd.radarlgpd.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validação customizada para garantir que o data_type seja um valor permitido.
 */
@Documented
@Constraint(validatedBy = ValidDataTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDataType {
    
    String message() default "data_type deve ser um dos valores permitidos: CPF, CNPJ, EMAIL, TELEFONE, RG, CNH, NOME_COMPLETO, ENDERECO, DATA_NASCIMENTO, CARTAO_CREDITO";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
