package org.example.demo.controllers;

import javafx.fxml.FXML;
import org.example.demo.managers.SceneManager;
import java.io.IOException;

public class HelloController {

    @FXML
    public void startApp() throws IOException {
        // Lance le menu principal
        SceneManager.getInstance().switchScene("accueil");
    }

    @FXML
    public void login() {
        System.out.println("Fonctionnalité Login à implémenter");
        // Tu pourras rediriger vers une page de login ici plus tard
    }

    @FXML
    public void goToStats() throws IOException {
        // Ouvre la page des statistiques
        SceneManager.getInstance().switchScene("stats");
    }
}