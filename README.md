# N-Queens Solver — Parallel & Animated JavaFX Application

## Overview

This project is a JavaFX-based **N-Queens Solver** that supports:

- ✔ Sequential solving
- ✔ Multi-core parallel solving
- ✔ Real-time board animation
- ✔ Listing and visualizing all found solutions
- ✔ Clean architecture with strict separation of concerns

The solver has been **fully refactored** to follow **SOLID principles**, ensuring:
- Algorithm purity
- Thread safety
- Clear responsibility boundaries
- Maintainable and extensible code

---

## Problem Description

The **N-Queens Problem** requires placing `N` queens on an `N × N` chessboard such that:

- No two queens share the same row
- No two queens share the same column
- No two queens share the same diagonal

---

## Original Design Problems (Before Refactoring)

The initial implementation suffered from several architectural issues:

### ❌ Mixed Responsibilities
- The solver class handled:
    - Algorithm logic
    - Threading
    - Task control
    - UI callbacks

### ❌ Tight Coupling
- UI components depended directly on solver internals
- Threading logic was embedded in algorithm code

### ❌ Poor Separation of Concerns
- No clear boundary between:
    - Algorithm
    - Execution strategy
    - UI rendering
    - Solution storage

### ❌ Hard to Extend
- Parallel execution was difficult to modify
- Testing the algorithm independently was nearly impossible

---

## Refactored Architecture (Current Design)

The project now follows a **layered architecture**:

```
UI (JavaFX)
│
├── Controller Layer
│ └── MainController
│
├── Worker Layer
│ └── ParallelSolverWorker
│
├── Task Layer
│ └── SolverTask
│
├── Solver Layer
│ └── BacktrackingSolver (Pure Algorithm)
│
└── Model Layer
├── Solution
└── SolutionStore
```

Each layer has a **single responsibility**.

---
## Class Responsibilities

### `BacktrackingSolver`
**Role:** Pure algorithm provider

- Implements classic backtracking for N-Queens
- No threading
- No UI logic
- No shared state
- Thread-interruption aware (for parallel cancellation)

✅ Easily testable  
✅ Reusable  
✅ Deterministic

---

### `SolverTask`
**Role:** One parallel branch of computation

- Fixes the queen in the first row
- Executes solver starting from row 1
- Coordinates early termination via shared flags
- Stops all tasks when a solution is found (if not `findAll`)

---

### `ParallelSolverWorker`
**Role:** Thread orchestration

- Creates thread pool based on available CPU cores
- Submits one `SolverTask` per first-row column
- Collects results
- Handles cancellation and shutdown
- Communicates results via callbacks

🚫 No algorithm logic  
🚫 No UI logic

---

### `SolutionStore`
**Role:** Solution persistence

- Thread-safe storage of solutions
- Used by UI to display and select solutions
- Decouples storage from solver and UI

---

### `MainController`
**Role:** JavaFX UI coordinator

- Handles user input
- Starts and stops solvers
- Updates board visualization
- Displays solution list
- Controls animation

🚫 No algorithm code  
🚫 No threading code

---

## Multithreading Strategy

Parallelism is achieved by:

1. Fixing the queen position in **row 0**
2. Assigning each column to a separate `SolverTask`
3. Running tasks in a fixed-size thread pool
4. Using:
    - `AtomicBoolean` for early termination
    - `ExecutorService` for task management
    - `shutdownNow()` for cooperative cancellation

This approach:
- Maximizes CPU usage
- Avoids shared mutable state
- Allows safe early exit

---

## Solution Flow

1. User clicks **Solve**
2. `MainController` initializes the board and worker
3. `ParallelSolverWorker` spawns tasks
4. Each `SolverTask` runs the solver independently
5. Found solutions are:
    - Stored in `SolutionStore`
    - Sent to UI callbacks
6. UI lists solutions
7. User clicks a solution → board updates instantly

---

## Key Design Decisions

### Why `BacktrackingSolver` is Algorithm-Only

- Ensures **Single Responsibility Principle**
- Enables:
    - Sequential solving
    - Parallel solving
    - Future strategies (DFS, heuristics)
- Makes testing and reuse trivial

---

## Code Quality Principles Applied

- SOLID principles
- Clean Code practices
- Clear naming
- No over-engineering
- Thread safety
- Readable documentation and comments

---

## How to Extend

Possible extensions:
- Add heuristic solvers (MRV, symmetry pruning)
- Export solutions to file
- Visual comparison of sequential vs parallel performance
- Add unit tests for solver layer

---

## Technologies Used

- Java 21+
- JavaFX
- ExecutorService
- Atomic primitives
- MVC-inspired architecture

---

## Final Notes

This project demonstrates:

- Correct use of concurrency
- Clean separation of concerns
- Professional-level architecture
- Production-ready structure

It is suitable for:
- Academic submission
- Code review
- Demonstrating parallel programming concepts

---

**Author:**  
Ahmed Nagah

**Project Type:**  
Parallel Algorithms / JavaFX / Clean Architecture
