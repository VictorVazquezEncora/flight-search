package com.encora.victorvazquez.flights_search.features.flight.domain.Locations;

import java.util.List;

public record Location(
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
    Double relevance,
    String category,
    List<String> tags,
    String rank
) {
    public record Links(
        String href,
        List<String> methods,
        Integer count
    ) {}

    public record GeoCode(
        Double latitude,
        Double longitude
    ) {}

    public record Address(
        String cityName,
        String cityCode,
        String countryName,
        String countryCode,
        String stateCode,
        String regionCode
    ) {}

    public record Distance(
        Integer value,
        String unit
    ) {}

    public record Analytics(
        Travelers travelers
    ) {
        public record Travelers(
            Integer score
        ) {}
    }
}
