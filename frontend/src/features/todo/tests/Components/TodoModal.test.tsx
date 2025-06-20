import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import TodoModal from "../../components/FlightOfferModal";

describe("TodoModal", () => {
  it("should render", () => {
    render(<TodoModal open={true} onOpenChange={() => {}} mode="create" />);
    expect(screen.getByText("Task Name")).toBeInTheDocument();
  });

  it("should render with correct title in create mode", () => {
    render(<TodoModal open={true} onOpenChange={() => {}} mode="create" />);
    expect(screen.getByText("Create New Task")).toBeInTheDocument();
  });

  it("should render with correct title in edit mode", () => {
    render(<TodoModal open={true} onOpenChange={() => {}} mode="edit" />);
    expect(screen.getByText("Edit Task")).toBeInTheDocument();
  });

  it("should render with correct button in create mode", () => {
    render(<TodoModal open={true} onOpenChange={() => {}} mode="create" />);
    expect(
      screen.getByRole("button", { name: "Create Task" })
    ).toBeInTheDocument();
  });

  it("should render with correct button in edit mode", () => {
    render(<TodoModal open={true} onOpenChange={() => {}} mode="edit" />);
    expect(
      screen.getByRole("button", { name: "Update Task" })
    ).toBeInTheDocument();
  });
});
