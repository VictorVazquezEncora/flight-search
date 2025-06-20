package com.encora.victorvazquez.flights_search.features.flight.validation.validators;

import com.encora.victorvazquez.flights_search.features.flight.dto.FlightOffers.FlightOfferSearchRequestDTO;
import com.encora.victorvazquez.flights_search.features.flight.validation.annotations.ValidTotalTravelers;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TotalTravelersValidator implements ConstraintValidator<ValidTotalTravelers, FlightOfferSearchRequestDTO> {

    @Override
    public void initialize(ValidTotalTravelers constraintAnnotation) {
    }

    @Override
    public boolean isValid(FlightOfferSearchRequestDTO request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        int adults = request.adults() != null ? request.adults() : 0;
        int children = request.children() != null ? request.children() : 0;
        
        int totalSeatedTravelers = adults + children;
        
        if (totalSeatedTravelers > 9) {
            return false;
        }

        int infants = request.infants() != null ? request.infants() : 0;
        if (infants > adults) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "The number of infants cannot exceed the number of adults")
                .addConstraintViolation();
            return false;
        }

        return true;
    }
} 