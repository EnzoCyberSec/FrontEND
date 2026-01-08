package org.example.demo.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.demo.managers.SceneManager;
import org.example.demo.models.Cart;
import org.example.demo.models.CartItem;
import org.example.demo.services.OrderApiService;

import java.io.IOException;

public class CartController {

    @FXML
    private VBox cartItemsContainer;
    @FXML
    private Label totalLabel;

    @FXML
    public void initialize() {
        refreshCartDisplay();
    }

    private void refreshCartDisplay() {
        cartItemsContainer.getChildren().clear();
        Cart cart = Cart.getInstance();

        if (cart.getItems().isEmpty()) {
            Label emptyLabel = new Label("Votre panier est vide ☹");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #888;");
            cartItemsContainer.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < cart.getItems().size(); i++) {
                CartItem item = cart.getItems().get(i);
                int index = i; // Pour le bouton supprimer

                HBox card = createCartItemCard(item, index);
                cartItemsContainer.getChildren().add(card);
            }
        }

        // Mise à jour du total
        totalLabel.setText(String.format("%.2f €", cart.getTotal()));
    }

    private HBox createCartItemCard(CartItem item, int index) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        // Image Produit (Petite)
        ImageView imgView = new ImageView();
        try {
            imgView.setImage(new Image(
                    getClass().getResource("/org/example/demo/images/logo.jpg").toExternalForm()
            ));
        } catch (Exception e) {
            // Ignorer si image non trouvée
        }
        imgView.setFitHeight(60);
        imgView.setFitWidth(60);
        imgView.setPreserveRatio(true);

        // Infos produit
        VBox infoBox = new VBox(5);

        Label nameLabel = new Label(item.getProduct().getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label priceLabel = new Label(String.format("%.2f € / unité", item.getProduct().getPrice()));
        priceLabel.setStyle("-fx-text-fill: #666;");

        infoBox.getChildren().addAll(nameLabel, priceLabel);

        // Spacer pour pousser les éléments à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Quantité
        Label qtyLabel = new Label("x" + item.getQuantity());
        qtyLabel.setStyle("-fx-font-size: 16px;");

        // Sous-total
        Label subTotalLabel = new Label(String.format("%.2f €", item.getSubtotal()));
        subTotalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #e46725;");

        // Bouton Supprimer (supprime du panier côté front uniquement)
        Button deleteBtn = new Button("X");
        deleteBtn.setStyle(
                "-fx-background-color: #ffcccc;" +
                        "-fx-text-fill: #cc0000;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        );
        deleteBtn.setOnAction(e -> {
            Cart.getInstance().removeItem(index);
            refreshCartDisplay();
        });

        card.getChildren().addAll(imgView, infoBox, spacer, qtyLabel, subTotalLabel, deleteBtn);
        return card;
    }

    @FXML
    public void goBack() throws IOException {
        // Retour au menu principal
        SceneManager.getInstance().switchScene("menu");
    }

    @FXML
    public void clearCart() {
        Cart.getInstance().clear();
        refreshCartDisplay();
    }

    @FXML
    public void checkout() {
        Cart cart = Cart.getInstance();

        if (cart.getItems().isEmpty()) {
            showInfoAlert("Panier vide", "Votre panier est vide, impossible de passer une commande.");
            return;
        }

        try {
            // Appel API : création commande + lignes de commande
            int idCommande = OrderApiService.createCommandeWithLinesFromCart(cart);

            // On vide le panier côté front
            cart.clear();
            refreshCartDisplay();

            // Message de confirmation
            showInfoAlert(
                    "Commande validée",
                    "Votre commande n°" + idCommande + " a été enregistrée."
            );

            // Si tu veux ensuite aller sur une page de paiement :
            // SceneManager.getInstance().switchScene("payment");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showErrorAlert(
                    "Erreur",
                    "Impossible de valider la commande : " + e.getMessage()
            );
        }
    }

    // ==========================
    //  Boîtes de dialogue
    // ==========================

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
