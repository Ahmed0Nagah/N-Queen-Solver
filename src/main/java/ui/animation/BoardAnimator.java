package ui.animation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

public class BoardAnimator {

    private final Queue<int[]> frames = new ArrayDeque<>();
    private final Timeline timeline;

    public BoardAnimator(Consumer<int[]> renderer, int delayMs, Runnable onEmpty) {
        timeline = new Timeline(new KeyFrame(
                Duration.millis(delayMs),
                e -> {
                    int[] next = frames.poll();
                    if (next != null) {
                        renderer.accept(next);
                    } else {
                        stop();
                        if (onEmpty != null) onEmpty.run();
                    }
                }
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }


    public void submit(int[] cols) {
        frames.offer(cols);
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
        frames.clear();
    }

    public boolean isRunning() {
        return timeline.getStatus() == Timeline.Status.RUNNING;
    }

}
