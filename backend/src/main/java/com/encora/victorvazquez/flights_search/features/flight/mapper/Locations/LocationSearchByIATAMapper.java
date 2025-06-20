package com.encora.victorvazquez.flights_search.features.flight.mapper.Locations;

import com.encora.victorvazquez.flights_search.features.flight.domain.Locations.LocationSearch;
import com.encora.victorvazquez.flights_search.features.flight.dto.Locations.LocationSearchRequestDTO;
import com.encora.victorvazquez.flights_search.features.flight.dto.Locations.LocationByIATAProviderDTO;
import com.encora.victorvazquez.flights_search.features.flight.domain.Locations.Location;

import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
public class LocationSearchByIATAMapper {

    public Location toDTO(LocationByIATAProviderDTO response) {
        var data = response.data();
        return new Location(
            data.id(),
            mapLinks(data.self()),
            data.type(),
            data.subType(),
            data.name(),
            data.detailedName(),
            data.timeZoneOffset(),
            data.iataCode(),
            mapGeoCode(data.geoCode()),
            mapAddress(data.address()),
            null,
            mapAnalytics(data.analytics()),
            null,
            null,
            Collections.emptyList(),
            null
        );
    }

    private Location.Links mapLinks(LocationByIATAProviderDTO.Self self) {
        return new Location.Links(
            self.href(),
            self.methods(),
            null
        );
    }

    private Location.GeoCode mapGeoCode(LocationByIATAProviderDTO.GeoCode geoCode) {
        return new Location.GeoCode(
            geoCode.latitude(),
            geoCode.longitude()
        );
    }

    private Location.Address mapAddress(LocationByIATAProviderDTO.Address address) {
        return new Location.Address(
            address.cityName(),
            address.cityCode(),
            address.countryName(),
            address.countryCode(),
            null,
            address.regionCode()
        );
    }

    private Location.Analytics mapAnalytics(LocationByIATAProviderDTO.Analytics analytics) {
        return new Location.Analytics(
            new Location.Analytics.Travelers(
                analytics.travelers().score()
            )
        );
    }
} 