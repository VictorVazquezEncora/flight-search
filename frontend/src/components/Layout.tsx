import Header from "./Header";
import { Outlet } from "react-router-dom";
import { SearchFlightProvider } from "@/features/todo/context/SearchFlightContext";

const Layout = () => {
  return (
    <SearchFlightProvider>
      <div className="min-h-screen flex flex-col">
        <Header />
        <div className="p-4 w-3/4 self-center space-y-6">
          <Outlet />
        </div>
      </div>
    </SearchFlightProvider>
  );
};

export default Layout; 