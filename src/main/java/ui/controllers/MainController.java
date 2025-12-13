package ui.controllers;

import storage.SolutionStore;
import solver.BacktrackingSolver;
import solver.Solver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.animation.BoardAnimator;
import worker.ParallelSolverWorker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * MainController
 * --------------
 * JavaFX controller for the N-Queens application.
 *
 * Responsibilities:
 * - Handle user interactions
 * - Trigger solver execution
 * - Display board and solutions
 * - Coordinate animation and UI updates
 *
 * Important:
 * - NO solving logic here
 * - NO thread management here
 */

public class MainController {

    @FXML private Spinner<Integer> nSpinner;
    @FXML private Button solveBtn;
    @FXML private Button stopBtn;
    @FXML private CheckBox findAllCheckbox;
    @FXML private ToggleButton animateToggle;
    @FXML private StackPane boardContainer;
    @FXML private Label statusLabel;
    @FXML private Label solutionsLabel;
    @FXML private Slider speedSlider;
    @FXML private ListView<String> solutionList;

    private Pane boardGrid; // will contain GridPane
    private StackPane[][] cellGrid; // Fast lookup array
    private int currentN = 8;
    private ParallelSolverWorker parallelSolverWorker;
    private final Solver solver = new BacktrackingSolver();
    private AtomicInteger solutionCount = new AtomicInteger(0);
    private BoardAnimator animator;
    private SolutionStore solutionStore;


    @FXML
    public void initialize() {
        // Spinner for N
        SpinnerValueFactory<Integer> valFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 15, 8);
        nSpinner.setValueFactory(valFactory);

        solveBtn.setOnAction(e -> startSolving());
        stopBtn.setOnAction(e -> stopSolving());

        solutionList.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldVal, newVal) -> {
                    int index = newVal.intValue();
                    if (index >= 0 && index < solutionStore.size()) {
                        render(solutionStore.get(index).getCols());
                    }
                }
        );

        solutionStore = new SolutionStore();
    }

    private void startSolving() {
        currentN = nSpinner.getValue();
        double speed = this.speedSlider.getValue();

        boardContainer.getChildren().clear();
        boardGrid = createBoardGrid(currentN);
        boardContainer.getChildren().add(boardGrid);

        solutionStore.clear();
        solutionList.getItems().clear();

        statusLabel.setText("Solving...");
        solutionCount.set(0);
        solutionsLabel.setText("0");

        solveBtn.setDisable(true);
        stopBtn.setDisable(false);

        boolean findAll = findAllCheckbox.isSelected();
        boolean animate = animateToggle.isSelected();

        // initialize it every time clicking on solve(Solving the empty board bug)
        parallelSolverWorker = new ParallelSolverWorker(solver, solutionStore);

        animator = animate
                ? new BoardAnimator(
                cols -> Platform.runLater(() -> render(cols)),
                (int) speed,
                () -> Platform.runLater(() -> {
                    solveBtn.setDisable(false);
                    stopBtn.setDisable(true);
                    statusLabel.setText("Done");
                })
        )
                : null;

        if (animator != null) animator.start();

        parallelSolverWorker.start(
                currentN,
                findAll,

                // onStep
                cols -> {
                    if (animator != null) {
                        // Pass a clone to avoid concurrent modification issues
                        animator.submit(cols.clone());
                    }
                },

                // onSolution
                solution -> Platform.runLater(() -> {
                    int c = solutionCount.incrementAndGet();
                    solutionsLabel.setText(String.valueOf(c));

                    if (animator == null) render(solution.getCols());
                }),

                // onFinished
                () -> Platform.runLater(() -> {
                    solutionList.getItems().clear();
                    for (int i = 0; i < solutionStore.size(); i++) {
                        solutionList.getItems().add("Solution #" + (i + 1));
                    }
                    statusLabel.setText("Done (Calculation)");
                    if (animator == null || !animator.isRunning()){
                        solveBtn.setDisable(false); // Enable solve button
                        stopBtn.setDisable(true); // Disable stop button
                    }
                }),

                // onError
                ex -> Platform.runLater(() -> {
                    statusLabel.setText("Failed");
                    if (animator != null) animator.stop();
                    solveBtn.setDisable(false);
                    stopBtn.setDisable(true); // Disable stop button
                    ex.printStackTrace();
                })
        );
    }


    private void stopSolving() {
        parallelSolverWorker.cancel();
        statusLabel.setText("Stopped");
        if (animator != null) animator.stop();
        solveBtn.setDisable(false);
        stopBtn.setDisable(true);
    }


    private Pane createBoardGrid(int n) {
        GridPane grid = new GridPane();
        cellGrid = new StackPane[n][n];

        double size = Math.min(600, 600);
        grid.setPrefSize(size, size);

        for (int r = 0; r < n; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / n);
            grid.getRowConstraints().add(rc);
        }

        for (int c = 0; c < n; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / n);
            grid.getColumnConstraints().add(cc);
        }

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                StackPane cell = new StackPane();
                cell.setStyle(((r + c) % 2 == 0) ? "-fx-background-color: whitesmoke; -fx-border-color: gray;" :
                        "-fx-background-color: lightgray; -fx-border-color: gray;");
                cell.setId("cell-"+r+"-"+c);
                grid.add(cell, c, r);
                cellGrid[r][c] = cell;
            }
        }
        return grid;
    }

    // Render board from cols array. cols[row] = col index or -1
    private void render(int[] cols) {
        if (cellGrid == null) return;

        int n = cols.length;

        if (boardGrid == null) return;
        GridPane grid = (GridPane) boardGrid;

        // 1. Clear old queens (efficiently)
        for(int r=0; r<n; r++) {
            for(int c=0; c<n; c++) {
                cellGrid[r][c].getChildren().clear();
            }
        }
        // place queens
        for (int r = 0; r < n; r++) {
            int c = cols[r];
            if (c >= 0 && c < n) {
                Label q = new Label("♛");
                q.setStyle("-fx-font-size: 24px;");
                cellGrid[r][c].getChildren().add(q);
            }
        }
    }
}
