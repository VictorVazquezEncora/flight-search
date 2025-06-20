import { useContext } from "react";
import { SearchFlightContext } from "./SearchFlightContext";

export const useSearchFlightContext = () => {
  const context = useContext(SearchFlightContext);
  return context;
};
