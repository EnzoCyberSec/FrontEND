package org.example.demo.controllers;

import javafx.fxml.FXML;
import org.example.demo.managers.SceneManager;
import java.io.IOException;

public class EntreeController {

    @FXML
    public void goBack() throws IOException {
        SceneManager.getInstance().switchScene("menu");
    }

    @FXML
    public void goToCart() throws IOException {
        SceneManager.getInstance().switchScene("cart");
    }
}
