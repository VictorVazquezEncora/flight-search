import { Button } from "@/components/ui/button";
import { cn } from "@/utils/utils";
import { Eye } from "lucide-react";
import React from "react";
import { FlightOffer } from "../services/flightSearchService";
import { useSearchFlight } from "../context/SearchFlightContext";

interface FlightOfferItemProps {
  flightOffer: FlightOffer;
  onSelect: () => void;
}

const FlightOfferItem: React.FC<FlightOfferItemProps> = ({ flightOffer, onSelect }) => {
  const { flightOffers } = useSearchFlight();

  const formatDateTime = (dateTime: string | any[]) => {
    let date: Date;
    if (Array.isArray(dateTime)) {
      date = new Date(dateTime[0], dateTime[1] - 1, dateTime[2], dateTime[3], dateTime[4]);
    } else {
      date = new Date(dateTime);
    }
    return {
      time: date.toLocaleString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false }),
      date: date.toLocaleString('en-US', { day: '2-digit', month: '2-digit', year: 'numeric' }),
      raw: date
    };
  };

  const formatPrice = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2
    }).format(amount);
  };

  const formatDuration = (duration: string) => {
    return duration.replace('PT', '').replace('H', 'h ').replace('M', 'm');
  };

  const calculateTotalDuration = (itinerary: any) => {
    // Get flight time in minutes
    const flightMinutes = parseDuration(itinerary.duration);
    
    // Calculate layover time
    let totalLayoverMinutes = 0;
    for (let i = 0; i < itinerary.segments.length - 1; i++) {
      const currentSegment = itinerary.segments[i];
      const nextSegment = itinerary.segments[i + 1];
      
      // Get layover start and end times
      const layoverStart = formatDateTime(currentSegment.arrival.at).raw;
      const layoverEnd = formatDateTime(nextSegment.departure.at).raw;
      
      // Calculate layover duration
      const layoverDuration = Math.round((layoverEnd.getTime() - layoverStart.getTime()) / (1000 * 60));
      totalLayoverMinutes += layoverDuration;
    }

    // Total duration is flight time plus layover time
    const totalMinutes = flightMinutes + totalLayoverMinutes;
    
    // Format the duration
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    
    return `${hours}h ${minutes}m`;
  };

  const parseDuration = (duration: string) => {
    // Handle PT format (e.g., "PT20H38M")
    const hours = duration.match(/(\d+)H/)?.[1] || '0';
    const minutes = duration.match(/(\d+)M/)?.[1] || '0';
    return parseInt(hours) * 60 + parseInt(minutes);
  };

  const getAirlineName = (carrierCode: string) => {
    const carrierName = flightOffers.dictionaries?.carriers?.[carrierCode];
    return carrierName ? `${carrierName} (${carrierCode})` : carrierCode;
  };

  const renderItinerary = (itinerary: any, index: number) => {
    const firstSegment = itinerary.segments[0];
    const lastSegment = itinerary.segments[itinerary.segments.length - 1];
    const departureInfo = formatDateTime(firstSegment.departure.at);
    const arrivalInfo = formatDateTime(lastSegment.arrival.at);
    const totalStops = itinerary.segments.length - 1;
    const totalDuration = calculateTotalDuration(itinerary);
    const flightDuration = formatDuration(itinerary.duration);

    return (
      <div key={index} className="flex flex-col gap-2">
        {/* Flight title (only for round trips) */}
        {flightOffer.itineraries.length > 1 && (
          <div className="text-sm font-medium text-gray-500">
            {index === 0 ? 'Outbound Flight' : 'Return Flight'}
          </div>
        )}

        {/* Schedule and route */}
        <div className="flex items-baseline gap-4">
          <div className="text-2xl font-semibold text-gray-900">
            {`${departureInfo.time} - ${arrivalInfo.time}`}
          </div>
          <div className="text-lg text-gray-700">
            {`${firstSegment.departure.iataCode} - ${lastSegment.arrival.iataCode}`}
          </div>
        </div>

        {/* Date */}
        <div className="text-sm text-gray-600">
          {departureInfo.date}
          {departureInfo.date !== arrivalInfo.date && ` - ${arrivalInfo.date}`}
        </div>

        {/* Duration and airline */}
        <div className="flex flex-col gap-2 text-sm text-gray-600">
          {/* Airline information */}
          <div className="flex items-center gap-2">
            <span className="font-medium">
              {getAirlineName(firstSegment.carrierCode)}
            </span>
            {firstSegment.operating?.carrierCode && 
             firstSegment.operating.carrierCode !== firstSegment.carrierCode && (
              <span className="text-gray-500">
                operated by {getAirlineName(firstSegment.operating.carrierCode)}
              </span>
            )}
          </div>
          {/* Flight duration and stops */}
          <div>
            Flight time: {flightDuration}
            <span className="text-gray-500">
              {totalStops === 0 ? ' (Direct)' : ` (${totalStops} stop${totalStops > 1 ? 's' : ''})`}
            </span>
          </div>
          {/* Total duration including layovers */}
          {totalStops > 0 && (
            <div className="text-sm text-gray-600">
              Total journey time: {totalDuration}
            </div>
          )}
        </div>

        {/* Layover information */}
        {totalStops > 0 && (
          <div className="text-sm text-gray-600">
            {itinerary.segments.map((segment: any, segIndex: number) => {
              if (segIndex < itinerary.segments.length - 1) {
                const nextSegment = itinerary.segments[segIndex + 1];
                const arrivalTime = formatDateTime(segment.arrival.at);
                const departureTime = formatDateTime(nextSegment.departure.at);
                const layoverMinutes = Math.round(
                  (departureTime.raw.getTime() - arrivalTime.raw.getTime()) / (1000 * 60)
                );
                const layoverHours = Math.floor(layoverMinutes / 60);
                const layoverMins = layoverMinutes % 60;
                return (
                  <div key={segIndex} className="ml-4">
                    Layover at {segment.arrival.iataCode}: {arrivalTime.time} - {departureTime.time}
                    <span className="text-gray-500"> ({layoverHours}h {layoverMins}m)</span>
                  </div>
                );
              }
              return null;
            })}
          </div>
        )}

        {/* Terminal information */}
        {(firstSegment.departure.terminal || lastSegment.arrival.terminal) && (
          <div className="text-sm text-gray-600">
            {firstSegment.departure.terminal && (
              <span>Departure terminal: <span className="font-medium">{firstSegment.departure.terminal}</span></span>
            )}
            {firstSegment.departure.terminal && lastSegment.arrival.terminal && ' | '}
            {lastSegment.arrival.terminal && (
              <span>Arrival terminal: <span className="font-medium">{lastSegment.arrival.terminal}</span></span>
            )}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="bg-white border rounded-md p-4 flex gap-4">
      {/* Left section - Flight information */}
      <div id="flight-offer-item-information" className="basis-2/3 flex flex-col gap-4">
        {/* Information for each itinerary */}
        {flightOffer.itineraries.map((itinerary, index) => (
          <React.Fragment key={index}>
            {index > 0 && <hr className="my-2" />}
            {renderItinerary(itinerary, index)}
          </React.Fragment>
        ))}

        {/* Available seats */}
        <div className="text-sm">
          <span className={cn(
            "font-medium",
            flightOffer.numberOfBookableSeats <= 3 ? "text-red-600" : "text-green-600"
          )}>
            {flightOffer.numberOfBookableSeats} seats available
          </span>
        </div>

        {/* Details button */}
        <div className="mt-auto">
          <Button
            onClick={onSelect}
            variant="outline"
            size="sm"
            className="w-full hover:bg-gray-50"
          >
            <Eye className="h-4 w-4 mr-2" />
            View details
          </Button>
        </div>
      </div>

      {/* Right section - Price information */}
      <div id="flight-offer-item-price" className="basis-1/3 flex flex-col justify-evenly border-l pl-4">
        {/* Total price */}
        <div className="text-right">
          <div className="text-2xl font-bold text-gray-900">
            {formatPrice(Number(flightOffer.price.total), flightOffer.price.currency)}
          </div>
          <div className="text-sm text-gray-500">total price</div>
        </div>

        {/* Price per traveler */}
        <div className="text-right mt-2">
          <div className="text-lg font-medium text-gray-700">
            {formatPrice(Number(flightOffer.travelerPricings[0].price.total), flightOffer.price.currency)}
          </div>
          <div className="text-sm text-gray-500">per traveler</div>
        </div>
      </div>
    </div>
  );
};

export default FlightOfferItem;
