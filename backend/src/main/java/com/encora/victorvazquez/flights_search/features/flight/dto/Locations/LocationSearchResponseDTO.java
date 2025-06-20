package com.encora.victorvazquez.flights_search.features.flight.dto.Locations;

import java.util.List;

public record LocationSearchResponseDTO(
    Meta meta,
    List<LocationDTO> data
) {
    
    public record Meta(
        int count,
        Links links
    ) {}

    public record LocationDTO(
        String id,
        Links self,
        String type,
        String subType,
        String name,
        String detailedName,
        String timeZoneOffset,
        String iataCode,
        GeoCode geoCode,
        Address address,
        Distance distance,
        Analytics analytics,
        double relevance,
        String category,
        List<String> tags,
        String rank
    ) {}

    public record Links(
        String href,
        List<String> methods
    ) {}

    public record GeoCode(
        double latitude,
        double longitude
    ) {}

    public record Address(
        String cityName,
        String cityCode,
        String countryName,
        String countryCode,
        String regionCode
    ) {}

    public record Distance(
        double value,
        String unit
    ) {}

    public record Analytics(
        Travelers travelers
    ) {}

    public record Travelers(
        int score
    ) {}
} 