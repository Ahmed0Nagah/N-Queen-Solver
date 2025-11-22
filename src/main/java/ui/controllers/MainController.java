package ui.controllers;

import model.Solution;
import solver.BacktrackingSolver;
import solver.Solver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController {

    @FXML private Spinner<Integer> nSpinner;
    @FXML private Button solveBtn;
    @FXML private Button stopBtn;
    @FXML private CheckBox findAllCheckbox;
    @FXML private Slider speedSlider;
    @FXML private ToggleButton animateToggle;
    @FXML private StackPane boardContainer;
    @FXML private Label statusLabel;
    @FXML private Label solutionsLabel;
    @FXML private Button exportBtn;

    private Pane boardGrid; // will contain GridPane
    private int currentN = 8;
    private Task<Void> solveTask;
    private Solver solver = new BacktrackingSolver();
    private AtomicInteger solutionCount = new AtomicInteger(0);

    @FXML
    public void initialize() {
        // Spinner for N
        SpinnerValueFactory<Integer> valFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 20, 8);
        nSpinner.setValueFactory(valFactory);

        solveBtn.setOnAction(e -> startSolving());
        stopBtn.setOnAction(e -> stopSolving());
        exportBtn.setOnAction(e -> exportSolutions());
    }

    private void startSolving() {
        currentN = nSpinner.getValue();
        boardContainer.getChildren().clear();
        boardGrid = createBoardGrid(currentN);
        boardContainer.getChildren().add(boardGrid);
        statusLabel.setText("Solving...");
        solutionCount.set(0);
        solutionsLabel.setText("0");
        solveBtn.setDisable(true);
        stopBtn.setDisable(false);
        exportBtn.setDisable(true);

        boolean findAll = findAllCheckbox.isSelected();
        boolean animate = animateToggle.isSelected();
        double speed = speedSlider.getValue(); // milliseconds delay between steps

        solveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    solver.solve(currentN,
                            cols -> {
                                // onStep callback — update UI
                                if (isCancelled()) return;
                                if (animate) {
                                    // show intermediate step with delay
                                    Platform.runLater(() -> render(cols));
                                    try { Thread.sleep(Math.max(1, (long) speed)); } catch (InterruptedException ex) { cancel(); }
                                } else {
                                    // if not animating, only show final states or on solution
                                    Platform.runLater(() -> render(cols));
                                }
                            },
                            solution -> {
                                // onSolution callback
                                int c = solutionCount.incrementAndGet();
                                Platform.runLater(() -> {
                                    solutionsLabel.setText(String.valueOf(c));
                                    // optionally highlight final solution
                                    render(solution.getCols());
                                });
                            },
                            findAll
                    );
                } catch (InterruptedException ex) {
                    // task cancelled
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    statusLabel.setText("Done");
                    solveBtn.setDisable(false);
                    stopBtn.setDisable(true);
                    exportBtn.setDisable(false);
                });
            }

            @Override
            protected void cancelled() {
                Platform.runLater(() -> {
                    statusLabel.setText("Stopped");
                    solveBtn.setDisable(false);
                    stopBtn.setDisable(true);
                    exportBtn.setDisable(solutionCount.get()==0);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    statusLabel.setText("Failed");
                    solveBtn.setDisable(false);
                    stopBtn.setDisable(true);
                });
            }
        };

        Thread th = new Thread(solveTask, "solver-thread");
        th.setDaemon(true);
        th.start();
    }

    private void stopSolving() {
        if (solveTask != null && !solveTask.isCancelled()) {
            solveTask.cancel(true);
        }
    }

    private Pane createBoardGrid(int n) {
        GridPane grid = new GridPane();
        double size = Math.min(600, 600); // heat map size; or dynamic
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
            }
        }
        return grid;
    }

    // Render board from cols array. cols[row] = col index or -1
    private void render(int[] cols) {
        int n = cols.length;
        if (boardGrid == null) return;
        GridPane grid = (GridPane) boardGrid;
        // clear all queens
        grid.getChildren().forEach(node -> {
            if (node instanceof StackPane) {
                StackPane cell = (StackPane) node;
                cell.getChildren().clear();
            }
        });
        // place queens
        for (int r = 0; r < n; r++) {
            int c = cols[r];
            if (c >= 0) {
                int finalR = r;
                StackPane cell = (StackPane) grid.getChildren().stream()
                        .filter(n2 -> GridPane.getRowIndex(n2) == finalR && GridPane.getColumnIndex(n2) == c)
                        .map(n2 -> (StackPane) n2).findFirst().orElse(null);
                if (cell != null) {
                    Label q = new Label("♛"); // chess queen unicode
                    q.setStyle("-fx-font-size: 24px;");
                    cell.getChildren().add(q);
                }
            }
        }
    }

    private void exportSolutions() {
        // You can implement export to text file using FileChooser and write solutions.
        statusLabel.setText("Export not implemented in this sample.");
    }
}
