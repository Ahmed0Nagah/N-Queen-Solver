package solver;

import model.Solution;

import java.util.*;
import java.util.function.Consumer;

/**
 * BacktrackingSolver
 * -------------------
 * Pure backtracking implementation of the N-Queens problem.
 *
 * Responsibilities:
 * - Generate N-Queens solutions using classic backtracking
 * - Optionally report intermediate steps (onStep)
 * - Report found solutions (onSolution)
 *
 * Important design notes:
 * - This class is THREAD-AGNOSTIC (no threads created here)
 * - This class is UI-AGNOSTIC
 * - Thread interruption is respected for early termination
 */
public class BacktrackingSolver implements Solver {

    /**
     * Solve N-Queens starting from an empty board.
     *
     * @param n number of queens / board size
     * @param onStep callback for each intermediate board state (can be null)
     * @param onSolution callback for each found solution (can be null)
     * @param findAll whether to search for all solutions or stop after first
     * @return list of found solutions
     */
    @Override
    public List<Solution> solve(
            int n,
            Consumer<int[]> onStep,
            Consumer<Solution> onSolution,
            boolean findAll
    ) {
        List<Solution> results = new ArrayList<>();

        // cols[row] = column index of queen, or -1 if empty
        int[] cols = new int[n];
        Arrays.fill(cols, -1);

        // Constraint tracking arrays
        boolean[] usedCols = new boolean[n];
        boolean[] diag1 = new boolean[2 * n]; // row + col
        boolean[] diag2 = new boolean[2 * n]; // row - col + n

        try {
            backtrack(
                    0,
                    n,
                    cols,
                    usedCols,
                    diag1,
                    diag2,
                    onStep,
                    onSolution,
                    results,
                    findAll
            );
        } catch (InterruptedException e) {
            // Interruption is expected in parallel mode
            Thread.currentThread().interrupt();
        }

        return results;
    }

    /**
     * Solve N-Queens starting from a partially-filled board.
     * Used for parallel solving (each task fixes first row differently).
     *
     * @param n board size
     * @param onStep step callback
     * @param onSolution solution callback
     * @param findAll whether to find all solutions
     * @param initialCols pre-filled column placements
     * @param startRow row index to start backtracking from
     */
    @Override
    public List<Solution> solve(
            int n,
            Consumer<int[]> onStep,
            Consumer<Solution> onSolution,
            boolean findAll,
            int[] initialCols,
            int startRow
    ) {
        List<Solution> results = new ArrayList<>();

        boolean[] usedCols = new boolean[n];
        boolean[] diag1 = new boolean[2 * n];
        boolean[] diag2 = new boolean[2 * n];

        // Initialize constraints from pre-filled rows
        for (int r = 0; r < startRow; r++) {
            int c = initialCols[r];
            usedCols[c] = true;
            diag1[r + c] = true;
            diag2[r - c + n] = true;
        }

        try {
            backtrack(
                    startRow,
                    n,
                    initialCols,
                    usedCols,
                    diag1,
                    diag2,
                    onStep,
                    onSolution,
                    results,
                    findAll
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return results;
    }

    /**
     * Core recursive backtracking algorithm.
     *
     * @throws InterruptedException when thread interruption is detected
     */
    private void backtrack(
            int row,
            int n,
            int[] cols,
            boolean[] usedCols,
            boolean[] diag1,
            boolean[] diag2,
            Consumer<int[]> onStep,
            Consumer<Solution> onSolution,
            List<Solution> results,
            boolean findAll
    ) throws InterruptedException {

        // Early termination when running in parallel and interrupted
        if (!findAll && Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        // Base case: all rows filled → valid solution
        if (row == n) {
            Solution solution = new Solution(Arrays.copyOf(cols, n));
            results.add(solution);

            if (onSolution != null) {
                onSolution.accept(solution);
            }
            return;
        }

        // Try placing queen in each column of current row
        for (int c = 0; c < n; c++) {
            if (usedCols[c] || diag1[row + c] || diag2[row - c + n]) {
                continue;
            }

            // Place queen
            cols[row] = c;
            usedCols[c] = diag1[row + c] = diag2[row - c + n] = true;

            // Notify UI / animator of intermediate step
            if (onStep != null) {
                onStep.accept(Arrays.copyOf(cols, n));
            }

            backtrack(
                    row + 1,
                    n,
                    cols,
                    usedCols,
                    diag1,
                    diag2,
                    onStep,
                    onSolution,
                    results,
                    findAll
            );

            // Stop early if only one solution is required
            if (!findAll && !results.isEmpty()) {
                return;
            }

            // Backtrack (remove queen)
            cols[row] = -1;
            usedCols[c] = diag1[row + c] = diag2[row - c + n] = false;
        }
    }
}
