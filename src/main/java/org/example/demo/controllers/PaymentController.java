package org.example.demo.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class PaymentController {

    @FXML
    private BorderPane root;

    @FXML
    public void goBack() throws IOException {
        loadPage("/org/example/demo/views/cart.fxml");
    }

    @FXML
    public void payByCreditCard() throws IOException {
        loadPage("/org/example/demo/views/confirmation.fxml");
    }

    @FXML
    public void payCash() throws IOException {
        loadPage("/org/example/demo/views/confirmation.fxml");
    }

    @FXML
    public void payQRCode() throws IOException {
        loadPage("/org/example/demo/views/confirmation.fxml");
    }

    private void loadPage(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent content = loader.load();

        BorderPane primaryRoot = (BorderPane) root.getScene().getRoot();
        primaryRoot.setCenter(content);
    }
}
