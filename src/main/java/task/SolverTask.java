package task;

import model.Solution;
import solver.Solver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * SolverTask
 * ----------
 * Represents a single parallel branch of the N-Queens solver.
 *
 * Each task:
 * - Fixes the queen position in row 0
 * - Runs the solver starting from row 1
 * - Optionally stops all tasks when one solution is found
 */
public class SolverTask implements Callable<List<Solution>> {

    private final Solver solver;
    private final int n;
    private final int firstCol;
    private final boolean findAll;
    private final Consumer<int[]> onStep;
    private final Consumer<Solution> onSolution;
    private final AtomicBoolean solutionFound;
    private final Runnable stopAll;

    /**
     * @param solver solver implementation (pure algorithm)
     * @param n board size
     * @param firstCol fixed column for row 0
     * @param findAll whether to find all solutions
     * @param onStep step callback
     * @param onSolution solution callback
     * @param solutionFound shared flag for early termination
     * @param stopAll callback to stop all running tasks
     */
    public SolverTask(
            Solver solver,
            int n,
            int firstCol,
            boolean findAll,
            Consumer<int[]> onStep,
            Consumer<Solution> onSolution,
            AtomicBoolean solutionFound,
            Runnable stopAll
    ) {
        this.solver = solver;
        this.n = n;
        this.firstCol = firstCol;
        this.findAll = findAll;
        this.onStep = onStep;
        this.onSolution = onSolution;
        this.solutionFound = solutionFound;
        this.stopAll = stopAll;
    }

    @Override
    public List<Solution> call() throws Exception {

        // Skip execution if solution already found
        if (!findAll && solutionFound.get()) {
            return List.of();
        }

        // Wrap solution callback to coordinate early termination
        Consumer<Solution> wrappedOnSolution = solution -> {
            if (!findAll && solutionFound.compareAndSet(false, true)) {
                onSolution.accept(solution);
                stopAll.run(); // interrupt all other tasks
            } else if (findAll) {
                onSolution.accept(solution);
            }
        };

        // Prepare initial board with fixed first row
        int[] cols = new int[n];
        Arrays.fill(cols, -1);
        cols[0] = firstCol;

        return solver.solve(
                n,
                onStep,
                wrappedOnSolution,
                findAll,
                cols,
                1 // start from row 1
        );
    }
}
