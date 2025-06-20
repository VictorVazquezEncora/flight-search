package com.encora.victorvazquez.flights_search.features.flight.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

import com.encora.victorvazquez.flights_search.features.flight.validation.validators.TotalTravelersValidator;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TotalTravelersValidator.class)
@Documented
public @interface ValidTotalTravelers {
    String message() default "The total number of seated travelers cannot exceed 9";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 