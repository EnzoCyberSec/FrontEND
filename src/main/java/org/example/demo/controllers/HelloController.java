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
        // Placeholder pour une future page de connexion
        System.out.println("Connexion (Fonctionnalité à implémenter)");
    }

    // Ajout de la méthode de JB pour le bouton Stats du FXML
    @FXML
    public void goToStats() throws IOException {
        SceneManager.getInstance().switchScene("stats");
    }
}