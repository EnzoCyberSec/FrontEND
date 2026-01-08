package org.example.demo;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.demo.managers.SceneManager;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialiser SceneManager avec le stage AVANT de charger la première scène
        SceneManager.getInstance().initialize(stage);

        // Charger hello-view.fxml via SceneManager
        SceneManager.getInstance().loadInitialScene("hello-view");

        stage.setTitle("Wok & Roll");
        stage.setFullScreen(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
