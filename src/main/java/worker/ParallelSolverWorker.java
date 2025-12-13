package worker;

import model.Solution;
import storage.SolutionStore;
import solver.Solver;
import task.SolverTask;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * ParallelSolverWorker
 * --------------------
 * Manages parallel execution of SolverTasks using a thread pool.
 *
 * Responsibilities:
 * - Split work across CPU cores
 * - Coordinate early termination
 * - Collect solutions in SolutionStore
 * - Notify UI through callbacks
 */
public class ParallelSolverWorker {

    private final Solver solver;
    private final SolutionStore solutionStore;

    private ExecutorService executor;
    private final AtomicBoolean solutionFound = new AtomicBoolean(false);
    private volatile boolean cancelled = false;

    public ParallelSolverWorker(Solver solver, SolutionStore store) {
        this.solver = solver;
        this.solutionStore = store;
    }

    /**
     * Starts parallel solving.
     */
    public void start(
            int n,
            boolean findAll,
            Consumer<int[]> onStep,
            Consumer<Solution> onSolution,
            Runnable onFinished,
            Consumer<Exception> onError
    ) {
        int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);

        List<Future<List<Solution>>> futures = new ArrayList<>();

        // Store solution and forward to UI
        Consumer<Solution> wrappedOnSolution = solution -> {
            solutionStore.add(solution);
            onSolution.accept(solution);
        };

        try {
            for (int col = 0; col < n; col++) {
                SolverTask task = new SolverTask(
                        solver,
                        n,
                        col,
                        findAll,
                        onStep,
                        wrappedOnSolution,
                        solutionFound,
                        this::stopAll
                );
                futures.add(executor.submit(task));
            }

            executor.shutdown();

            // Wait for all tasks in background thread
            new Thread(() -> {
                try {
                    for (Future<List<Solution>> future : futures) {
                        if (cancelled) break;
                        future.get();
                    }
                    onFinished.run();
                } catch (Exception ex) {
                    onError.accept(ex);
                }
            }).start();

        } catch (Exception ex) {
            onError.accept(ex);
        }
    }

    /**
     * Cancel execution manually.
     */
    public void cancel() {
        cancelled = true;
        stopAll();
    }

    /**
     * Interrupt all running tasks.
     */
    private void stopAll() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
