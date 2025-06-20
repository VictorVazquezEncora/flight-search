package com.encora.victorvazquez.flights_search.features.flight.controller;

import com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers.FlightOfferSearch;
import com.encora.victorvazquez.flights_search.features.flight.domain.Locations.LocationSearch;
import com.encora.victorvazquez.flights_search.features.flight.dto.FlightOffers.FlightOfferSearchRequestDTO;
import com.encora.victorvazquez.flights_search.features.flight.dto.FlightOffers.FlightOffersProviderDTO;
import com.encora.victorvazquez.flights_search.features.flight.dto.Locations.LocationSearchResponseDTO;
import com.encora.victorvazquez.flights_search.features.flight.dto.Locations.LocationSearchRequestDTO;
import com.encora.victorvazquez.flights_search.features.flight.mapper.FlightSearchMapper;
import com.encora.victorvazquez.flights_search.features.flight.mapper.Locations.LocationSearchMapper;
import com.encora.victorvazquez.flights_search.features.flight.service.FlightSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class FlightSearchController {
    private final FlightSearchService flightSearchService;
    private final FlightSearchMapper flightSearchMapper;
    private final LocationSearchMapper locationSearchMapper;

    @GetMapping("/flights")
    public Mono<FlightOffersProviderDTO> searchFlights(@Valid FlightOfferSearchRequestDTO request) {
        FlightOfferSearch domain = flightSearchMapper.toDomain(request);
        return flightSearchService.searchFlights(domain);
    }

    @GetMapping("/locations")
    public Mono<LocationSearchResponseDTO> searchLocations(@Valid LocationSearchRequestDTO request) {
        LocationSearch domain = locationSearchMapper.toDomain(request);
        return flightSearchService.searchLocations(domain);
    }
}