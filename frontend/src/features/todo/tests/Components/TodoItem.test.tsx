import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import TodoItem from "../../components/FlightOfferItem";
import { Todo } from "../../types";

jest.mock("../../context/useTodo", () => ({
  useTodo: () => ({
    toggleTodoStatus: jest.fn(),
  }),
}));

describe("TodoItem", () => {
  const mockOnDelete = jest.fn();
  const mockOnEdit = jest.fn();

  const mockTodoWithDate: Todo = {
    id: 1,
    text: "New Task 1",
    priority: "HIGH",
    done: false,
    dueDate: "2024-03-20T12:00:00Z",
  };

  const mockTodoWithoutDate: Todo = {
    id: 2,
    text: "New Task 2",
    priority: "HIGH",
    done: false,
    dueDate: null,
  };

  it("should render with checkbox", () => {
    render(
      <TodoItem
        todo={mockTodoWithDate}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );
    expect(screen.getByRole("checkbox")).toBeInTheDocument();
  });

  it("should show the correct priority with proper styling", () => {
    render(
      <TodoItem
        todo={mockTodoWithDate}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );
    const priorityBadge = screen.getByText("HIGH");
    expect(priorityBadge).toBeInTheDocument();
    expect(priorityBadge).toHaveClass("bg-red-100", "text-red-800");
  });

  it("should show the correct task name", () => {
    render(
      <TodoItem
        todo={mockTodoWithDate}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );
    expect(screen.getByText("New Task 1")).toBeInTheDocument();
  });

  it("should show the correct due date when provided", () => {
    render(
      <TodoItem
        todo={mockTodoWithDate}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );
    expect(screen.getByText("Mar 20, 2024")).toBeInTheDocument();
  });

  it("should show dash when no due date is provided", () => {
    render(
      <TodoItem
        todo={mockTodoWithoutDate}
        onDelete={mockOnDelete}
        onEdit={mockOnEdit}
      />
    );
    expect(screen.getByText("-")).toBeInTheDocument();
  });
});
