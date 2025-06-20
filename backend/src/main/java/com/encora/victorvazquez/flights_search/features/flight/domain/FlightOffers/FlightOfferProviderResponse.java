package com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers;

import lombok.Value;
import java.util.List;
import java.util.Map;

@Value
public class FlightOfferProviderResponse {
    List<FlightOffer> data;
    Meta meta;
    Dictionaries dictionaries;

    @Value
    public static class Meta {
        int count;
        Links links;
    }

    @Value
    public static class Links {
        String self;
        String next;
        String previous;
        String last;
        String first;
        String up;
    }

    @Value
    public static class Dictionaries {
        Map<String, LocationEntry> locations;
        Map<String, String> aircraft;
        Map<String, String> currencies;
        Map<String, String> carriers;
    }

    @Value
    public static class LocationEntry {
        String cityCode;
        String countryCode;
    }
}
