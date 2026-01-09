package fr.isen.wokandroll;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import fr.isen.wokandroll.managers.SceneManager;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialiser SceneManager avec le stage AVANT de charger la première scène
        SceneManager.getInstance().initialize(stage);

        // Charger home.fxml via SceneManager
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
