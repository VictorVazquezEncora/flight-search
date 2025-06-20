package com.encora.victorvazquez.flights_search.features.flight.service;

import com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers.FlightOfferSearch;
import com.encora.victorvazquez.flights_search.features.flight.domain.Locations.LocationSearch;
import com.encora.victorvazquez.flights_search.features.flight.dto.FlightOffers.FlightOffersProviderDTO;
import com.encora.victorvazquez.flights_search.features.flight.dto.Locations.LocationSearchResponseDTO;
import com.encora.victorvazquez.flights_search.features.flight.mapper.FlightOfferProviderMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import com.encora.victorvazquez.flights_search.features.flight.port.out.FlightOfferPort;
import com.encora.victorvazquez.flights_search.infrastructure.AmadeusClient;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightSearchService {
    private final FlightOfferPort flightOfferPort;
    private final FlightOfferProviderMapper flightOfferProviderMapper;
    private final AmadeusClient amadeusClient;

    public Mono<FlightOffersProviderDTO> searchFlights(FlightOfferSearch flightSearch) {
        if (flightSearch == null || flightSearch.originLocationCode() == null || 
            flightSearch.destinationLocationCode() == null || flightSearch.departureDate() == null) {
            return Mono.just(new FlightOffersProviderDTO(
                new FlightOffersProviderDTO.Meta(0, new FlightOffersProviderDTO.Links("")),
                new ArrayList<>(),
                new FlightOffersProviderDTO.Dictionaries(Map.of(), Map.of(), Map.of(), Map.of())
            ));
        }

        return flightOfferPort.searchFlights(flightSearch)
            .map(response -> flightOfferProviderMapper.toDTO(response));
    }

    public Mono<LocationSearchResponseDTO> searchLocations(LocationSearch locationSearch) {
        if (locationSearch == null || locationSearch.keyword() == null || locationSearch.keyword().trim().isEmpty()) {
            return Mono.just(new LocationSearchResponseDTO(
                new LocationSearchResponseDTO.Meta(0, new LocationSearchResponseDTO.Links(null, null)),
                new ArrayList<>()
            ));
        }

        return flightOfferPort.searchLocations(locationSearch).next();
    }
} 