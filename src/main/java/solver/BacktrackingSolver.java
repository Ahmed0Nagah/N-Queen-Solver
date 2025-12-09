package solver;

import model.Solution;
import java.util.*;
import java.util.function.Consumer;

public class BacktrackingSolver implements Solver {

    @Override
    public List<Solution> solve(int n, Consumer<int[]> onStep, Consumer<Solution> onSolution, boolean findAll) throws InterruptedException {
        // 1. Thread-safe list to hold results from multiple threads
        List<Solution> results = Collections.synchronizedList(new ArrayList<>());

        // List to keep track of the threads we spawn
        List<Thread> threads = new ArrayList<>();

        // 2. The "Literal" Loop: Iterate through every column in the FIRST ROW (Row 0)
        for (int startCol = 0; startCol < n; startCol++) {

            final int c = startCol; // Final variable for use inside lambda

            // Create a new thread for this specific starting position
            Thread worker = new Thread(() -> {
                try {
                    // 3. Initialize Independent State for this Thread
                    int[] cols = new int[n];
                    Arrays.fill(cols, -1);
                    boolean[] usedCols = new boolean[n];
                    boolean[] diag1 = new boolean[2 * n];
                    boolean[] diag2 = new boolean[2 * n];

                    // Place the Queen for this thread's assigned column in Row 0
                    cols[0] = c;
                    usedCols[c] = true;
                    diag1[0 + c] = true;
                    diag2[0 - c + n] = true;

                    // Notify GUI of initial placement (optional, might be chaotic with N threads)
                    if (onStep != null) onStep.accept(Arrays.copyOf(cols, n));

                    // 4. Start backtracking from ROW 1 (since Row 0 is set)
                    backtrack(1, n, cols, usedCols, diag1, diag2, onStep, onSolution, results, findAll);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            threads.add(worker);
            worker.start();
        }

        // 5. Main thread waits for all workers to finish
        for (Thread t : threads) {
            t.join();
        }

        return new ArrayList<>(results); // Return a clean ArrayList
    }

    // This method remains largely the same, but 'results' must be the synchronized list passed down
    private void backtrack(int row, int n, int[] cols, boolean[] usedCols, boolean[] diag1, boolean[] diag2,
                           Consumer<int[]> onStep, Consumer<Solution> onSolution, List<Solution> results, boolean findAll) throws InterruptedException {

        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

        if (row == n) {
            // Found solution
            int[] sol = Arrays.copyOf(cols, n);
            Solution s = new Solution(sol);

            // This add is safe because 'results' is a SynchronizedList
            results.add(s);

            if (onSolution != null) onSolution.accept(s);
            return;
        }

        for (int c = 0; c < n; c++) {
            if (usedCols[c] || diag1[row + c] || diag2[row - c + n]) continue;

            // Place queen
            cols[row] = c;
            usedCols[c] = true;
            diag1[row + c] = true;
            diag2[row - c + n] = true;

            // Notify step (ensure your GUI handler is thread-safe!)
            if (onStep != null) onStep.accept(Arrays.copyOf(cols, n));

            // Recurse
            backtrack(row + 1, n, cols, usedCols, diag1, diag2, onStep, onSolution, results, findAll);

            // Optimization: If we only need 1 solution and we found it, stop exploring
            if (!findAll && !results.isEmpty()) return;

            // Remove queen (Backtrack)
            cols[row] = -1;
            usedCols[c] = false;
            diag1[row + c] = false;
            diag2[row - c + n] = false;

            if (onStep != null) onStep.accept(Arrays.copyOf(cols, n));
        }
    }
}