package org.example.demo.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class ConfirmationController {

    @FXML
    private BorderPane root;

    @FXML
    public void goHome() throws IOException {
        loadPage("/org/example/demo/views/menu.fxml");
    }

    @FXML
    public void newOrder() throws IOException {
        loadPage("/org/example/demo/views/menu.fxml");
    }

    private void loadPage(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent content = loader.load();

        BorderPane primaryRoot = (BorderPane) root.getScene().getRoot();
        primaryRoot.setCenter(content);
    }
}
