package com.encora.victorvazquez.flights_search.features.flight.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

import com.encora.victorvazquez.flights_search.features.flight.validation.validators.DateRangeValidator;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@Documented
public @interface ValidDateRange {
    String message() default "Return date must be after departure date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}