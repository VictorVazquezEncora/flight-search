import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { FlightOffer } from "../services/flightSearchService";
import { useSearchFlight } from "../context/SearchFlightContext";
import { formatDuration } from "@/utils/utils";

interface FlightOfferModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  flightOffer?: FlightOffer;
}

const FlightOfferModal: React.FC<FlightOfferModalProps> = ({
  open,
  onOpenChange,
  flightOffer,
}) => {
  const { flightOffers } = useSearchFlight();

  if (!flightOffer) return null;

  const formatDateTime = (dateTime: string) => {
    try {
      if (Array.isArray(dateTime)) {
        const [year, month, day, hour, minute] = dateTime;
        return {
          time: `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`,
          date: `${String(month).padStart(2, '0')}/${String(day).padStart(2, '0')}/${year}`,
          raw: new Date(year, month - 1, day, hour || 0, minute || 0)
        };
      }

      const date = new Date(dateTime);
      return {
        time: date.toLocaleTimeString('en-US', { 
          hour: '2-digit', 
          minute: '2-digit', 
          hour12: false,
          timeZone: 'UTC'
        }),
        date: date.toLocaleDateString('en-US', {
          month: '2-digit',
          day: '2-digit',
          year: 'numeric',
          timeZone: 'UTC'
        }),
        raw: date
      };
    } catch (error) {
      return {
        time: '--:--',
        date: '--/--/----',
        raw: new Date()
      };
    }
  };

  const getAirlineName = (carrierCode: string) => {
    return flightOffers.dictionaries?.carriers?.[carrierCode] || carrierCode;
  };

  const getAircraftName = (aircraftCode: string) => {
    return flightOffers.dictionaries?.aircraft?.[aircraftCode] || aircraftCode;
  };

  const calculateLayoverTime = (currentSegment: any, nextSegment: any) => {
    const parseDate = (dt: any): Date | null => {
      if (!dt) return null;
      if (Array.isArray(dt)) {
        const [year, month, day, hour = 0, minute = 0] = dt;
        return new Date(year, month - 1, day, hour, minute);
      }
      // string ISO
      const parsed = new Date(dt);
      return isNaN(parsed.getTime()) ? null : parsed;
    };

    const arrivalTime = parseDate(currentSegment?.arrival?.at);
    const departureTime = parseDate(nextSegment?.departure?.at);

    if (!arrivalTime || !departureTime) return null;

    const diffMinutes = Math.round((departureTime.getTime() - arrivalTime.getTime()) / (1000 * 60));
    if (diffMinutes <= 0 || isNaN(diffMinutes)) return null;

    const hours = Math.floor(diffMinutes / 60);
    const minutes = diffMinutes % 60;
    return `${hours}h ${minutes}m`;
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="w-[90vw] max-w-[90vw] lg:max-w-[1600px] p-8 max-h-[80vh]">
        <DialogHeader>
          <DialogTitle>Flight Details</DialogTitle>
        </DialogHeader>

        <div className="grid grid-cols-1 md:grid-cols-12 gap-8 pt-4">
          {/* Left section - Segments and Fare Details */}
          <div className="md:col-span-8 space-y-6 max-h-[70vh] overflow-y-auto pb-10 pr-2">
            {flightOffer.itineraries.map((itinerary, itineraryIndex) => (
              <div key={itineraryIndex} className="space-y-4">
                {/* Journey type header */}
                {flightOffer.itineraries.length > 1 && (
                  <h3 className="text-lg font-semibold">
                    {itineraryIndex === 0 ? 'Outbound Flight' : 'Return Flight'}
                  </h3>
                )}

                {/* Segments */}
                {itinerary.segments.map((segment, segmentIndex) => (
                  <div key={segmentIndex} className="border rounded-lg p-4 space-y-4">
                    {/* Segment header */}
                    <div className="flex justify-between items-center">
                      <h4 className="font-medium">Segment {segmentIndex + 1}</h4>
                      <span className="text-sm text-gray-500">
                        {formatDateTime(segment.departure.at).date}
                      </span>
                    </div>

                    {/* Flight route and times */}
                    <div className="flex justify-between items-center">
                      <div className="space-y-1">
                        <div className="text-xl font-semibold">
                          {formatDateTime(segment.departure.at).time}
                        </div>
                        <div className="text-sm text-gray-600">
                          {segment.departure.iataCode}
                          {segment.departure.terminal && ` Terminal ${segment.departure.terminal}`}
                        </div>
                      </div>

                      <div className="flex-1 px-4 text-center">
                        <div className="text-sm text-gray-500">
                          {formatDuration(segment.duration)}
                        </div>
                        <div className="border-t my-2" />
                        <div className="text-sm">
                          Flight {segment.carrierCode}
                          {segment.number}
                        </div>
                      </div>

                      <div className="space-y-1 text-right">
                        <div className="text-xl font-semibold">
                          {formatDateTime(segment.arrival.at).time}
                        </div>
                        <div className="text-sm text-gray-600">
                          {segment.arrival.iataCode}
                          {segment.arrival.terminal && ` Terminal ${segment.arrival.terminal}`}
                        </div>
                      </div>
                    </div>

                    {/* Airline and aircraft details */}
                    <div className="space-y-2 text-sm text-gray-600">
                      <div>
                        Operated by: {getAirlineName(segment.carrierCode)}
                        {segment.operating?.carrierCode !== segment.carrierCode && (
                          <span className="text-gray-500">
                            {" "}(operated by {getAirlineName(segment.operating.carrierCode)})
                          </span>
                        )}
                      </div>
                      <div>Aircraft: {getAircraftName(segment.aircraft.code)}</div>
                    </div>

                    {/* Traveler fare details */}
                    <div className="mt-4 border-t pt-4">
                      <h5 className="font-medium mb-2">Traveler Fare Details</h5>
                      {flightOffer.travelerPricings.map((pricing, index) => {
                        const fareDetail = pricing.fareDetailsBySegment.find(
                          (fare) => fare.segmentId === segment.id
                        );
                        return fareDetail ? (
                          <div key={index} className="text-sm space-y-1">
                            <div className="flex flex-col md:flex-row md:justify-between md:items-start gap-4 pb-2">
                              {/* Basic data */}
                              <div className="space-y-1 md:w-1/2">
                                <div>Cabin: {fareDetail.cabin}</div>
                                <div>Class: {fareDetail.classType}</div>
                                <div>Fare Basis: {fareDetail.fareBasis}</div>
                                {fareDetail.includedCheckedBags && (
                                  <div>
                                    Included Baggage: {fareDetail.includedCheckedBags.weight}
                                    {fareDetail.includedCheckedBags.weightUnit}
                                  </div>
                                )}
                              </div>

                              {/* Amenities */}
                              {fareDetail.amenities && fareDetail.amenities.length > 0 && (
                                <div className="pt-2 md:pt-0 md:w-1/2 md:text-right">
                                  <div className="font-medium">Amenities:</div>
                                  <ul className="list-disc list-inside md:list-none md:inline-block text-left md:text-right space-y-1 md:space-y-0">
                                    {fareDetail.amenities.map((amenity, amenityIndex) => (
                                      <li key={amenityIndex}>
                                        {amenity.description} {" "}
                                        <span className="text-gray-500">
                                          ({amenity.isChargeable ? "Chargeable" : "Free"})
                                        </span>
                                      </li>
                                    ))}
                                  </ul>
                                </div>
                              )}
                            </div>
                          </div>
                        ) : null;
                      })}
                    </div>
                  </div>
                ))}

                {/* Layover information */}
                {itinerary.segments.length > 1 && (
                  <div className="space-y-2">
                    {itinerary.segments.slice(0, -1).map((segment, index) => (
                      <div key={index} className="text-sm text-gray-600 pl-4">
                        {
                          (() => {
                            const layover = calculateLayoverTime(segment, itinerary.segments[index + 1]);
                            return `Layover at ${segment.arrival.iataCode}: ${layover ?? 'No layover'}`;
                          })()
                        }
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Right section - Price Breakdown */}
          <div className="md:col-span-4 border-t md:border-t-0 md:border-l pt-6 md:pt-0 md:pl-8">
            <div className="space-y-4 sticky top-4">
              <h3 className="text-xl font-semibold">Price Breakdown</h3>
              
              {/* Base price */}
              <div className="flex justify-between text-sm">
                <span>Base Price</span>
                <span>{flightOffer.price.base} {flightOffer.price.currency}</span>
              </div>

              {/* Fees */}
              <div className="flex justify-between text-sm">
                <span>Fees</span>
                <span>{(Number(flightOffer.price.total) - Number(flightOffer.price.base)).toFixed(2)} {flightOffer.price.currency}</span>
              </div>

              {/* Grand Total */}
              <div className="pt-4 border-t">
                <div className="flex justify-between font-semibold">
                  <span>Total</span>
                  <span>{(Number(flightOffer.price.total)).toFixed(2)} {flightOffer.price.currency}</span>
                </div>
              </div>

              {/* Price per traveler */}
              <div className="flex justify-between text-sm pt-2">
                <span>Price per Traveler ({flightOffer.travelerPricings.length})</span>
                <span>{(Number(flightOffer.price.total) / flightOffer.travelerPricings.length).toFixed(2)} {flightOffer.price.currency}</span>
              </div>

            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default FlightOfferModal;
