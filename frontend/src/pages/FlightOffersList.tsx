import React, { useMemo, useState } from "react";
import { useSearchFlight } from "@/features/todo/context/SearchFlightContext";
import FlightOfferItem from "@/features/todo/components/FlightOfferItem";
import FlightOfferModal from "@/features/todo/components/FlightOfferModal";
import { FlightOffer } from "@/features/todo/services/flightSearchService";
import { Button } from "@/components/ui/button";
import { ArrowLeft, ArrowUp, ArrowDown } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { cn } from "@/utils/utils";

const FlightOffersList = () => {
  const navigate = useNavigate();
  const {
    flightOffers,
    isLoading,
    error,
    selectedFlight,
    setSelectedFlight
  } = useSearchFlight();

  type SortKey = "price" | "duration";
  type SortDirection = "asc" | "desc";

  const [sortConfig, setSortConfig] = useState<{
    key: SortKey;
    direction: SortDirection;
  } | null>(null);

  const parseDuration = (duration: string) => {
    const hourMatch = duration.match(/(\d+)H/);
    const minuteMatch = duration.match(/(\d+)M/);
    const hours = hourMatch ? parseInt(hourMatch[1]) : 0;
    const minutes = minuteMatch ? parseInt(minuteMatch[1]) : 0;
    return hours * 60 + minutes;
  };

  const sortedFlightOffers = useMemo(() => {
    if (!flightOffers?.data) return [] as FlightOffer[];

    const offers = [...flightOffers.data];

    if (sortConfig) {
      offers.sort((a, b) => {
        let aValue: number = 0;
        let bValue: number = 0;

        if (sortConfig.key === "price") {
          aValue = parseFloat(a.price.total);
          bValue = parseFloat(b.price.total);
        } else if (sortConfig.key === "duration") {
          const totalDuration = (offer: FlightOffer) => {
            return offer.itineraries.reduce((sum, itinerary) => sum + parseDuration(itinerary.duration), 0);
          };
          aValue = totalDuration(a);
          bValue = totalDuration(b);
        }

        if (aValue < bValue) return sortConfig.direction === "asc" ? -1 : 1;
        if (aValue > bValue) return sortConfig.direction === "asc" ? 1 : -1;
        return 0;
      });
    }

    return offers;
  }, [flightOffers?.data, sortConfig]);

  const handleSort = (key: SortKey) => {
    setSortConfig((prev) => {
      if (prev && prev.key === key) {
        // Same key → toggle direction
        const newDirection: SortDirection = prev.direction === "asc" ? "desc" : "asc";
        return { key, direction: newDirection };
      }
      return { key, direction: "asc" };
    });
  };

  const handleFlightSelect = (flight: FlightOffer) => {
    setSelectedFlight(flight);
  };

  if (error) {
    return (
      <div className="space-y-4">
        <Button
          onClick={() => navigate("/")}
          variant="outline"
          className="flex items-center gap-2"
        >
          <ArrowLeft className="h-4 w-4" /> Volver a la búsqueda
        </Button>
        <div className="bg-card rounded-md border shadow-sm p-8 text-center text-destructive">
          {error}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Back button */}
      <Button
        onClick={() => navigate("/")}
        variant="outline"
        className="flex items-center gap-2"
      >
        <ArrowLeft className="h-4 w-4" /> Volver a la búsqueda
      </Button>

      {/* Sorting controls */}
      <div className="flex items-center gap-4">
        <span className="text-sm font-medium">Ordenar por:</span>
        <Button
          variant="ghost"
          className={cn("flex items-center gap-1", sortConfig?.key === "price" && "text-primary")}
          onClick={() => handleSort("price")}
          size="sm"
        >
          Precio
          {sortConfig?.key === "price" && (
            sortConfig.direction === "asc" ? <ArrowUp className="h-4 w-4" /> : <ArrowDown className="h-4 w-4" />
          )}
        </Button>
        <Button
          variant="ghost"
          className={cn("flex items-center gap-1", sortConfig?.key === "duration" && "text-primary")}
          onClick={() => handleSort("duration")}
          size="sm"
        >
          Duración
          {sortConfig?.key === "duration" && (
            sortConfig.direction === "asc" ? <ArrowUp className="h-4 w-4" /> : <ArrowDown className="h-4 w-4" />
          )}
        </Button>
      </div>

      {/* Flight offers list section */}
      <div className="space-y-6">
        {isLoading ? (
          <div className="bg-card rounded-md border shadow-sm p-8 text-center">Searching flights...</div>
        ) : !flightOffers?.data ? (
          <div className="bg-card rounded-md border shadow-sm p-8 text-center text-destructive">
            Error: No flight data found
          </div>
        ) : flightOffers.data.length === 0 ? (
          <div className="bg-card rounded-md border shadow-sm p-8 text-center text-muted-foreground">
            No flights matching your search were found.
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6">
            {sortedFlightOffers.map((flight) => (
              <FlightOfferItem
                key={flight.id}
                flightOffer={flight}
                onSelect={() => handleFlightSelect(flight)}
              />
            ))}
          </div>
        )}
      </div>

      <FlightOfferModal
        open={!!selectedFlight}
        onOpenChange={(open) => !open && setSelectedFlight(null)}
        flightOffer={selectedFlight || undefined}
      />
    </div>
  );
};

export default FlightOffersList;