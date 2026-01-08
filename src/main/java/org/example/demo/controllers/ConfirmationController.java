package org.example.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.demo.managers.SceneManager;

import java.io.IOException;
import java.util.Random;

public class ConfirmationController {

    @FXML private Label orderNumber;
    @FXML private Label estimatedTime;

    @FXML
    public void initialize() {
        // Simulation d'un numéro de commande pour l'affichage immédiat
        // (Idéalement, on passerait l'ID réel reçu de l'API via le SceneManager)
        int fakeId = 1000 + new Random().nextInt(9000);
        setOrderData(fakeId, "15-20 minutes");
    }

    /**
     * Permet de mettre à jour les infos depuis l'extérieur (ex: CartController)
     */
    public void setOrderData(int id, String time) {
        if (orderNumber != null) {
            orderNumber.setText("Numéro de commande : #" + id);
        }
        if (estimatedTime != null) {
            estimatedTime.setText("Temps estimé : " + time);
        }
    }

    @FXML
    public void goHome() throws IOException {
        // Retour au menu principal (Accueil)
        SceneManager.getInstance().switchScene("accueil");
    }

    @FXML
    public void newOrder() throws IOException {
        // Retour à l'écran de démarrage (Reset complet pour un nouveau client)
        SceneManager.getInstance().switchScene("hello-view");
    }
}