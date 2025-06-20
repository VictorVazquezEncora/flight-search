package com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers;

import com.encora.victorvazquez.flights_search.shared.Money;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Value
public class FlightOffer {
    String type;
    String id;
    String source;
    boolean instantTicketingRequired;
    boolean nonHomogeneous;
    boolean oneWay;
    boolean paymentCardRequired;
    String lastTicketingDate;
    String lastTicketingDateTime;
    int numberOfBookableSeats;
    List<Itinerary> itineraries;
    Price price;
    PricingOptions pricingOptions;
    List<String> validatingAirlineCodes;
    List<TravelerPricing> travelerPricings;

    @Value
    public static class Itinerary {
        String duration;
        List<Segment> segments;
    }

    @Value
    public static class Segment {
        Location departure;
        Location arrival;
        String carrierCode;
        String number;
        Aircraft aircraft;
        Operating operating;
        String duration;
        String id;
        int numberOfStops;
        boolean blacklistedInEU;
    }

    @Value
    public static class Location {
        String iataCode;
        String terminal;
        LocalDateTime at;
        String city;
        String country;
    }

    @Value
    public static class Aircraft {
        String code;
    }

    @Value
    public static class Operating {
        String carrierCode;
    }

    @Value
    public static class Price {
        String currency;
        Money total;
        Money base;
        List<Fee> fees;
        Money grandTotal;
        Money refundableTaxes;
    }

    @Value
    public static class Fee {
        Money amount;
        String type;
    }

    @Value
    public static class PricingOptions {
        List<String> fareType;
        boolean includedCheckedBagsOnly;
        boolean refundableFare;
        boolean noRestrictionFare;
        boolean noPenaltyFare;
    }

    @Value
    public static class TravelerPricing {
        String travelerId;
        String fareOption;
        String travelerType;
        String associatedAdultId;
        Price price;
        List<FareDetailsBySegment> fareDetailsBySegment;
    }

    @Value
    public static class FareDetailsBySegment {
        String segmentId;
        String cabin;
        String fareBasis;
        String brandedFare;
        String brandedFareLabel;
        String classType;
        boolean isAllotment;
        String sliceDiceIndicator;
        IncludedCheckedBags includedCheckedBags;
        IncludedCheckedBags includedCabinBags;
        List<Amenity> amenities;
    }

    @Value
    public static class IncludedCheckedBags {
        int quantity;
        String weightUnit;
        int weight;
    }

    @Value
    public static class Amenity {
        String description;
        boolean isChargeable;
        String amenityType;
        AmenityProvider amenityProvider;
    }

    @Value
    public static class AmenityProvider {
        String name;
    }
} 