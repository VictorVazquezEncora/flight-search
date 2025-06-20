package com.encora.victorvazquez.flights_search.features.flight.mapper.Locations;

import com.encora.victorvazquez.flights_search.features.flight.domain.Locations.Location;
import com.encora.victorvazquez.flights_search.features.flight.domain.Locations.LocationProviderResponse;
import com.encora.victorvazquez.flights_search.features.flight.dto.Locations.LocationSearchResponseDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class LocationResponseMapper {

    public LocationProviderResponse toDomain(LocationSearchResponseDTO dto) {
        return new LocationProviderResponse(
            mapMeta(dto.meta()),
            dto.data().stream()
                .map(this::mapLocation)
                .collect(Collectors.toList())
        );
    }

    private LocationProviderResponse.Meta mapMeta(LocationSearchResponseDTO.Meta meta) {
        return new LocationProviderResponse.Meta(
            meta.count(),
            new LocationProviderResponse.Meta.Links(
                meta.links().href()
            )
        );
    }

    private Location mapLocation(LocationSearchResponseDTO.LocationDTO dto) {
        return new Location(
            dto.id(),
            mapLinks(dto.self()),
            dto.type(),
            dto.subType(),
            dto.name(),
            dto.detailedName(),
            dto.timeZoneOffset(),
            dto.iataCode(),
            mapGeoCode(dto.geoCode()),
            mapAddress(dto.address()),
            mapDistance(dto.distance()),
            mapAnalytics(dto.analytics()),
            dto.relevance(),
            dto.category(),
            dto.tags(),
            dto.rank()
        );
    }

    private Location.Links mapLinks(LocationSearchResponseDTO.Links links) {
        return new Location.Links(
            links.href(),
            links.methods(),
            null
        );
    }

    private Location.GeoCode mapGeoCode(LocationSearchResponseDTO.GeoCode geoCode) {
        return new Location.GeoCode(
            geoCode.latitude(),
            geoCode.longitude()
        );
    }

    private Location.Address mapAddress(LocationSearchResponseDTO.Address address) {
        return new Location.Address(
            address.cityName(),
            address.cityCode(),
            address.countryName(),
            address.countryCode(),
            null,
            address.regionCode()
        );
    }

    private Location.Distance mapDistance(LocationSearchResponseDTO.Distance distance) {
        return new Location.Distance(
            (int) distance.value(),
            distance.unit()
        );
    }

    private Location.Analytics mapAnalytics(LocationSearchResponseDTO.Analytics analytics) {
        return new Location.Analytics(
            new Location.Analytics.Travelers(
                analytics.travelers().score()
            )
        );
    }
}
