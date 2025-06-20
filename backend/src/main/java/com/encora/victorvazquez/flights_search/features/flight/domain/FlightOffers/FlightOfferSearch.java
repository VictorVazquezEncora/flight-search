package com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers;

import java.time.LocalDate;
import java.util.List;

public record FlightOfferSearch(
    String originLocationCode,
    String destinationLocationCode,
    LocalDate departureDate,
    LocalDate returnDate,
    int adults,
    int children,
    int infants,
    String travelClass,
    List<String> includedAirlineCodes,
    List<String> excludedAirlineCodes,
    Boolean nonStop,
    String currencyCode,
    Integer maxPrice,
    int max
) {} 