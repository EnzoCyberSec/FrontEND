package org.example.demo.controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.example.demo.managers.SceneManager;

import java.io.IOException;
import java.util.Random;

public class ConfirmationController {

    @FXML private Label orderNumber;
    @FXML private Label estimatedTime;

    // Variable used to pass the order ID from CartController
    public static int lastOrderId = 0;

    @FXML
    public void initialize() {
        // 1. Order number display handling
        int idToDisplay = (lastOrderId != 0)
                ? lastOrderId
                : (1000 + new Random().nextInt(9000));

        // Reset for the next order
        lastOrderId = 0;

        setOrderData(idToDisplay, "15–20 minutes");

        // 2. Start the 5-second countdown
        startAutoRedirect();
    }

    /**
     * Waits 5 seconds then redirects to the home screen ("Tap to Start").
     */
    private void startAutoRedirect() {
        // 5-second duration
        PauseTransition delay = new PauseTransition(Duration.seconds(5));

        // Action executed when the timer ends
        delay.setOnFinished(event -> {
            try {
                // Simulates a click on "New Order" to return to 'hello-view'
                newOrder();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Start the timer
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
        SceneManager.getInstance().switchScene("hello-view");
    }
}
