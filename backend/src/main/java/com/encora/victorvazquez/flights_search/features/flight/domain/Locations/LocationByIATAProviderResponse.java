package com.encora.victorvazquez.flights_search.features.flight.domain.Locations;

public record LocationByIATAProviderResponse(
    Location data
) {
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
}
