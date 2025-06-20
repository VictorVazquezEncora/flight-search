
const Header = () => {

  return (
    <header className="border-b-1 border-gray-400 sticky bg-background">
      <div className="container mx-auto pt-6 pb-8 px-4 w-5/6 flex justify-center items-center">
        <div className="flex items-center">
          <h1 className="text-3xl font-bold">
          Flight Search <span className="text-purple-500 text-4xl">.</span>
          </h1>
        </div>
      </div>
    </header>
  );
};

export default Header;
