package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;

public class MenuController {

    @FXML private Label title;
    @FXML private TilePane grid;

    @FXML
    public void initialize() {
        showMenus();
    }

    @FXML public void showHome() { title.setText("Home"); grid.getChildren().clear(); }
    @FXML public void showMenus() { title.setText("Menus"); grid.getChildren().clear(); }
    @FXML public void showEntree() { title.setText("Entrée"); grid.getChildren().clear(); }
    @FXML public void showPlats() { title.setText("Plats"); grid.getChildren().clear(); }
    @FXML public void showDesserts() { title.setText("Desserts"); grid.getChildren().clear(); }
    @FXML public void showPetiteFaim() { title.setText("Petite faim"); grid.getChildren().clear(); }
    @FXML public void showBoissons() { title.setText("Boissons"); grid.getChildren().clear(); }




}
