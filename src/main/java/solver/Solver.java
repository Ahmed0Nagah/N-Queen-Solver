package solver;

import model.Solution;

import java.util.List;
import java.util.function.Consumer;

public interface Solver {
    /**
     * Solve the N-Queens problem.
     * @param n board size
     * @param onStep called whenever a step/state update happens (may be used for visualization)
     * @param onSolution called when a full solution is found
     * @param findAll if true, search all solutions; otherwise stop after first.
     * @return list of solutions found (may be large)
     */
    List<Solution> solve(int n,
                         Consumer<int[]> onStep,
                         Consumer<Solution> onSolution,
                         boolean findAll) throws InterruptedException;
}
