package solver;

import model.Solution;
import java.util.*;
import java.util.function.Consumer;

public class BacktrackingSolver implements Solver {

    private volatile boolean stop = false;

    @Override
    public List<Solution> solve(int n, Consumer<int[]> onStep, Consumer<Solution> onSolution, boolean findAll) throws InterruptedException {
        stop = false;
        List<Solution> results = new ArrayList<>();
        int[] cols = new int[n];
        Arrays.fill(cols, -1);
        boolean[] usedCols = new boolean[n]; // columns used
        // diag checks: r-c + (n-1) and r+c
        boolean[] diag1 = new boolean[2*n]; // r+c
        boolean[] diag2 = new boolean[2*n]; // r-c + n

        backtrack(0, n, cols, usedCols, diag1, diag2, onStep, onSolution, results, findAll);
        return results;
    }

    private void backtrack(int row, int n, int[] cols, boolean[] usedCols, boolean[] diag1, boolean[] diag2,
                           Consumer<int[]> onStep, Consumer<Solution> onSolution, List<Solution> results, boolean findAll) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        if (row == n) {
            // found solution - copy cols
            int[] sol = Arrays.copyOf(cols, n);
            Solution s = new Solution(sol);
            results.add(s);
            if (onSolution != null) onSolution.accept(s);
            return;
        }
        for (int c = 0; c < n; c++) {
            if (usedCols[c] || diag1[row + c] || diag2[row - c + n]) continue;
            // place queen
            cols[row] = c;
            usedCols[c] = true;
            diag1[row + c] = true;
            diag2[row - c + n] = true;
            if (onStep != null) onStep.accept(Arrays.copyOf(cols, n)); // notify step
            // recurse
            backtrack(row + 1, n, cols, usedCols, diag1, diag2, onStep, onSolution, results, findAll);
            // optionally stop if not finding all
            if (!findAll && !results.isEmpty()) return;
            // backtrack
            cols[row] = -1;
            usedCols[c] = false;
            diag1[row + c] = false;
            diag2[row - c + n] = false;
            if (onStep != null) onStep.accept(Arrays.copyOf(cols, n)); // notify removal
        }
    }
}
