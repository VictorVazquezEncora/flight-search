package com.encora.victorvazquez.flights_search.features.flight.mapper;

import com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers.FlightOfferSearch;
import com.encora.victorvazquez.flights_search.features.flight.dto.FlightOffers.FlightOfferSearchRequestDTO;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class FlightSearchMapper {

    public FlightOfferSearch toDomain(FlightOfferSearchRequestDTO dto) {
        return new FlightOfferSearch(
            dto.originLocationCode(),
            dto.destinationLocationCode(),
            dto.departureDate(),
            dto.returnDate(),
            dto.adults() != null ? dto.adults() : 1,
            dto.children() != null ? dto.children() : 0,
            dto.infants() != null ? dto.infants() : 0,
            dto.travelClass(),
            parseAirlineCodes(dto.includedAirlineCodes()),
            parseAirlineCodes(dto.excludedAirlineCodes()),
            dto.nonStop(),
            dto.currencyCode(),
            dto.maxPrice(),
            dto.max() != null ? dto.max() : 10
        );
    }

    private List<String> parseAirlineCodes(String codes) {
        if (codes == null || codes.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(codes.split(","));
    }
} 