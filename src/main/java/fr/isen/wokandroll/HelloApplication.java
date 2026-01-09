package fr.isen.wokandroll;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import fr.isen.wokandroll.managers.SceneManager;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialisation de SceneManager
        SceneManager.getInstance().initialize(stage);

        // Chargement de home.fxml via SceneManager
        SceneManager.getInstance().loadInitialScene("home");

        stage.setTitle("Wok & Roll");
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/fr/isen/wokandroll/images/app.png"))
        );
        stage.setFullScreen(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
