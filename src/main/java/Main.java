import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/main.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("N-Queens Visualizer");
        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}