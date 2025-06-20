package com.encora.victorvazquez.flights_search.features.flight.mapper.Locations;

import com.encora.victorvazquez.flights_search.features.flight.domain.Locations.LocationSearch;
import com.encora.victorvazquez.flights_search.features.flight.dto.Locations.LocationSearchRequestDTO;

import org.springframework.stereotype.Component;

@Component
public class LocationSearchMapper {

    public LocationSearch toDomain(LocationSearchRequestDTO dto) {
        return new LocationSearch(
            dto.subType(),
            dto.keyword() != null ? dto.keyword().trim() : null,
            dto.countryCode(),
            dto.pageLimit(),
            dto.pageOffset(),
            dto.sort(),
            dto.view()
        );
    }
} 