package com.encora.victorvazquez.flights_search.features.flight.validation.validators;
import com.encora.victorvazquez.flights_search.features.flight.dto.FlightOffers.FlightOfferSearchRequestDTO;
import com.encora.victorvazquez.flights_search.features.flight.validation.annotations.ValidDateRange;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, FlightOfferSearchRequestDTO> {

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
    }

    @Override
    public boolean isValid(FlightOfferSearchRequestDTO request, ConstraintValidatorContext context) {
        if (request.returnDate() == null) {
            // If return date is null, it's a one-way flight, so it's valid
            return true;
        }

        return request.departureDate().isBefore(request.returnDate()) || 
               request.departureDate().isEqual(request.returnDate());
    }
} 