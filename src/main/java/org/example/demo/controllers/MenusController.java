package org.example.demo.controllers;

import javafx.fxml.FXML;
import org.example.demo.managers.SceneManager;
import java.io.IOException;

public class MenusController {

    @FXML
    public void goBack() throws IOException {
        SceneManager.getInstance().switchScene("menu");
    }

    @FXML
    public void goToCart() throws IOException {
        SceneManager.getInstance().switchScene("cart");
    }

    @FXML
    public void goToMenus() throws IOException {
        SceneManager.getInstance().switchScene("desserts");
    }

    @FXML
    public void goToStarters() throws IOException {
        SceneManager.getInstance().switchScene("desserts");
    }

    @FXML
    public void goToMainDishes() throws IOException {
        SceneManager.getInstance().switchScene("desserts");
    }

    @FXML
    public void goToDesserts() throws IOException {
        SceneManager.getInstance().switchScene("desserts");
    }

    @FXML
    public void goToSnacks() throws IOException {
        SceneManager.getInstance().switchScene("desserts");
    }

    @FXML
    public void goToDrinks() throws IOException {
        SceneManager.getInstance().switchScene("desserts");
    }
}
