package com.encora.victorvazquez.flights_search.features.flight.port.out;

import com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers.FlightOfferSearch;
import com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers.FlightOfferProviderResponse;
import com.encora.victorvazquez.flights_search.features.flight.domain.Locations.LocationSearch;
import com.encora.victorvazquez.flights_search.features.flight.dto.Locations.LocationSearchResponseDTO;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface FlightOfferPort {
    Mono<FlightOfferProviderResponse> searchFlights(FlightOfferSearch request);
    Flux<LocationSearchResponseDTO> searchLocations(LocationSearch locationSearch);
} 