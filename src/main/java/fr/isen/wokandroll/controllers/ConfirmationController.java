package fr.isen.wokandroll.controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;
import fr.isen.wokandroll.managers.SceneManager;

import java.io.IOException;
import java.util.Random;

public class ConfirmationController {

    @FXML private Label orderNumber;
    @FXML private Label estimatedTime;

    // Variable utilisée pour transmettre l'identifiant de la commande depuis CartController
    public static int lastOrderId = 0;

    @FXML
    public void initialize() {
        // 1. Gestion de l'affichage du numéro de commande
        int idToDisplay = (lastOrderId != 0)
                ? lastOrderId
                : (1000 + new Random().nextInt(9000));

        // Réinitialisation pour la prochaine commande
        lastOrderId = 0;

        setOrderData(idToDisplay, "15–20 minutes");

        // 2. Démarrage du compte à rebours de 5 secondes
        startAutoRedirect();
    }

    /**
     * Attend 5 secondes puis redirige vers l'écran d'accueil.
     */
    private void startAutoRedirect() {
        // Durée de 5 secondes
        PauseTransition delay = new PauseTransition(Duration.seconds(5));

        // Action exécutée à la fin du délai imparti
        delay.setOnFinished(event -> {
            try {
                // Simule un clic sur « Nouvelle commande » pour revenir à l'accueil
                newOrder();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Lance le timer
        delay.play();
    }

    public void setOrderData(int id, String time) {
        if (orderNumber != null) {
            orderNumber.setText("Order number: #" + id);
        }
        if (estimatedTime != null) {
            estimatedTime.setText("Please wait, your order is being prepared");
        }
    }

    @FXML
    public void newOrder() throws IOException {
        SceneManager.getInstance().switchScene("home");
    }
}
