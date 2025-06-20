export interface Todo {
  id: number;
  text: string;
  dueDate?: string | null;
  done: boolean;
  doneDate?: string | null;
  priority: Priority;
  creationDate?: string | null;
}

export type Priority = "LOW" | "MEDIUM" | "HIGH";

export interface TodoFilters {
  text: string;
  priority: Priority | "ALL";
  status: "ALL" | "DONE" | "UNDONE";
}

export type SortDirection = "asc" | "dsc" | null;

export interface TodoSorting {
  sortString: string | null;
}

export interface PaginationState {
  pageSize: number;
  currentPage: number;
  totalItems: number;
}

export interface TodoMetrics {
  averageTime: number;
  byPriority: {
    [key: string]: number;
  };
}
