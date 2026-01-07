package org.example.demo.controllers;

import javafx.fxml.FXML;
import org.example.demo.managers.SceneManager;
import java.io.IOException;

public class EntreeController {

    @FXML
    public void goBack() throws IOException {
        // Retour vers l'accueil ou le menu principal
        SceneManager.getInstance().switchScene("menu");
    }

    @FXML
    public void goToCart() throws IOException {
        SceneManager.getInstance().switchScene("cart");
    }

    // --- Navigation Barre Latérale (Même logique que MenusController) ---

    @FXML
    public void goToMenus() throws IOException {
        SceneManager.getInstance().switchScene("menu");
    }

    @FXML
    public void goToStarters() throws IOException {
        // Déjà sur la page Entrées, on recharge ou on ne fait rien
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
}