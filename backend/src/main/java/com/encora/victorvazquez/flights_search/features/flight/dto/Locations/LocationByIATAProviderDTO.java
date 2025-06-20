package com.encora.victorvazquez.flights_search.features.flight.dto.Locations;

import java.util.List;

public record LocationByIATAProviderDTO(
    Meta meta,
    Data data
) {
    public record Meta(
        Links links
    ) {}

    public record Links(
        String self
    ) {}

    public record Data(
        String type,
        String subType,
        String name,
        String detailedName,
        String id,
        Self self,
        String timeZoneOffset,
        String iataCode,
        GeoCode geoCode,
        Address address,
        Analytics analytics
    ) {}

    public record Self(
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

    public record Analytics(
        Travelers travelers
    ) {}

    public record Travelers(
        int score
    ) {}
}
