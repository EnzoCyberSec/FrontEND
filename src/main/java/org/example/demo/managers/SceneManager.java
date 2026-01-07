package org.example.demo.managers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static SceneManager instance;
    private Stage stage;
    private Scene mainScene;
    private BorderPane mainRoot;
    private static final double SCENE_WIDTH = 1920;
    private static final double SCENE_HEIGHT = 1080;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void initialize(Stage stage) {
        this.stage = stage;
    }

    public void loadInitialScene(String sceneName) throws IOException {
        // Charger le premier fichier FXML et créer la Scene une seule fois
        String fxmlPath = "/org/example/demo/views/" + sceneName + ".fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

        if (loader.getLocation() == null) {
            throw new IOException("Cannot load FXML: " + fxmlPath);
        }

        Parent root = loader.load();
        mainScene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);

        // Ajouter les CSS
        try {
            mainScene.getStylesheets().add(
                    getClass().getResource("/org/example/demo/styles/menu.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        try {
            mainScene.getStylesheets().add(
                    getClass().getResource("/org/example/demo/styles/common.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        stage.setScene(mainScene);
        mainRoot = (BorderPane) root;
    }

    public void switchScene(String sceneName) throws IOException {
        // Charger uniquement le contenu FXML
        String fxmlPath = "/org/example/demo/views/" + sceneName + ".fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

        if (loader.getLocation() == null) {
            throw new IOException("Cannot load FXML: " + fxmlPath);
        }

        Parent content = loader.load();

        // Remplacer le center du BorderPane principal
        mainRoot.setCenter(content);
    }

    public Stage getStage() {
        return stage;
    }

    public BorderPane getMainRoot() {
        return mainRoot;
    }
}
