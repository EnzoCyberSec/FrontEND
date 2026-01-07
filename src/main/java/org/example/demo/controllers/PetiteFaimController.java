package org.example.demo.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.demo.managers.SceneManager;
import org.example.demo.models.Cart;
import org.example.demo.models.Product;
import java.io.IOException;

public class PetiteFaimController {

    @FXML private Label totalLabel;

    @FXML public void initialize() { updateTotal(); }

    private void updateTotal() {
        double total = Cart.getInstance().getTotal();
        if (totalLabel != null) totalLabel.setText(String.format("Total: %.2f €", total));
    }

    @FXML public void onSelectMenu(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String name = "Snack";
        double price = 0.0;
        String imageUrl = "/org/example/demo/images/logo.jpg";
        try {
            if (clickedButton.getGraphic() instanceof VBox) {
                VBox vbox = (VBox) clickedButton.getGraphic();
                if (vbox.getChildren().size() >= 3) {
                    if (vbox.getChildren().get(1) instanceof Label) name = ((Label) vbox.getChildren().get(1)).getText();
                    if (vbox.getChildren().get(2) instanceof Label) {
                        String priceText = ((Label) vbox.getChildren().get(2)).getText().replace(" €", "").replace(",", ".").trim();
                        price = Double.parseDouble(priceText);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        Product product = new Product(500 + (int)(Math.random()*100), name, "Parfait pour une petite faim.", price, imageUrl, "Snack");
        SceneManager.getInstance().showProductDetails(product);
        updateTotal();
    }

    // Navigation
    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("hello-view"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }
    @FXML public void goToMenus() throws IOException { SceneManager.getInstance().switchScene("menu"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToSnacks() throws IOException { SceneManager.getInstance().switchScene("petite-faim"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
}