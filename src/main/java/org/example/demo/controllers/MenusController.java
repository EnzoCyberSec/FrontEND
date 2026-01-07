package org.example.demo.controllers;

import javafx.fxml.FXML;
import org.example.demo.managers.SceneManager;
import java.io.IOException;

public class MenusController {

    @FXML
    public void goBack() throws IOException {
        // Retour à l'écran d'accueil "Toucher pour commencer" (hello-view)
        SceneManager.getInstance().switchScene("hello-view");
    }

    @FXML
    public void goToCart() throws IOException {
        // Lien vers la page panier
        SceneManager.getInstance().switchScene("cart");
    }

    // --- Navigation Catégories ---

    @FXML
    public void goToMenus() throws IOException {
        SceneManager.getInstance().switchScene("menu");
    }

    @FXML
    public void goToStarters() throws IOException {
        SceneManager.getInstance().switchScene("entree");
    }

    @FXML
    public void goToMainDishes() throws IOException {
        SceneManager.getInstance().switchScene("plats");
    }

    @FXML
    public void goToDesserts() throws IOException {
        SceneManager.getInstance().switchScene("desserts");
    }

    @FXML
    public void goToSnacks() throws IOException {
        SceneManager.getInstance().switchScene("petite-faim");
    }

    @FXML
    public void goToDrinks() throws IOException {
        SceneManager.getInstance().switchScene("boissons");
    }

    // --- Action lors du clic sur un article ---

    @FXML
    public void onSelectMenu() {
        System.out.println("Article sélectionné !");
        // Ici, tu pourras ajouter la logique pour ajouter l'item au Cart
        // Exemple : Cart.getInstance().addItem(new Product(...), 1);
    }
}