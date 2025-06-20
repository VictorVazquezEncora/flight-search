import { useSearchFlight } from "@/features/todo/context/SearchFlightContext";
import FlightOfferItem from "@/features/todo/components/FlightOfferItem";
import FlightOfferModal from "@/features/todo/components/FlightOfferModal";
import { FlightOffer } from "@/features/todo/services/flightSearchService";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";

const FlightOffersList = () => {
  const navigate = useNavigate();
  const {
    getPaginatedFlightOffers,
    flightOffers,
    isLoading,
    error,
    selectedFlight,
    setSelectedFlight
  } = useSearchFlight();

  const paginatedFlightOffers = getPaginatedFlightOffers();

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
            {paginatedFlightOffers.map((flight) => (
              <FlightOfferItem
                key={flight.id}
                flightOffer={flight}
                onSelect={() => handleFlightSelect(flight)}
              />
            ))}
          </div>
        )}
      </div>

      {/* Pagination
      {flightOffers?.data && flightOffers.data.length > 0 && (
        <div className="flex justify-center mt-4">
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => setPage(currentPage - 1)}
                  className={currentPage === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                />
              </PaginationItem>

              {(() => {
                const visiblePages = [];
                const maxVisiblePages = 5;
                const halfVisible = Math.floor(maxVisiblePages / 2);

                visiblePages.push(
                  <PaginationItem key={0}>
                    <PaginationLink
                      onClick={() => setPage(0)}
                      isActive={currentPage === 0}
                      className="cursor-pointer"
                    >
                      1
                    </PaginationLink>
                  </PaginationItem>
                );

                let startPage = Math.max(1, currentPage - halfVisible);
                let endPage = Math.min(totalPages - 2, currentPage + halfVisible);

                if (currentPage <= halfVisible) {
                  endPage = Math.min(totalPages - 2, maxVisiblePages - 1);
                } else if (currentPage >= totalPages - halfVisible - 1) {
                  startPage = Math.max(1, totalPages - maxVisiblePages);
                }

                if (startPage > 1) {
                  visiblePages.push(
                    <PaginationItem key="dots-1">
                      <PaginationLink className="cursor-default">...</PaginationLink>
                    </PaginationItem>
                  );
                }

                for (let i = startPage; i <= endPage; i++) {
                  visiblePages.push(
                    <PaginationItem key={i}>
                      <PaginationLink
                        onClick={() => setPage(i)}
                        isActive={currentPage === i}
                        className="cursor-pointer"
                      >
                        {i + 1}
                      </PaginationLink>
                    </PaginationItem>
                  );
                }

                if (endPage < totalPages - 2) {
                  visiblePages.push(
                    <PaginationItem key="dots-2">
                      <PaginationLink className="cursor-default">...</PaginationLink>
                    </PaginationItem>
                  );
                }

                if (totalPages > 1) {
                  visiblePages.push(
                    <PaginationItem key={totalPages - 1}>
                      <PaginationLink
                        onClick={() => setPage(totalPages - 1)}
                        isActive={currentPage === totalPages - 1}
                        className="cursor-pointer"
                      >
                        {totalPages}
                      </PaginationLink>
                    </PaginationItem>
                  );
                }

                return visiblePages;
              })()}

              <PaginationItem>
                <PaginationNext
                  onClick={() => setPage(currentPage + 1)}
                  className={
                    currentPage >= totalPages - 1
                      ? "pointer-events-none opacity-50"
                      : "cursor-pointer"
                  }
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      )} */}

      <FlightOfferModal
        open={!!selectedFlight}
        onOpenChange={(open) => !open && setSelectedFlight(null)}
        flightOffer={selectedFlight || undefined}
      />
    </div>
  );
};

export default FlightOffersList;