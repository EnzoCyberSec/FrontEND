package org.example.demo.managers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.demo.controllers.MenuWizardController;
import org.example.demo.controllers.ProductDetailsController;
import org.example.demo.models.Product;

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
     * Charge la toute première scène.
     */
    public void loadInitialScene(String sceneName) throws IOException {
        Parent root = loadFXML(sceneName);
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        addCss(scene, "/org/example/demo/styles/menu.css");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Change complètement la scène affichée.
     */
    public void switchScene(String sceneName) throws IOException {
        Parent root = loadFXML(sceneName);
        stage.getScene().setRoot(root);
    }

    /**
     * Ouvre la fenêtre POPUP pour un produit unitaire.
     */
    public void showProductDetails(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/views/product-details.fxml"));
            Parent root = loader.load();

            ProductDetailsController controller = loader.getController();
            controller.setProduct(product);

            Stage popupStage = new Stage();
            popupStage.initOwner(stage);
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(null);
            popupStage.setScene(scene);
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur Popup Produit : " + e.getMessage());
        }
    }

    /**
     * Ouvre le WIZARD pour composer un menu.
     */
    public void showMenuWizard(String menuType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/views/menu-wizard.fxml"));
            Parent root = loader.load();

            MenuWizardController controller = loader.getController();
            controller.startWizard(menuType);

            Stage popupStage = new Stage();
            popupStage.initOwner(stage);
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(null);
            popupStage.setScene(scene);
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur Wizard Menu : " + e.getMessage());
        }
    }

    private Parent loadFXML(String sceneName) throws IOException {
        String fxmlPath = "/org/example/demo/views/" + sceneName + ".fxml";
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) throw new IOException("FXML introuvable : " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(resource);
        return loader.load();
    }

    private void addCss(Scene scene, String cssPath) {
        URL resource = getClass().getResource(cssPath);
        if (resource != null) scene.getStylesheets().add(resource.toExternalForm());
        else System.err.println("CSS introuvable : " + cssPath);
    }

    public Stage getStage() {
        return stage;
    }
}