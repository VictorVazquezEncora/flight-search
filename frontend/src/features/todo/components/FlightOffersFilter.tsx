import { ChangeEvent, FormEvent, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Search, Loader2 } from "lucide-react";
import { FlightOfferSearchRequest } from "../services/flightSearchService";
import { useSearchFlight } from "../context/SearchFlightContext";
import { useNavigate } from "react-router-dom";
import ComboBoxAutocomplete from "@/components/ui/comboBoxAutocomplete";
import { LoadingOverlay } from "@/components/ui/loading-overlay";

const FlightOffersFilter = () => {
  const { searchFlights, searchParams } = useSearchFlight();
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  
  const [formData, setFormData] = useState<FlightOfferSearchRequest>({
    originLocationCode: searchParams.originLocationCode || "",
    destinationLocationCode: searchParams.destinationLocationCode || "",
    departureDate: searchParams.departureDate || "",
    returnDate: searchParams.returnDate,
    adults: searchParams.adults || 1,
    children: searchParams.children || 0,
    infants: searchParams.infants || 0,
    travelClass: searchParams.travelClass || "ECONOMY",
    currencyCode: searchParams.currencyCode || "USD",
    nonStop: searchParams.nonStop || false,
    maxPrice: searchParams.maxPrice
  });

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value, type } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "number" ? Number(value) : value
    }));
  };

  const handleSelectChange = (name: keyof FlightOfferSearchRequest, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
    await searchFlights(formData);
    navigate('/flight-offers');
    } catch (error) {
      console.error('Error searching flights:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <LoadingOverlay isLoading={isLoading} message="Searching for flights..." />
      <form onSubmit={handleSubmit} className="grid grid-cols-12 gap-4 min-h-[60vh] max-h-[75vh]">
      {/* Origin and Destination */}
      <div className="col-span-6">
        <ComboBoxAutocomplete
          label="Origin"
          placeholder="Origin Airport (e.g., MEX)"
          value={formData.originLocationCode}
          onValueChange={(code) =>
            setFormData((prev) => ({ ...prev, originLocationCode: code }))
          }
        />
      </div>

      <div className="col-span-6">
        <ComboBoxAutocomplete
          label="Destination"
          placeholder="Destination Airport (e.g., LAX)"
          value={formData.destinationLocationCode}
          onValueChange={(code) =>
            setFormData((prev) => ({ ...prev, destinationLocationCode: code }))
          }
        />
      </div>

      {/* Dates */}
      <div className="col-span-6">
        <label className="text-sm font-medium">Departure Date</label>
        <Input
          type="date"
          name="departureDate"
          value={formData.departureDate}
          onChange={handleInputChange}
          className="mt-1"
        />
      </div>

      <div className="col-span-6">
        <label className="text-sm font-medium">Return Date (Optional)</label>
        <Input
          type="date"
          name="returnDate"
          value={formData.returnDate}
          onChange={handleInputChange}
          className="mt-1"
        />
      </div>

      {/* Passengers */}
      <div className="col-span-4">
        <label className="text-sm font-medium">Adults</label>
        <Input
          type="number"
          name="adults"
          min={1}
          max={9}
          value={formData.adults}
          onChange={handleInputChange}
          className="mt-1"
        />
      </div>

      <div className="col-span-4">
        <label className="text-sm font-medium">Children</label>
        <Input
          type="number"
          name="children"
          min={0}
          max={9}
          value={formData.children}
          onChange={handleInputChange}
          className="mt-1"
        />
      </div>

      <div className="col-span-4">
        <label className="text-sm font-medium">Infants</label>
        <Input
          type="number"
          name="infants"
          min={0}
          max={9}
          value={formData.infants}
          onChange={handleInputChange}
          className="mt-1"
        />
      </div>

        {/* Travel Class, Currency and Max Price */}
        <div className="col-span-3">
        <label className="text-sm font-medium">Travel Class</label>
        <Select
          value={formData.travelClass}
          onValueChange={(value) => handleSelectChange("travelClass", value)}
        >
            <SelectTrigger className="w-full mt-1">
            <SelectValue placeholder="Select class" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ECONOMY">Economy</SelectItem>
            <SelectItem value="PREMIUM_ECONOMY">Premium Economy</SelectItem>
            <SelectItem value="BUSINESS">Business</SelectItem>
            <SelectItem value="FIRST">First</SelectItem>
          </SelectContent>
        </Select>
      </div>

        <div className="col-span-3">
        <label className="text-sm font-medium">Currency</label>
        <Select
          value={formData.currencyCode}
          onValueChange={(value) => handleSelectChange("currencyCode", value)}
        >
            <SelectTrigger className="w-full mt-1">
            <SelectValue placeholder="Select currency" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="USD">USD</SelectItem>
            <SelectItem value="MXN">MXN</SelectItem>
            <SelectItem value="EUR">EUR</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="col-span-6">
        <label className="text-sm font-medium">Max Price</label>
        <Input
          type="number"
          name="maxPrice"
          min={0}
          value={formData.maxPrice}
          onChange={handleInputChange}
          placeholder="Maximum price"
          className="mt-1"
        />
      </div>

      {/* Search Button */}
      <div className="col-span-12">
        <Button
          type="submit"
          className="w-full bg-background border-2 hover:bg-accent text-foreground"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                Searching...
              </>
            ) : (
              <>
                <Search className="h-4 w-4 mr-2" />
                Search Flights
              </>
            )}
        </Button>
      </div>
    </form>
    </>
  );
};

export default FlightOffersFilter;
