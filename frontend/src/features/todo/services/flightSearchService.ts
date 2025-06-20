import { get } from "../../../lib/fetch";
import { TodoFilters, PaginationState } from "../types";

const buildQueryParams = (
  filters?: TodoFilters,
  pagination?: Partial<PaginationState>,
  sortString?: string | null
): string => {
  const params = new URLSearchParams();

  if (filters) {
    if (filters.text && filters.text.trim() !== "") {
      params.append("text", filters.text);
    }

    if (filters.priority && filters.priority !== "ALL") {
      params.append("priority", filters.priority);
    }

    if (filters.status && filters.status !== "ALL") {
      params.append("status", filters.status.toLowerCase());
    }
  }

  if (pagination) {
    if (pagination.pageSize) {
      params.append("size", pagination.pageSize.toString());
    }

    if (pagination.currentPage !== undefined) {
      params.append("page", pagination.currentPage.toString());
    }
  }

  if (sortString) {
    params.append("sortBy", sortString);
  }

  const queryString = params.toString();
  return queryString ? `?${queryString}` : "";
};

// Types
export interface FlightOfferSearchRequest {
  originLocationCode: string;
  destinationLocationCode: string;
  departureDate: string;
  returnDate?: string;
  adults: number;
  children?: number;
  infants?: number;
  travelClass?: 'ECONOMY' | 'PREMIUM_ECONOMY' | 'BUSINESS' | 'FIRST';
  includedAirlineCodes?: string;
  excludedAirlineCodes?: string;
  nonStop?: boolean;
  currencyCode?: 'USD' | 'MXN' | 'EUR';
  maxPrice?: number;
}

export interface LocationSearchRequest {
  subType: 'AIRPORT' | 'CITY';
  keyword: string;
  countryCode?: string;
  pageLimit?: number;
  pageOffset?: number;
  sort?: string;
  view?: 'LIGHT' | 'FULL';
}

// Response types
export interface FlightOffersResponse {
  meta: {
    count: number;
    links: {
      self: string;
    };
  };
  data: FlightOffer[];
  dictionaries?: Dictionaries;
}

export interface Dictionaries {
  locations: {
    [key: string]: {
      cityCode: string;
      countryCode: string;
      name: string | null;
      detailedName: string | null;
      timeZoneOffset: string | null;
      latitude: number;
      longitude: number;
    };
  };
  aircraft: {
    [key: string]: string;
  };
  currencies: {
    [key: string]: string;
  };
  carriers: {
    [key: string]: string;
  };
}

export interface FlightOffer {
  id: string;
  source: string;
  instantTicketingRequired: boolean;
  nonHomogeneous: boolean;
  oneWay: boolean;
  lastTicketingDate: string;
  numberOfBookableSeats: number;
  itineraries: Itinerary[];
  price: Price;
  pricingOptions: PricingOptions;
  validatingAirlineCodes: string[];
  travelerPricings: TravelerPricing[];
}

interface Itinerary {
  duration: string;
  segments: Segment[];
}

interface Segment {
  departure: Location;
  arrival: Location;
  carrierCode: string;
  number: string;
  aircraft: {
    code: string;
  };
  operating: {
    carrierCode: string;
  };
  duration: string;
  id: string;
  numberOfStops: number;
  blacklistedInEU: boolean;
}

interface Location {
  iataCode: string;
  terminal?: string;
  at: string;
}

interface Price {
  currency: string;
  total: string;
  base: string;
  fees: Fee[];
  grandTotal: string;
}

interface Fee {
  amount: string;
  type: string;
}

interface PricingOptions {
  fareType: string[];
  includedCheckedBagsOnly: boolean;
}

interface TravelerPricing {
  travelerId: string;
  fareOption: string;
  travelerType: string;
  price: Price;
  fareDetailsBySegment: FareDetailsBySegment[];
}

interface Amenity {
  description: string;
  isChargeable: boolean;
  amenityType: string;
  amenityProvider: {
    name: string;
  };
}

interface FareDetailsBySegment {
  segmentId: string;
  cabin: string;
  fareBasis: string;
  classType: string;
  includedCheckedBags: {
    weight: number;
    weightUnit: string;
    quantity?: number;
  };
  includedCabinBags?: {
    weight: number;
    weightUnit: string;
    quantity?: number;
  };
  amenities?: Amenity[];
}

export interface LocationSearchResponse {
  meta: {
    count: number;
    links: {
      href: string;
      methods: string[];
    };
  };
  data: LocationDTO[];
}

export interface LocationDTO {
  id: string;
  self: {
    href: string;
    methods: string[];
  };
  type: string;
  subType: string;
  name: string;
  detailedName: string;
  timeZoneOffset: string;
  iataCode: string;
  geoCode: {
    latitude: number;
    longitude: number;
  };
  address: {
    cityName: string;
    cityCode: string;
    countryName: string;
    countryCode: string;
    regionCode: string;
  };
  distance: {
    value: number;
    unit: string;
  };
  analytics: {
    travelers: {
      score: number;
    };
  };
  relevance: number;
  category: string;
  tags: string[];
  rank: string;
}

// Service functions
export const searchFlights = async (request: FlightOfferSearchRequest): Promise<FlightOffersResponse> => {
  const queryParams = new URLSearchParams();
  
  const DEFAULT_MAX_RESULTS = 15;
  
  Object.entries(request).forEach(([key, value]) => {
    if (value !== undefined) {
      queryParams.append(key, value.toString());
    }
  });
  
  // Always append max parameter with default value
  queryParams.append('max', DEFAULT_MAX_RESULTS.toString());

  const response = await get<FlightOffersResponse>(`/api/flights?${queryParams.toString()}`);
  
  if (!response.meta || !response.data) {
    throw new Error('Respuesta del servidor inválida: falta meta o data');
  }

  return response;
};

export const searchLocations = async (request: LocationSearchRequest): Promise<LocationSearchResponse> => {
  const queryParams = new URLSearchParams();
  
  Object.entries(request).forEach(([key, value]) => {
    if (value !== undefined) {
      queryParams.append(key, value.toString());
    }
  });

  return await get<LocationSearchResponse>(`/api/locations?${queryParams.toString()}`);
};
