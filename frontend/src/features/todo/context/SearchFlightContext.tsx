import { createContext, useContext, useState } from "react";
import { 
  FlightOffer, 
  FlightOfferSearchRequest, 
  FlightOffersResponse, 
  Dictionaries,
  searchFlights as searchFlightsService 
} from "../services/flightSearchService";

interface SearchFlightContextType {
  searchParams: FlightOfferSearchRequest;
  setSearchParams: (params: FlightOfferSearchRequest) => void;
  
  flightOffers: FlightOffersResponse;
  selectedFlight: FlightOffer | null;
  setSelectedFlight: (flight: FlightOffer | null) => void;
  
  meta: {
    count: number;
    links: {
      self: string;
    };
  } | null;
  dictionaries: Dictionaries | undefined;
  
  isLoading: boolean;
  error: string | null;
  
  currentPage: number;
  itemsPerPage: number;
  totalPages: number;
  setPage: (page: number) => void;
  
  searchFlights: (request: FlightOfferSearchRequest) => Promise<void>;
  getPaginatedFlightOffers: () => FlightOffer[];
}

export const SearchFlightContext = createContext<SearchFlightContextType | undefined>(undefined);

export const SearchFlightProvider = ({ children }: { children: React.ReactNode }) => {
  const [searchParams, setSearchParams] = useState<FlightOfferSearchRequest>({
    originLocationCode: "",
    destinationLocationCode: "",
    departureDate: "",
    adults: 1,
  });

  const [flightOffers, setFlightOffers] = useState<FlightOffersResponse>({
    meta: {
      count: 0,
      links: {
        self: ""
      }
    },
    data: [],
    dictionaries: undefined
  });
  const [selectedFlight, setSelectedFlight] = useState<FlightOffer | null>(null);
  
  const [meta, setMeta] = useState<{ count: number; links: { self: string } } | null>(null);
  const [dictionaries, setDictionaries] = useState<Dictionaries | undefined>(undefined);

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [currentPage, setCurrentPage] = useState(0);
  const itemsPerPage = 15;

  const totalPages = Math.max(1, Math.ceil(flightOffers.data.length / itemsPerPage));

  const searchFlights = async (request: FlightOfferSearchRequest) => {
    try {
      setIsLoading(true);
      setError(null);
      setSelectedFlight(null);
      
      const response = await searchFlightsService(request);
      
      if (response && response.meta && Array.isArray(response.data)) {
        setFlightOffers(response);
        setMeta(response.meta);
        setDictionaries(response.dictionaries);
      } else {
        setFlightOffers({ meta: { count: 0, links: { self: "" } }, data: [], dictionaries: undefined });
        setMeta(null);
        setDictionaries(undefined);
        setError("The response structure is invalid");
      }
      
      setSearchParams(request);
      setCurrentPage(0);
    } catch (error) {
      console.error("Error searching flights:", error);
      setError(error instanceof Error ? error.message : "Error searching flights. Please try again.");
      setFlightOffers({ meta: { count: 0, links: { self: "" } }, data: [], dictionaries: undefined });
      setMeta(null);
      setDictionaries(undefined);
    } finally {
      setIsLoading(false);
    }
  };

  const getPaginatedFlightOffers = (): FlightOffer[] => {
    const start = currentPage * itemsPerPage;
    const end = start + itemsPerPage;
    return flightOffers.data.slice(start, end);
  };

  const setPage = (page: number) => {
    if (page >= 0 && page < totalPages) {
      setCurrentPage(page);
    }
  };

  return (
    <SearchFlightContext.Provider
      value={{
        searchParams,
        setSearchParams,
        
        flightOffers,
        selectedFlight,
        setSelectedFlight,
        
        meta,
        dictionaries,
        
        isLoading,
        error,
        
        currentPage,
        itemsPerPage,
        totalPages,
        setPage,
        
        searchFlights,
        getPaginatedFlightOffers,
      }}
    >
      {children}
    </SearchFlightContext.Provider>
  );
};

export const useSearchFlight = () => {
  const context = useContext(SearchFlightContext);
  if (context === undefined) {
    throw new Error("useSearchFlight must be used within a SearchFlightProvider");
  }
  return context;
};
