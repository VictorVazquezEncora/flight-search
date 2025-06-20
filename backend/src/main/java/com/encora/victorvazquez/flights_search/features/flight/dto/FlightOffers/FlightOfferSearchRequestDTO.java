package com.encora.victorvazquez.flights_search.features.flight.dto.FlightOffers;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import com.encora.victorvazquez.flights_search.features.flight.validation.annotations.ValidDateRange;
import com.encora.victorvazquez.flights_search.features.flight.validation.annotations.ValidTotalTravelers;

@ValidDateRange(message = "Return date must be after departure date")
@ValidTotalTravelers(message = "The total number of seated travelers (adults + children) cannot exceed 9")
public record FlightOfferSearchRequestDTO(
    @NotNull(message = "Origin IATA code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Origin code must be a valid 3-letter IATA code in uppercase")
    String originLocationCode,

    @NotNull(message = "Destination IATA code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Destination code must be a valid 3-letter IATA code in uppercase")
    String destinationLocationCode,

    @NotNull(message = "Departure date is required")
    @Future(message = "Departure date must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate,

    @Future(message = "Return date must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate,

    @NotNull(message = "Number of adults is required")
    @Min(value = 1, message = "There must be at least 1 adult")
    @Max(value = 9, message = "There cannot be more than 9 adults")
    Integer adults,

    @Min(value = 0, message = "Number of children cannot be negative")
    @Max(value = 9, message = "There cannot be more than 9 children")
    Integer children,

    @Min(value = 0, message = "Number of infants cannot be negative")
    @Max(value = 9, message = "There cannot be more than 9 infants")
    Integer infants,

    @Pattern(regexp = "^(ECONOMY|PREMIUM_ECONOMY|BUSINESS|FIRST)$", 
            message = "Class must be ECONOMY, PREMIUM_ECONOMY, BUSINESS or FIRST")
    String travelClass,

    @Pattern(regexp = "^[A-Z0-9]+(,[A-Z0-9]+)*$", 
            message = "Included airline codes must be valid IATA codes separated by commas")
    String includedAirlineCodes,

    @Pattern(regexp = "^[A-Z0-9]+(,[A-Z0-9]+)*$", 
            message = "Excluded airline codes must be valid IATA codes separated by commas")
    String excludedAirlineCodes,

    Boolean nonStop,

    @Pattern(regexp = "^(USD|MXN|EUR)$", 
            message = "Currency must be USD, MXN or EUR")
    String currencyCode,

    @Min(value = 0, message = "Maximum price cannot be negative")
    Integer maxPrice,

    @Min(value = 1, message = "Maximum results must be at least 1")
    @Max(value = 250, message = "Maximum results cannot exceed 250")
    Integer max
) {} 