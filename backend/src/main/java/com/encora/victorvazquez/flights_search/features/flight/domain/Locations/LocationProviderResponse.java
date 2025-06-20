package com.encora.victorvazquez.flights_search.features.flight.domain.Locations;

import java.util.List;

public record LocationProviderResponse(
    Meta meta,
    List<Location> data
) {
    public record Meta(
        Integer count,
        Links links
    ) {
        public record Links(
            String self
        ) {}
    }

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
}
