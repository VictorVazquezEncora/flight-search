package com.encora.victorvazquez.flights_search.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers.FlightOfferSearch;
import com.encora.victorvazquez.flights_search.features.flight.domain.Locations.LocationSearch;
import com.encora.victorvazquez.flights_search.features.flight.port.out.FlightOfferPort;
import com.encora.victorvazquez.flights_search.features.flight.dto.FlightOffers.FlightOffersProviderDTO;
import com.encora.victorvazquez.flights_search.features.flight.dto.Locations.LocationSearchResponseDTO;
import com.encora.victorvazquez.flights_search.features.flight.domain.FlightOffers.FlightOfferProviderResponse;
import com.encora.victorvazquez.flights_search.features.flight.mapper.FlightOfferProviderMapper;
import com.encora.victorvazquez.flights_search.infrastructure.exception.BusinessException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

@Component
public class AmadeusClient implements FlightOfferPort {
    private static final Logger logger = LoggerFactory.getLogger(AmadeusClient.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final FlightOfferProviderMapper flightOfferProviderMapper;

    public AmadeusClient(@Qualifier("amadeusWebClient") WebClient webClient, FlightOfferProviderMapper flightOfferProviderMapper) {
        if (webClient == null) {
            String error = "WebClient cannot be null";
            logger.error(error);
            throw new IllegalArgumentException(error);
        }
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
        this.flightOfferProviderMapper = flightOfferProviderMapper;
        objectMapper.findAndRegisterModules();
    }

    @Override
    public Mono<FlightOfferProviderResponse> searchFlights(FlightOfferSearch request) {
        logger.info("Searching flights with request: {}", request);
        
        StringBuilder uriBuilder = new StringBuilder("/v2/shopping/flight-offers");
        List<String> params = new ArrayList<>();
        
        params.add(String.format("originLocationCode=%s", request.originLocationCode()));
        params.add(String.format("destinationLocationCode=%s", request.destinationLocationCode()));
        params.add(String.format("departureDate=%s", request.departureDate()));
        params.add(String.format("adults=%d", request.adults()));
        
        if (request.returnDate() != null) {
            params.add(String.format("returnDate=%s", request.returnDate()));
        }
        if (request.children() > 0) {
            params.add(String.format("children=%d", request.children()));
        }
        if (request.infants() > 0) {
            params.add(String.format("infants=%d", request.infants()));
        }
        if (request.travelClass() != null && !request.travelClass().isEmpty()) {
            params.add(String.format("travelClass=%s", request.travelClass()));
        }
        if (request.nonStop() != null) {
            params.add(String.format("nonStop=%b", request.nonStop()));
        }
        if (request.currencyCode() != null && !request.currencyCode().isEmpty()) {
            params.add(String.format("currencyCode=%s", request.currencyCode()));
        }
        if (request.maxPrice() != null) {
            params.add(String.format("maxPrice=%d", request.maxPrice()));
        }
        if (request.max() > 0) {
            params.add(String.format("max=%d", request.max()));
        }
        
        if (!params.isEmpty()) {
            uriBuilder.append("?").append(String.join("&", params));
        }
        
        String uri = uriBuilder.toString();
        logger.debug("Calling Amadeus API with URI: {}", uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseStr -> {
                    try {
                        logger.debug("Received response from Amadeus: {}", responseStr);
                        JsonNode root = objectMapper.readTree(responseStr);
                        List<FlightOffersProviderDTO.FlightOffer> offers = new ArrayList<>();
                        
                        for (JsonNode offer : root.get("data")) {
                            offers.add(parseFlightOffer(offer));
                        }

                        FlightOffersProviderDTO dto = new FlightOffersProviderDTO(
                            new FlightOffersProviderDTO.Meta(
                                root.get("meta").get("count").asInt(),
                                new FlightOffersProviderDTO.Links(root.get("meta").get("links").get("self").asText())
                            ),
                            offers,
                            parseDictionaries(root.get("dictionaries"))
                        );
                        return flightOfferProviderMapper.toDomain(dto);
                    } catch (Exception e) {
                        logger.error("Error parsing Amadeus response: {}", e.getMessage());
                        throw new BusinessException("PARSE_ERROR", "Error processing Amadeus response: " + e.getMessage());
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    String errorDetail = "";
                    try {
                        JsonNode errorNode = objectMapper.readTree(e.getResponseBodyAsString());
                        if (errorNode.has("errors") && errorNode.get("errors").isArray()) {
                            errorDetail = errorNode.get("errors").get(0).get("detail").asText();
                        }
                    } catch (Exception ex) {
                        errorDetail = e.getResponseBodyAsString();
                    }
                    
                    logger.error("Error during Amadeus API call. Status: {}, Detail: {}", 
                        e.getStatusCode(), errorDetail);
                    
                    String errorMessage = String.format("Error in Amadeus API: %s - %s", 
                        e.getStatusCode(), errorDetail);
                    
                    return Mono.error(new BusinessException("API_ERROR", errorMessage));
                })
                .onErrorResume(io.netty.handler.timeout.ReadTimeoutException.class, e -> {
                    logger.error("Timeout during Amadeus API call");
                    return Mono.error(new BusinessException("TIMEOUT_ERROR", 
                        "The request timed out. Please try again."));
                })
                .onErrorResume(Exception.class, e -> {
                    logger.error("Unexpected error processing request: {}", e.getMessage(), e);
                    String errorMessage = "Unexpected error processing request";
                    if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                        errorMessage += ": " + e.getMessage();
                    }
                    return Mono.error(new BusinessException("UNEXPECTED_ERROR", errorMessage));
                });
    }

    @Override
    public Flux<LocationSearchResponseDTO> searchLocations(LocationSearch locationSearch) {
        StringBuilder uriBuilder = new StringBuilder("/v1/reference-data/locations");
        List<String> params = new ArrayList<>();
        
        params.add(String.format("subType=%s", locationSearch.subType()));
        params.add(String.format("keyword=%s", locationSearch.keyword()));
        
        if (locationSearch.countryCode() != null && !locationSearch.countryCode().trim().isEmpty()) {
            params.add(String.format("countryCode=%s", locationSearch.countryCode()));
        }
        
        if (locationSearch.pageLimit() > 0) {
            params.add(String.format("page[limit]=%d", locationSearch.pageLimit()));
        }
        
        if (locationSearch.pageOffset() != null && locationSearch.pageOffset() > 0) {
            params.add(String.format("page[offset]=%d", locationSearch.pageOffset()));
        }
        
        if (locationSearch.sort() != null && !locationSearch.sort().trim().isEmpty()) {
            params.add(String.format("sort=%s", locationSearch.sort()));
        }
        
        if (locationSearch.view() != null && !locationSearch.view().trim().isEmpty()) {
            params.add(String.format("view=%s", locationSearch.view()));
        }
        
        if (!params.isEmpty()) {
            uriBuilder.append("?").append(String.join("&", params));
        }
        
        String uri = uriBuilder.toString();
        logger.debug("Calling Amadeus API for locations with URI: {}", uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(LocationSearchResponseDTO.class)
                .flux()
                .onErrorMap(WebClientResponseException.class, e -> {
                    logger.error("Error calling Amadeus API for locations: {}", e.getMessage());
                    return new BusinessException("API_ERROR", "Error calling Amadeus API for locations: " + e.getMessage());
                })
                .onErrorMap(Exception.class, e -> {
                    logger.error("Unexpected error searching locations: {}", e.getMessage());
                    return new BusinessException("UNEXPECTED_ERROR", "Unexpected error searching locations: " + e.getMessage());
                });
    }

    public Flux<LocationSearchResponseDTO> searchLocationsByIataCode(String iataCode) {
        return webClient.get()
                .uri("/v1/reference-data/locations/airports/{iataCode}", iataCode)
                .retrieve()
                .bodyToMono(LocationSearchResponseDTO.class)
                .flux()
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode().value() == 404) {
                        logger.debug("Not found as airport, searching as city: {}", iataCode);
                        return webClient.get()
                                .uri("/v1/reference-data/locations/cities/{iataCode}", iataCode)
                                .retrieve()
                                .bodyToMono(LocationSearchResponseDTO.class)
                                .flux()
                                .onErrorMap(WebClientResponseException.class, ex -> {
                                    logger.error("Error searching location by IATA code: {} - Status: {} - Response: {}", 
                                        iataCode, ex.getStatusCode(), ex.getResponseBodyAsString());
                                    return new BusinessException("LOCATION_NOT_FOUND", 
                                        String.format("Location with code %s not found", iataCode));
                                });
                    }

                    String errorDetail = "";
                    try {
                        JsonNode errorNode = objectMapper.readTree(e.getResponseBodyAsString());
                        if (errorNode.has("errors") && errorNode.get("errors").isArray()) {
                            errorDetail = errorNode.get("errors").get(0).get("detail").asText();
                        }
                    } catch (Exception ex) {
                        errorDetail = e.getResponseBodyAsString();
                    }

                    logger.error("Error in Amadeus API: Status {} - Detail: {}", 
                        e.getStatusCode(), errorDetail);

                    String errorMessage = String.format("Error in Amadeus API: %s - %s", 
                        e.getStatusCode(), errorDetail);

                    if (e.getStatusCode().value() == 429) {
                        return Flux.error(new BusinessException("RATE_LIMIT_EXCEEDED", 
                            "Rate limit exceeded. Please try again later."));
                    }

                    return Flux.error(new BusinessException("API_ERROR", errorMessage));
                })
                .onErrorResume(io.netty.handler.timeout.ReadTimeoutException.class, e -> {
                    logger.error("Timeout searching location: {}", iataCode);
                    return Flux.error(new BusinessException("TIMEOUT_ERROR", 
                        "The request timed out. Please try again."));
                })
                .onErrorResume(Exception.class, e -> {
                    if (!(e instanceof BusinessException)) {
                        logger.error("Unexpected error searching location {}: {}", iataCode, e.getMessage());
                        return Flux.error(new BusinessException("UNEXPECTED_ERROR", 
                            "Unexpected error searching location. Please try again."));
                    }
                    return Flux.error(e);
                });
    }

    private FlightOffersProviderDTO.FlightOffer parseFlightOffer(JsonNode offer) {
        try {
            validateRequiredField(offer, "id", "Flight offer must have an ID");
            validateRequiredField(offer, "source", "Flight offer must have a source");
            validateRequiredField(offer, "price", "Flight offer must have a price");
            validateRequiredField(offer, "itineraries", "Flight offer must have itineraries");

            return new FlightOffersProviderDTO.FlightOffer(
                offer.get("id").asText(),
                offer.get("source").asText(),
                offer.has("instantTicketingRequired") ? offer.get("instantTicketingRequired").asBoolean() : false,
                offer.has("nonHomogeneous") ? offer.get("nonHomogeneous").asBoolean() : false,
                offer.has("oneWay") ? offer.get("oneWay").asBoolean() : false,
                offer.has("lastTicketingDate") ? offer.get("lastTicketingDate").asText() : null,
                offer.has("numberOfBookableSeats") ? offer.get("numberOfBookableSeats").asInt() : 0,
                parseItineraries(offer.get("itineraries")),
                parsePrice(offer.get("price")),
                offer.has("pricingOptions") ? parsePricingOptions(offer.get("pricingOptions")) : 
                    new FlightOffersProviderDTO.FlightOffer.PricingOptions(Collections.emptyList(), false),
                offer.has("validatingAirlineCodes") ? parseStringList(offer.get("validatingAirlineCodes")) : Collections.emptyList(),
                offer.has("travelerPricings") ? parseTravelerPricings(offer.get("travelerPricings")) : Collections.emptyList()
            );
        } catch (Exception e) {
            logger.error("Error parsing flight offer: {}", e.getMessage());
            throw new BusinessException("PARSE_ERROR", "Error processing flight offer: " + e.getMessage());
        }
    }

    private void validateRequiredField(JsonNode node, String fieldName, String errorMessage) {
        if (!node.has(fieldName) || node.get(fieldName).isNull()) {
            logger.error("Required field missing: {}", fieldName);
            throw new BusinessException("INVALID_RESPONSE", errorMessage);
        }
    }

    private FlightOffersProviderDTO.FlightOffer.Price parsePrice(JsonNode price) {
        validateRequiredField(price, "currency", "Price must have a currency");
        validateRequiredField(price, "total", "Price must have a total amount");
        validateRequiredField(price, "base", "Price must have a base amount");

        BigDecimal total = new BigDecimal(price.get("total").asText());
        BigDecimal grandTotal = price.has("grandTotal") && !price.get("grandTotal").isNull() 
            ? new BigDecimal(price.get("grandTotal").asText())
            : total;

        return new FlightOffersProviderDTO.FlightOffer.Price(
            price.get("currency").asText(),
            total,
            new BigDecimal(price.get("base").asText()),
            price.has("fees") ? parseFees(price.get("fees")) : Collections.emptyList(),
            grandTotal
        );
    }

    private List<FlightOffersProviderDTO.FlightOffer.Itinerary> parseItineraries(JsonNode itineraries) {
        if (!itineraries.isArray()) {
            logger.error("Itineraries must be an array");
            throw new BusinessException("INVALID_RESPONSE", "Itineraries must be an array");
        }

        List<FlightOffersProviderDTO.FlightOffer.Itinerary> result = new ArrayList<>();
        for (JsonNode itinerary : itineraries) {
            validateRequiredField(itinerary, "duration", "Itinerary must have a duration");
            validateRequiredField(itinerary, "segments", "Itinerary must have segments");

            result.add(new FlightOffersProviderDTO.FlightOffer.Itinerary(
                itinerary.get("duration").asText(),
                parseSegments(itinerary.get("segments"))
            ));
        }
        return result;
    }

    private List<FlightOffersProviderDTO.FlightOffer.Segment> parseSegments(JsonNode segments) {
        List<FlightOffersProviderDTO.FlightOffer.Segment> result = new ArrayList<>();
        for (JsonNode segment : segments) {
            result.add(parseSegment(segment));
        }
        return result;
    }

    private FlightOffersProviderDTO.FlightOffer.Segment parseSegment(JsonNode segment) {
        validateRequiredField(segment, "departure", "Segment must have departure information");
        validateRequiredField(segment, "arrival", "Segment must have arrival information");
        validateRequiredField(segment, "carrierCode", "Segment must have a carrier code");
        validateRequiredField(segment, "number", "Segment must have a flight number");

        return new FlightOffersProviderDTO.FlightOffer.Segment(
            parseFlightLocation(segment.get("departure")),
            parseFlightLocation(segment.get("arrival")),
            segment.get("carrierCode").asText(),
            segment.get("number").asText(),
            segment.has("aircraft") ? new FlightOffersProviderDTO.FlightOffer.Aircraft(
                getTextOrDefault(segment.get("aircraft"), "code", "")) : null,
            segment.has("operating") ? new FlightOffersProviderDTO.FlightOffer.Operating(
                getTextOrDefault(segment.get("operating"), "carrierCode", "")) : null,
            getTextOrDefault(segment, "duration", ""),
            getTextOrDefault(segment, "id", ""),
            segment.has("numberOfStops") ? segment.get("numberOfStops").asInt() : 0,
            segment.has("blacklistedInEU") ? segment.get("blacklistedInEU").asBoolean() : false
        );
    }

    private FlightOffersProviderDTO.FlightOffer.Location parseFlightLocation(JsonNode location) {
        validateRequiredField(location, "iataCode", "Location must have an IATA code");
        validateRequiredField(location, "at", "Location must have a timestamp");

        return new FlightOffersProviderDTO.FlightOffer.Location(
            location.get("iataCode").asText(),
            location.has("terminal") ? location.get("terminal").asText() : null,
            parseDateTime(location.get("at"))
        );
    }

    private LocalDateTime parseDateTime(JsonNode node) {
        try {
            String dateTimeStr = node.asText();
            if (dateTimeStr == null || dateTimeStr.isEmpty()) {
                logger.error("Invalid datetime format: empty or null");
                throw new BusinessException("INVALID_DATE", "Invalid datetime format");
            }
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            logger.error("Error parsing datetime: {}", e.getMessage());
            throw new BusinessException("INVALID_DATE", "Invalid datetime format: " + e.getMessage());
        }
    }

    private List<FlightOffersProviderDTO.FlightOffer.Fee> parseFees(JsonNode fees) {
        List<FlightOffersProviderDTO.FlightOffer.Fee> result = new ArrayList<>();
        for (JsonNode fee : fees) {
            result.add(new FlightOffersProviderDTO.FlightOffer.Fee(
                new BigDecimal(fee.get("amount").asText()),
                fee.get("type").asText()
            ));
        }
        return result;
    }

    private FlightOffersProviderDTO.FlightOffer.PricingOptions parsePricingOptions(JsonNode options) {
        return new FlightOffersProviderDTO.FlightOffer.PricingOptions(
            parseStringList(options.get("fareType")),
            options.get("includedCheckedBagsOnly").asBoolean()
        );
    }

    private List<FlightOffersProviderDTO.FlightOffer.TravelerPricing> parseTravelerPricings(JsonNode pricings) {
        if (pricings == null || !pricings.isArray()) {
            return Collections.emptyList();
        }

        List<FlightOffersProviderDTO.FlightOffer.TravelerPricing> result = new ArrayList<>();
        for (JsonNode pricing : pricings) {
            try {
                validateRequiredField(pricing, "travelerId", "Traveler pricing must have a traveler ID");
                validateRequiredField(pricing, "fareOption", "Traveler pricing must have a fare option");
                validateRequiredField(pricing, "travelerType", "Traveler pricing must have a traveler type");
                validateRequiredField(pricing, "price", "Traveler pricing must have a price");
                validateRequiredField(pricing, "fareDetailsBySegment", "Traveler pricing must have fare details by segment");

                result.add(new FlightOffersProviderDTO.FlightOffer.TravelerPricing(
                    pricing.get("travelerId").asText(),
                    pricing.get("fareOption").asText(),
                    pricing.get("travelerType").asText(),
                    parsePrice(pricing.get("price")),
                    parseFareDetailsBySegment(pricing.get("fareDetailsBySegment"))
                ));
            } catch (Exception e) {
                logger.error("Error parsing traveler pricing: {}", e.getMessage());
                continue;
            }
        }
        return result;
    }

    private List<FlightOffersProviderDTO.FlightOffer.FareDetailsBySegment> parseFareDetailsBySegment(JsonNode details) {
        if (details == null || !details.isArray()) {
            return Collections.emptyList();
        }

        List<FlightOffersProviderDTO.FlightOffer.FareDetailsBySegment> result = new ArrayList<>();
        for (JsonNode detail : details) {
            try {
                result.add(new FlightOffersProviderDTO.FlightOffer.FareDetailsBySegment(
                    getTextOrDefault(detail, "segmentId", ""),
                    getTextOrDefault(detail, "cabin", ""),
                    getTextOrDefault(detail, "fareBasis", ""),
                    getTextOrDefault(detail, "class", ""),
                    getTextOrDefault(detail, "brandedFare", ""),
                    getTextOrDefault(detail, "brandedFareLabel", ""),
                    parseIncludedBags(detail.get("includedCheckedBags")),
                    parseIncludedBags(detail.get("includedCabinBags")),
                    parseAmenities(detail.get("amenities"))
                ));
            } catch (Exception e) {
                logger.error("Error parsing fare details by segment: {}", e.getMessage());
                continue;
            }
        }
        return result;
    }

    private FlightOffersProviderDTO.FlightOffer.IncludedCheckedBags parseIncludedBags(JsonNode bagsNode) {
        if (bagsNode == null || bagsNode.isNull()) {
            return new FlightOffersProviderDTO.FlightOffer.IncludedCheckedBags(0, "", 0);
        }

        return new FlightOffersProviderDTO.FlightOffer.IncludedCheckedBags(
            bagsNode.has("quantity") ? bagsNode.get("quantity").asInt() : 0,
            bagsNode.has("weightUnit") ? bagsNode.get("weightUnit").asText() : "",
            bagsNode.has("weight") ? bagsNode.get("weight").asInt() : 0
        );
    }

    private List<FlightOffersProviderDTO.FlightOffer.Amenity> parseAmenities(JsonNode amenitiesNode) {
        if (amenitiesNode == null || !amenitiesNode.isArray()) {
            return Collections.emptyList();
        }

        List<FlightOffersProviderDTO.FlightOffer.Amenity> amenities = new ArrayList<>();
        for (JsonNode amenity : amenitiesNode) {
            try {
                amenities.add(new FlightOffersProviderDTO.FlightOffer.Amenity(
                    getTextOrDefault(amenity, "description", ""),
                    amenity.has("isChargeable") ? amenity.get("isChargeable").asBoolean() : false,
                    getTextOrDefault(amenity, "amenityType", ""),
                    parseAmenityProvider(amenity.get("amenityProvider"))
                ));
            } catch (Exception e) {
                logger.error("Error parsing amenity: {}", e.getMessage());
                continue;
            }
        }
        return amenities;
    }

    private FlightOffersProviderDTO.FlightOffer.AmenityProvider parseAmenityProvider(JsonNode providerNode) {
        if (providerNode == null || providerNode.isNull()) {
            return new FlightOffersProviderDTO.FlightOffer.AmenityProvider("");
        }
        return new FlightOffersProviderDTO.FlightOffer.AmenityProvider(
            getTextOrDefault(providerNode, "name", "")
        );
    }

    private String getTextOrDefault(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asText() : defaultValue;
    }

    private int getIntOrDefault(JsonNode node, String fieldName, int defaultValue) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asInt() : defaultValue;
    }

    private List<String> parseStringList(JsonNode node) {
        if (node == null || node.isNull() || !node.isArray()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        node.forEach(item -> result.add(item.asText()));
        return result;
    }

    private FlightOffersProviderDTO.Dictionaries parseDictionaries(JsonNode dictionaries) {
        if (dictionaries == null || dictionaries.isNull()) {
            return new FlightOffersProviderDTO.Dictionaries(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of()
            );
        }

        Map<String, FlightOffersProviderDTO.DictionaryLocation> locations = new HashMap<>();
        if (dictionaries.has("locations")) {
            JsonNode locationsNode = dictionaries.get("locations");
            locationsNode.fields().forEachRemaining(entry -> {
                JsonNode locationData = entry.getValue();
                locations.put(entry.getKey(), new FlightOffersProviderDTO.DictionaryLocation(
                    locationData.get("cityCode").asText(),
                    locationData.get("countryCode").asText(),
                    null,
                    null,
                    null,
                    0.0,
                    0.0
                ));
            });
        }

        Map<String, String> aircraft = new HashMap<>();
        if (dictionaries.has("aircraft")) {
            JsonNode aircraftNode = dictionaries.get("aircraft");
            aircraftNode.fields().forEachRemaining(entry -> 
                aircraft.put(entry.getKey(), entry.getValue().asText()));
        }

        Map<String, String> currencies = new HashMap<>();
        if (dictionaries.has("currencies")) {
            JsonNode currenciesNode = dictionaries.get("currencies");
            currenciesNode.fields().forEachRemaining(entry -> 
                currencies.put(entry.getKey(), entry.getValue().asText()));
        }

        Map<String, String> carriers = new HashMap<>();
        if (dictionaries.has("carriers")) {
            JsonNode carriersNode = dictionaries.get("carriers");
            carriersNode.fields().forEachRemaining(entry -> 
                carriers.put(entry.getKey(), entry.getValue().asText()));
        }

        return new FlightOffersProviderDTO.Dictionaries(
            locations,
            aircraft,
            currencies,
            carriers
        );
    }

}
