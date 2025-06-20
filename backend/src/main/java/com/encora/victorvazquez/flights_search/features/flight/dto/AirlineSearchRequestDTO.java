package com.encora.victorvazquez.flights_search.features.flight.dto;

import jakarta.validation.constraints.*;

public record AirlineSearchRequestDTO(
    @NotEmpty
    @Pattern(regexp = "^[A-Z0-9]{2}$", message = "code must be a valid airline code")
    String code,

    @Min(1) @Max(100)
    Integer pageLimit,

    @Min(0)
    Integer pageOffset
) {} 