package storage;

import model.Solution;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SolutionStore {

    private final List<Solution> solutions = new CopyOnWriteArrayList<>();

    public void add(Solution solution) {
        solutions.add(solution);
    }

    public void clear() {
        solutions.clear();
    }

    public List<Solution> getAll() {
        return List.copyOf(solutions);
    }

    public Solution get(int index) {
        return solutions.get(index);
    }

    public int size() {
        return solutions.size();
    }
}
