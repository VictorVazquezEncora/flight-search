package com.encora.victorvazquez.flights_search.features.flight.dto.Locations;

import jakarta.validation.constraints.*;

public record LocationSearchRequestDTO(
    @NotEmpty
    @Pattern(regexp = "^(AIRPORT|CITY)$", message = "subType must be either AIRPORT or CITY")
    String subType,

    @NotEmpty
    @Pattern(regexp = "^[A-Za-z0-9./:()'\"_\\s\\-]+$", message = "keyword contains unsupported characters")
    String keyword,

    @Pattern(regexp = "^[A-Z]{2}$", message = "countryCode must be a valid ISO 3166-1 alpha-2 code")
    String countryCode,

    @Min(1) @Max(100)
    Integer pageLimit,

    @Min(0)
    Integer pageOffset,

    @Pattern(regexp = "^analytics\\.travelers\\.score$", message = "sort must be analytics.travelers.score")
    String sort,

    @Pattern(regexp = "^(LIGHT|FULL)$", message = "view must be either LIGHT or FULL")
    String view
) {} 