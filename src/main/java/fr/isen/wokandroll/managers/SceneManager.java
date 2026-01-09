package fr.isen.wokandroll.managers;

import fr.isen.wokandroll.controllers.ProductDetailsController;
import fr.isen.wokandroll.models.Product;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class SceneManager {
    private static SceneManager instance;
    private Stage stage;

    // Constantes de configuration
    private static final double SCENE_WIDTH = 1920;
    private static final double SCENE_HEIGHT = 1080;
    private static final String VIEWS_PATH = "/fr/isen/wokandroll/views/";
    private static final String CSS_PATH = "/fr/isen/wokandroll/styles/menu.css";

    private SceneManager() {}

    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void initialize(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("Wok & Roll");
        // Optionnel : this.stage.setResizable(false);
    }

    // ========================
    //      NAVIGATION
    // ========================

    /**
     * Charge la scène initiale (crée une nouvelle Scene).
     */
    public void loadInitialScene(String viewName) {
        try {
            Parent root = loadRoot(viewName);
            Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
            addCss(scene);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            handleError("Erreur lors du chargement initial (" + viewName + ")", e);
        }
    }

    /**
     * Remplace le contenu de la scène actuelle (garde la fenêtre et les dimensions).
     */
    public void switchScene(String viewName) {
        try {
            Parent root = loadRoot(viewName);
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            handleError("Erreur lors du changement de scène vers : " + viewName, e);
        }
    }

    /**
     * Affiche le popup de détails produit (Modal & Transparent).
     */
    public void showProductDetails(Product product) {
        try {
            // 1. Charger le FXML et récupérer le contrôleur
            FXMLLoader loader = getLoader("product-details");
            Parent root = loader.load();

            ProductDetailsController controller = loader.getController();
            controller.setProduct(product);

            // 2. Configurer le Stage du popup
            Stage popupStage = createTransparentModalStage(root);
            popupStage.showAndWait();

        } catch (IOException e) {
            handleError("Impossible d'ouvrir le détail du produit", e);
        }
    }

    // ========================
    //      OUTILS PRIVÉS
    // ========================

    /**
     * Stage modal et transparent pour les popups.
     */
    private Stage createTransparentModalStage(Parent root) {
        Stage popupStage = new Stage();
        popupStage.initOwner(stage);
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.initStyle(StageStyle.TRANSPARENT);

        Scene scene = new Scene(root);
        scene.setFill(null);
        popupStage.setScene(scene);

        return popupStage;
    }

    /**
     * Récupère un FXMLLoader configuré avec le bon chemin.
     */
    private FXMLLoader getLoader(String viewName) throws IOException {
        String path = VIEWS_PATH + viewName + ".fxml";
        URL resource = getClass().getResource(path);
        if (resource == null) throw new IOException("Fichier FXML introuvable : " + path);
        return new FXMLLoader(resource);
    }

    /**
     * Raccourci pour charger directement la racine (Parent).
     */
    private Parent loadRoot(String viewName) throws IOException {
        return getLoader(viewName).load();
    }

    private void addCss(Scene scene) {
        URL resource = getClass().getResource(CSS_PATH);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        } else {
            System.err.println("CSS introuvable : " + CSS_PATH);
        }
    }

    private void handleError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }

    public Stage getStage() {
        return stage;
    }
}