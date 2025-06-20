package com.encora.victorvazquez.flights_search.features.flight.domain.Locations;

public record LocationSearch(
    String subType,
    String keyword,
    String countryCode,
    Integer pageLimit,
    Integer pageOffset,
    String sort,
    String view
) {} 