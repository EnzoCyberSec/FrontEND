package org.example.demo.controllers;

import javafx.fxml.FXML;
import org.example.demo.managers.SceneManager;

import java.io.IOException;

public class ConfirmationController {

    @FXML
    public void goHome() throws IOException {
        // Home = accueil (ou change si tu veux autre page)
        SceneManager.getInstance().switchScene("accueil");
    }

    @FXML
    public void newOrder() throws IOException {
        // comme tu as demandé : retour vers hello-view
        SceneManager.getInstance().switchScene("hello-view");
    }
}
