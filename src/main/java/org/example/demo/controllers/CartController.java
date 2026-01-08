package org.example.demo.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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
import org.example.demo.models.Option; // Import ajouté pour les options
import org.example.demo.services.OrderApiService;

import java.io.IOException;
import java.io.InputStream;

public class CartController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label totalLabel;

    @FXML
    public void initialize() {
        refreshCartDisplay();
    }

    // =============================================================
    // DISPLAY METHODS
    // =============================================================

    private void refreshCartDisplay() {
        if (cartItemsContainer == null) return;

        cartItemsContainer.getChildren().clear();
        Cart cart = Cart.getInstance();

        if (cart.getItems().isEmpty()) {
            Label emptyLabel = new Label("Your cart is empty ☹");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #888;");
            cartItemsContainer.getChildren().add(emptyLabel);
            if (totalLabel != null) totalLabel.setText("0.00 €");
        } else {
            for (int i = 0; i < cart.getItems().size(); i++) {
                CartItem item = cart.getItems().get(i);
                HBox card = createCartItemCard(item, i);
                cartItemsContainer.getChildren().add(card);
            }
            if (totalLabel != null) {
                totalLabel.setText(String.format("%.2f €", cart.getTotal()));
            }
        }
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

        // --- 1. Product image ---
        String imagePath = item.getProduct().getImageUrl();
        if (imagePath == null || imagePath.isEmpty()) {
            imagePath = "/org/example/demo/images/logo.jpg";
        }

        ImageView imgView = squareImage(imagePath, 60);

        // --- 2. Product information ---
        VBox infoBox = new VBox(5);

        Label nameLabel = new Label(item.getProduct().getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: black;");

        Label priceLabel = new Label(
                String.format("%.2f € / unit", item.getProduct().getPrice())
        );
        priceLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        infoBox.getChildren().addAll(nameLabel, priceLabel);

        // --- Options ---
        if (item.getOptions() != null && !item.getOptions().isEmpty()) {
            for (Option opt : item.getOptions()) {
                Label optLabel = new Label(" + " + opt.getLibelle());
                optLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 11px; -fx-font-style: italic;");
                infoBox.getChildren().add(optLabel);
            }
        }

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- 3. Quantity Controls (MODIFIÉ) ---
        HBox qtyBox = new HBox(8);
        qtyBox.setAlignment(Pos.CENTER);

        // Bouton Moins
        Button minusBtn = new Button("-");
        minusBtn.setStyle("-fx-min-width: 25px; -fx-background-radius: 5;");
        minusBtn.setOnAction(e -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                refreshCartDisplay(); // Rafraîchit l'affichage et le total
            }
        });

        // Label Quantité
        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black; -fx-min-width: 20px; -fx-alignment: center;");

        // Bouton Plus
        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-min-width: 25px; -fx-background-radius: 5;");
        plusBtn.setOnAction(e -> {
            if (item.getQuantity() < 9) { // Limite max à 9
                item.setQuantity(item.getQuantity() + 1);
                refreshCartDisplay(); // Rafraîchit l'affichage et le total
            }
        });

        qtyBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

        // --- 4. Subtotal ---
        Label subTotalLabel = new Label(String.format("%.2f €", item.getSubtotal()));
        subTotalLabel.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #e46725;"
        );
        subTotalLabel.setMinWidth(80);
        subTotalLabel.setAlignment(Pos.CENTER_RIGHT);

        // --- 5. Delete button ---
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

        // Ajout de qtyBox à la place de l'ancien label qty
        card.getChildren().addAll(
                imgView, infoBox, spacer, qtyBox, subTotalLabel, deleteBtn
        );
        return card;
    }

    private ImageView squareImage(String resourcePath, double size) {
        ImageView iv = new ImageView();
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);

        Image img = null;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) img = new Image(is);
        } catch (Exception ignored) {}

        if (img == null) {
            try (InputStream isLogo = getClass().getResourceAsStream(
                    "/org/example/demo/images/logo.jpg")) {
                if (isLogo != null) img = new Image(isLogo);
            } catch (Exception ignored) {}
        }

        if (img != null) {
            iv.setImage(img);
            double w = img.getWidth();
            double h = img.getHeight();
            double side = Math.min(w, h);
            iv.setViewport(new Rectangle2D(
                    (w - side) / 2, (h - side) / 2, side, side
            ));
        }
        return iv;
    }

    // =============================================================
    // NAVIGATION & CHECKOUT
    // =============================================================

    @FXML
    public void goBack() throws IOException {
        SceneManager.getInstance().switchScene("accueil");
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
            showInfoAlert(
                    "Empty cart",
                    "Your cart is empty. You cannot place an order."
            );
            return;
        }

        try {
            int orderId = OrderApiService.createCommandeWithLinesFromCart(cart);

            cart.clear();

            ConfirmationController.lastOrderId = orderId;

            SceneManager.getInstance().switchScene("confirmation");

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert(
                    "Technical error",
                    "Unable to validate the order.\n\nDetails: " + e.getMessage()
            );
        }
    }

    // ==========================
    // Alerts
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