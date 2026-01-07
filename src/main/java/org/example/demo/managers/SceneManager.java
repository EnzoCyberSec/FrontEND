package org.example.demo.managers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneManager {
    private static SceneManager instance;
    private Stage stage;

    // Taille de la fenêtre (Full HD par défaut)
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

    /**
     * Charge la toute première scène (au lancement de l'app).
     * Configure la fenêtre et ajoute le CSS global.
     */
    public void loadInitialScene(String sceneName) throws IOException {
        Parent root = loadFXML(sceneName);

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);

        // Ajout du CSS global (menu.css contient maintenant tout le style propre)
        addCss(scene, "/org/example/demo/styles/menu.css");
        // Si tu as encore des styles génériques dans common.css, décommente la ligne suivante :
        // addCss(scene, "/org/example/demo/styles/common.css");

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Change complètement la scène affichée.
     * C'est la méthode "propre" qui remplace toute la racine de la fenêtre.
     */
    public void switchScene(String sceneName) throws IOException {
        Parent root = loadFXML(sceneName);
        // On remplace la racine actuelle par la nouvelle page
        // Cela résout les problèmes de superposition ou de barres fantômes
        stage.getScene().setRoot(root);
    }

    /**
     * Méthode utilitaire pour charger un fichier FXML.
     */
    private Parent loadFXML(String sceneName) throws IOException {
        String fxmlPath = "/org/example/demo/views/" + sceneName + ".fxml";
        URL resource = getClass().getResource(fxmlPath);

        if (resource == null) {
            throw new IOException("Impossible de trouver le fichier FXML : " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        return loader.load();
    }

    /**
     * Méthode utilitaire pour ajouter un CSS en sécurité.
     */
    private void addCss(Scene scene, String cssPath) {
        URL resource = getClass().getResource(cssPath);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        } else {
            System.err.println("Attention : Fichier CSS introuvable -> " + cssPath);
        }
    }

    public Stage getStage() {
        return stage;
    }
}