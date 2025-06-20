import { toast } from "sonner";
import { useSearchFlight } from "../context/SearchFlightContext";
import FlightOffersFilter from "./FlightOffersFilter";
import { FlightOfferSearchRequest } from "../services/flightSearchService";
import { useNavigate } from "react-router-dom";
import FlightOffersList from "@/pages/FlightOffersList";

const FlightSearchFilter = () => {
  const { searchFlights, searchParams } = useSearchFlight();
  const navigate = useNavigate();

  const handleSearch = async (searchRequest: FlightOfferSearchRequest) => {
    try {
      await searchFlights(searchRequest);
      navigate("/flight-offers");
    } catch (error) {
      console.error("Error searching flights:", error);
      toast.error("Error", {
        description: "Error searching flights. Please try again.",
      });
    }
  };

  return (
    <div className="space-y-6 flex flex-col items-center justify-center">
      {/* Filters section */}
      <div className="bg-card p-4 rounded-md border shadow-sm w-full max-w-4xl">
        <FlightOffersFilter/>
      </div>

    </div>
  );
};

export default FlightSearchFilter;
