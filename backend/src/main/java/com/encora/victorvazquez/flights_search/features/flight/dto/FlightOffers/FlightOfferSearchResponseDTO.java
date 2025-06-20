package com.encora.victorvazquez.flights_search.features.flight.dto.FlightOffers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FlightOfferSearchResponseDTO(
    Meta meta,
    List<FlightOffer> data
) 
{
    public record Meta(
        Integer count,
        Links links
    ) {}

    public record Links(
        String self
    ) {}

    public record FlightOffer(
    String id,
    String source,
    Boolean instantTicketingRequired,
    Boolean nonHomogeneous,
    Boolean oneWay,
    String lastTicketingDate,
    Integer numberOfBookableSeats,
    List<Itinerary> itineraries,
    Price price,
    PricingOptions pricingOptions,
    List<String> validatingAirlineCodes,
    List<TravelerPricing> travelerPricings
) {
    public record Itinerary(
        String duration,
        List<Segment> segments
    ) {}

    public record Segment(
        Location departure,
        Location arrival,
        String carrierCode,
        String number,
        Aircraft aircraft,
        Operating operating,
        String duration,
        String id,
        Integer numberOfStops,
        Boolean blacklistedInEU
    ) {}

    public record Location(
        String iataCode,
        String terminal,
        LocalDateTime at
    ) {}

    public record Aircraft(
        String code
    ) {}

    public record Operating(
        String carrierCode
    ) {}

    public record Price(
        String currency,
        BigDecimal total,
        BigDecimal base,
        List<Fee> fees,
        BigDecimal grandTotal
    ) {}

    public record Fee(
        BigDecimal amount,
        String type
    ) {}

    public record PricingOptions(
        List<String> fareType,
        Boolean includedCheckedBagsOnly
    ) {}

    public record TravelerPricing(
        String travelerId,
        String fareOption,
        String travelerType,
        Price price,
        List<FareDetailsBySegment> fareDetailsBySegment
    ) {}

    public record FareDetailsBySegment(
        String segmentId,
        String cabin,
        String fareBasis,
        String classType,
        IncludedCheckedBags includedCheckedBags
    ) {}

    public record IncludedCheckedBags(
        Integer weight,
            String weightUnit
        ) {}
}
} 