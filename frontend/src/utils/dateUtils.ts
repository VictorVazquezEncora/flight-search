import { differenceInDays, format, parseISO } from "date-fns";

export const formatDate = (dateString: string | null): string => {
  if (!dateString) return "";
  try {
    return format(parseISO(dateString), "MMM d, yyyy");
  } catch (error) {
    console.error("Error formatting date:", error);
    return "Invalid date";
  }
};

export const getTimeDifferenceInMinutes = (
  startDate: string,
  endDate: string
): number => {
  try {
    const start = parseISO(startDate);
    const end = parseISO(endDate);

    const diffMs = end.getTime() - start.getTime();
    return Math.floor(diffMs / (1000 * 60));
  } catch (error) {
    console.error("Error calculating time difference:", error);
    return 0;
  }
};

export const getDueDateUrgency = (
  dueDate: string | null
): "urgent" | "moderate" | "normal" | null => {
  if (!dueDate) return null;

  try {
    const today = new Date();
    const dueDateObj = parseISO(dueDate);
    const daysUntilDue = differenceInDays(dueDateObj, today);

    if (daysUntilDue <= 7) {
      return "urgent";
    } else if (daysUntilDue <= 14) {
      return "moderate";
    } else {
      return "normal";
    }
  } catch (error) {
    console.error("Error determining due date urgency:", error);
    return null;
  }
};

export const formatMinutes = (minutes: number): string => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;

  if (hours > 0) {
    return `${hours}h ${mins}m`;
  }
  return `${mins}m`;
};

// Calculate total flight journey time considering timezones and layovers
export const calculateTotalJourneyTime = (
  departureDateTime: string | Date,
  arrivalDateTime: string | Date,
  layoverDuration?: number // in minutes
): number => {
  try {
    const departure = typeof departureDateTime === 'string' ? parseISO(departureDateTime) : departureDateTime;
    const arrival = typeof arrivalDateTime === 'string' ? parseISO(arrivalDateTime) : arrivalDateTime;

    const diffMs = arrival.getTime() - departure.getTime();
    const diffMinutes = Math.floor(diffMs / (1000 * 60));

    const totalMinutes = layoverDuration ? diffMinutes + layoverDuration : diffMinutes;

    return totalMinutes;
  } catch (error) {
    console.error("Error calculating total journey time:", error);
    return 0;
  }
};

export const formatDuration = (minutes: number): string => {
  const days = Math.floor(minutes / (24 * 60));
  const hours = Math.floor((minutes % (24 * 60)) / 60);
  const mins = minutes % 60;

  let duration = '';
  if (days > 0) duration += `${days}d `;
  if (hours > 0) duration += `${hours}h `;
  duration += `${mins}m`;

  return duration.trim();
};
