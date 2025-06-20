package com.encora.victorvazquez.flights_search.features.flight.mapper;

import com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers.FlightOffer;
import com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers.FlightOfferProviderResponse;
import com.encora.victorvazquez.flights_search.features.flight.dto.FlightOffers.FlightOffersProviderDTO;
import com.encora.victorvazquez.flights_search.shared.Money;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FlightOfferProviderMapper {

    public FlightOfferProviderResponse toDomain(FlightOffersProviderDTO dto) {
        return new FlightOfferProviderResponse(
            dto.data().stream()
                .map(this::mapFlightOffer)
                .collect(Collectors.toList()),
            mapMeta(dto.meta()),
            mapDictionaries(dto.dictionaries())
        );
    }

    public FlightOffersProviderDTO toDTO(FlightOfferProviderResponse domain) {
        return new FlightOffersProviderDTO(
            new FlightOffersProviderDTO.Meta(
                domain.getMeta().getCount(),
                new FlightOffersProviderDTO.Links(domain.getMeta().getLinks().getSelf())
            ),
            domain.getData().stream()
                .map(this::mapFlightOfferToDTO)
                .collect(Collectors.toList()),
            mapDictionariesToDTO(domain.getDictionaries())
        );
    }

    private FlightOffersProviderDTO.FlightOffer mapFlightOfferToDTO(FlightOffer domain) {
        return new FlightOffersProviderDTO.FlightOffer(
            domain.getId(),
            domain.getSource(),
            domain.isInstantTicketingRequired(),
            domain.isNonHomogeneous(),
            domain.isOneWay(),
            domain.getLastTicketingDate(),
            domain.getNumberOfBookableSeats(),
            mapItinerariesToDTO(domain.getItineraries()),
            mapPriceToDTO(domain.getPrice()),
            mapPricingOptionsToDTO(domain.getPricingOptions()),
            domain.getValidatingAirlineCodes(),
            mapTravelerPricingsToDTO(domain.getTravelerPricings())
        );
    }

    private List<FlightOffersProviderDTO.FlightOffer.Itinerary> mapItinerariesToDTO(List<FlightOffer.Itinerary> itineraries) {
        return itineraries.stream()
            .map(domain -> new FlightOffersProviderDTO.FlightOffer.Itinerary(
                domain.getDuration(),
                mapSegmentsToDTO(domain.getSegments())
            ))
            .collect(Collectors.toList());
    }

    private List<FlightOffersProviderDTO.FlightOffer.Segment> mapSegmentsToDTO(List<FlightOffer.Segment> segments) {
        return segments.stream()
            .map(domain -> new FlightOffersProviderDTO.FlightOffer.Segment(
                mapLocationToDTO(domain.getDeparture()),
                mapLocationToDTO(domain.getArrival()),
                domain.getCarrierCode(),
                domain.getNumber(),
                new FlightOffersProviderDTO.FlightOffer.Aircraft(domain.getAircraft().getCode()),
                new FlightOffersProviderDTO.FlightOffer.Operating(domain.getOperating().getCarrierCode()),
                domain.getDuration(),
                domain.getId(),
                domain.getNumberOfStops(),
                domain.isBlacklistedInEU()
            ))
            .collect(Collectors.toList());
    }

    private FlightOffersProviderDTO.FlightOffer.Location mapLocationToDTO(FlightOffer.Location domain) {
        return new FlightOffersProviderDTO.FlightOffer.Location(
            domain.getIataCode(),
            domain.getTerminal(),
            domain.getAt()
        );
    }

    private FlightOffersProviderDTO.FlightOffer.Price mapPriceToDTO(FlightOffer.Price domain) {
        return new FlightOffersProviderDTO.FlightOffer.Price(
            domain.getCurrency(),
            domain.getTotal().getAmount(),
            domain.getBase().getAmount(),
            mapFeesToDTO(domain.getFees()),
            domain.getGrandTotal().getAmount()
        );
    }

    private List<FlightOffersProviderDTO.FlightOffer.Fee> mapFeesToDTO(List<FlightOffer.Fee> fees) {
        return fees.stream()
            .map(domain -> new FlightOffersProviderDTO.FlightOffer.Fee(
                domain.getAmount().getAmount(),
                domain.getType()
            ))
            .collect(Collectors.toList());
    }

    private FlightOffersProviderDTO.FlightOffer.PricingOptions mapPricingOptionsToDTO(FlightOffer.PricingOptions domain) {
        return new FlightOffersProviderDTO.FlightOffer.PricingOptions(
            domain.getFareType(),
            domain.isIncludedCheckedBagsOnly()
        );
    }

    private List<FlightOffersProviderDTO.FlightOffer.TravelerPricing> mapTravelerPricingsToDTO(List<FlightOffer.TravelerPricing> pricings) {
        return pricings.stream()
            .map(domain -> new FlightOffersProviderDTO.FlightOffer.TravelerPricing(
                domain.getTravelerId(),
                domain.getFareOption(),
                domain.getTravelerType(),
                mapPriceToDTO(domain.getPrice()),
                mapFareDetailsBySegmentToDTO(domain.getFareDetailsBySegment())
            ))
            .collect(Collectors.toList());
    }

    private List<FlightOffersProviderDTO.FlightOffer.FareDetailsBySegment> mapFareDetailsBySegmentToDTO(List<FlightOffer.FareDetailsBySegment> details) {
        return details.stream()
            .map(domain -> new FlightOffersProviderDTO.FlightOffer.FareDetailsBySegment(
                domain.getSegmentId(),
                domain.getCabin(),
                domain.getFareBasis(),
                domain.getClassType(),
                domain.getBrandedFare(),
                domain.getBrandedFareLabel(),
                mapIncludedCheckedBagsToDTO(domain.getIncludedCheckedBags()),
                mapIncludedCheckedBagsToDTO(domain.getIncludedCabinBags()),
                mapAmenitiesToDTO(domain.getAmenities())
            ))
            .collect(Collectors.toList());
    }

    private FlightOffersProviderDTO.FlightOffer.IncludedCheckedBags mapIncludedCheckedBagsToDTO(FlightOffer.IncludedCheckedBags domain) {
        if (domain == null) {
            return new FlightOffersProviderDTO.FlightOffer.IncludedCheckedBags(0, "", 0);
        }
        return new FlightOffersProviderDTO.FlightOffer.IncludedCheckedBags(
            domain.getQuantity(),
            domain.getWeightUnit(),
            domain.getWeight()
        );
    }

    private List<FlightOffersProviderDTO.FlightOffer.Amenity> mapAmenitiesToDTO(List<FlightOffer.Amenity> amenities) {
        if (amenities == null) {
            return new ArrayList<>();
        }
        return amenities.stream()
            .map(domain -> new FlightOffersProviderDTO.FlightOffer.Amenity(
                domain.getDescription(),
                domain.isChargeable(),
                domain.getAmenityType(),
                new FlightOffersProviderDTO.FlightOffer.AmenityProvider(domain.getAmenityProvider().getName())
            ))
            .collect(Collectors.toList());
    }

    private FlightOfferProviderResponse.Meta mapMeta(FlightOffersProviderDTO.Meta meta) {
        return new FlightOfferProviderResponse.Meta(
            meta.count(),
            new FlightOfferProviderResponse.Links(
                meta.links().self(),
                null,
                null,
                null,
                null,
                null
            )
        );
    }

    private FlightOffer mapFlightOffer(FlightOffersProviderDTO.FlightOffer dto) {
        return new FlightOffer(
            "flight-offer",
            dto.id(),
            dto.source(),
            dto.instantTicketingRequired(),
            dto.nonHomogeneous(),
            dto.oneWay(),
            false,
            dto.lastTicketingDate(),
            null,
            dto.numberOfBookableSeats(),
            mapItineraries(dto.itineraries()),
            mapPrice(dto.price()),
            mapPricingOptions(dto.pricingOptions()),
            dto.validatingAirlineCodes(),
            mapTravelerPricings(dto.travelerPricings())
        );
    }

    private List<FlightOffer.Itinerary> mapItineraries(List<FlightOffersProviderDTO.FlightOffer.Itinerary> itineraries) {
        return itineraries.stream()
            .map(dto -> new FlightOffer.Itinerary(
                dto.duration(),
                mapSegments(dto.segments())
            ))
            .collect(Collectors.toList());
    }

    private List<FlightOffer.Segment> mapSegments(List<FlightOffersProviderDTO.FlightOffer.Segment> segments) {
        return segments.stream()
            .map(dto -> new FlightOffer.Segment(
                mapLocation(dto.departure()),
                mapLocation(dto.arrival()),
                dto.carrierCode(),
                dto.number(),
                new FlightOffer.Aircraft(dto.aircraft().code()),
                new FlightOffer.Operating(dto.operating().carrierCode()),
                dto.duration(),
                dto.id(),
                dto.numberOfStops(),
                dto.blacklistedInEU()
            ))
            .collect(Collectors.toList());
    }

    private FlightOffer.Location mapLocation(FlightOffersProviderDTO.FlightOffer.Location dto) {
        return new FlightOffer.Location(
            dto.iataCode(),
            dto.terminal(),
            dto.at(),
            null,
            null
        );
    }

    private FlightOffer.Price mapPrice(FlightOffersProviderDTO.FlightOffer.Price dto) {
        return new FlightOffer.Price(
            dto.currency(),
            Money.of(dto.total(), dto.currency()),
            Money.of(dto.base(), dto.currency()),
            mapFees(dto.fees(), dto.currency()),
            Money.of(dto.grandTotal(), dto.currency()),
            null
        );
    }

    private List<FlightOffer.Fee> mapFees(List<FlightOffersProviderDTO.FlightOffer.Fee> fees, String currency) {
        return fees.stream()
            .map(dto -> new FlightOffer.Fee(
                Money.of(dto.amount(), currency),
                dto.type()
            ))
            .collect(Collectors.toList());
    }

    private FlightOffer.PricingOptions mapPricingOptions(FlightOffersProviderDTO.FlightOffer.PricingOptions dto) {
        return new FlightOffer.PricingOptions(
            dto.fareType(),
            dto.includedCheckedBagsOnly(),
            false,
            false,
            false
        );
    }

    private List<FlightOffer.TravelerPricing> mapTravelerPricings(List<FlightOffersProviderDTO.FlightOffer.TravelerPricing> pricings) {
        return pricings.stream()
            .map(dto -> new FlightOffer.TravelerPricing(
                dto.travelerId(),
                dto.fareOption(),
                dto.travelerType(),
                null,
                mapPrice(dto.price()),
                mapFareDetailsBySegment(dto.fareDetailsBySegment())
            ))
            .collect(Collectors.toList());
    }

    private List<FlightOffer.FareDetailsBySegment> mapFareDetailsBySegment(List<FlightOffersProviderDTO.FlightOffer.FareDetailsBySegment> details) {
        return details.stream()
            .map(dto -> new FlightOffer.FareDetailsBySegment(
                dto.segmentId(),
                dto.cabin(),
                dto.fareBasis(),
                dto.brandedFare(),
                dto.brandedFareLabel(),
                dto.classType(),
                false,
                null,
                mapIncludedCheckedBags(dto.includedCheckedBags()),
                mapIncludedCheckedBags(dto.includedCabinBags()),
                mapAmenities(dto.amenities())
            ))
            .collect(Collectors.toList());
    }

    private FlightOffer.IncludedCheckedBags mapIncludedCheckedBags(FlightOffersProviderDTO.FlightOffer.IncludedCheckedBags dto) {
        if (dto == null) {
            return new FlightOffer.IncludedCheckedBags(0, "", 0);
        }
        return new FlightOffer.IncludedCheckedBags(
            dto.quantity(),
            dto.weightUnit(),
            dto.weight()
        );
    }

    private List<FlightOffer.Amenity> mapAmenities(List<FlightOffersProviderDTO.FlightOffer.Amenity> amenities) {
        if (amenities == null) {
            return new ArrayList<>();
        }
        return amenities.stream()
            .map(dto -> new FlightOffer.Amenity(
                dto.description(),
                dto.isChargeable(),
                dto.amenityType(),
                new FlightOffer.AmenityProvider(dto.amenityProvider().name())
            ))
            .collect(Collectors.toList());
    }

    private FlightOfferProviderResponse.Dictionaries mapDictionaries(FlightOffersProviderDTO.Dictionaries dto) {
        if (dto == null) {
            return null;
        }

        return new FlightOfferProviderResponse.Dictionaries(
            mapLocations(dto.locations()),
            dto.aircraft(),
            dto.currencies(),
            dto.carriers()
        );
    }

    private Map<String, FlightOfferProviderResponse.LocationEntry> mapLocations(
            Map<String, FlightOffersProviderDTO.DictionaryLocation> locations) {
        if (locations == null) {
            return Map.of();
        }

        return locations.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new FlightOfferProviderResponse.LocationEntry(
                    entry.getValue().cityCode(),
                    entry.getValue().countryCode()
                )
            ));
    }

    private FlightOffersProviderDTO.Dictionaries mapDictionariesToDTO(FlightOfferProviderResponse.Dictionaries domain) {
        if (domain == null) {
            return new FlightOffersProviderDTO.Dictionaries(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of()
            );
        }

        Map<String, FlightOffersProviderDTO.DictionaryLocation> locations = domain.getLocations().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new FlightOffersProviderDTO.DictionaryLocation(
                    entry.getValue().getCityCode(),
                    entry.getValue().getCountryCode(),
                    null,
                    null,
                    null,
                    0.0,
                    0.0
                )
            ));

        return new FlightOffersProviderDTO.Dictionaries(
            locations,
            domain.getAircraft(),
            domain.getCurrencies(),
            domain.getCarriers()
        );
    }
} 