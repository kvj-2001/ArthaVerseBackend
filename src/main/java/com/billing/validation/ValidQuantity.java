package com.billing.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QuantityValidator.class)
@Documented
public @interface ValidQuantity {
    String message() default "Invalid quantity for unit type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}