package org.example.demo.controllers;

import javafx.fxml.FXML;
import org.example.demo.managers.SceneManager;
import java.io.IOException;

public class HelloController {

    @FXML
    public void startApp() throws IOException {
        SceneManager.getInstance().switchScene("accueil");
    }

    @FXML
    public void login() {
        System.out.println("Connexion");
    }
}
