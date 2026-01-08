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
import org.example.demo.services.OrderApiService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class CartController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label totalLabel;

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
            totalLabel.setText("0.00 €");
        } else {
            for (int i = 0; i < cart.getItems().size(); i++) {
                CartItem item = cart.getItems().get(i);
                // On passe l'index pour que le bouton supprimer sache quel item viser
                HBox card = createCartItemCard(item, i);
                cartItemsContainer.getChildren().add(card);
            }
            // Mise à jour du total
            totalLabel.setText(String.format("%.2f €", cart.getTotal()));
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

        // --- 1. Image du produit (Logique intelligente) ---
        // On récupère le chemin d'image stocké dans le produit (depuis Accueil/Boissons)
        String imagePath = item.getProduct().getImageUrl();
        // Si pas d'image spécifique, on met le logo par défaut
        if (imagePath == null || imagePath.isEmpty()) {
            imagePath = "/org/example/demo/images/logo.jpg";
        }

        ImageView imgView = squareImage(imagePath, 60);

        // --- 2. Informations (Nom + Prix) ---
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(item.getProduct().getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label priceLabel = new Label(String.format("%.2f € / unité", item.getProduct().getPrice()));
        priceLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        infoBox.getChildren().addAll(nameLabel, priceLabel);

        // Spacer pour pousser le reste à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- 3. Quantité ---
        Label qtyLabel = new Label("x" + item.getQuantity());
        qtyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // --- 4. Sous-total ---
        Label subTotalLabel = new Label(String.format("%.2f €", item.getSubtotal()));
        subTotalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #e46725;");
        subTotalLabel.setMinWidth(80); // Alignement propre
        subTotalLabel.setAlignment(Pos.CENTER_RIGHT);

        // --- 5. Bouton Supprimer ---
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

    // Méthode utilitaire pour afficher une image carrée (Uniformité du design)
    private ImageView squareImage(String resourcePath, double size) {
        ImageView iv = new ImageView();
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);

        Image img = null;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) img = new Image(is);
        } catch (Exception ignored) {}

        // Fallback sur le logo si l'image spécifique échoue
        if (img == null) {
            try (InputStream isLogo = getClass().getResourceAsStream("/org/example/demo/images/logo.jpg")) {
                if (isLogo != null) img = new Image(isLogo);
            } catch (Exception ignored) {}
        }

        if (img != null) {
            iv.setImage(img);
            // Centrage et crop carré
            double w = img.getWidth();
            double h = img.getHeight();
            double side = Math.min(w, h);
            iv.setViewport(new Rectangle2D((w - side) / 2, (h - side) / 2, side, side));
        }
        return iv;
    }

    // ==========================
    //  Actions (Navigation & API)
    // ==========================

    @FXML
    public void goBack() throws IOException {
        // Retour à l'accueil (Navigation JB plus cohérente)
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
            showInfoAlert("Panier vide", "Votre panier est vide, impossible de passer une commande.");
            return;
        }

        try {
            // LOGIQUE ENZO : Appel API Réel
            // Création de la commande + lignes de commande dans la BDD
            int idCommande = OrderApiService.createCommandeWithLinesFromCart(cart);

            // On vide le panier côté front après succès
            cart.clear();
            refreshCartDisplay();

            // Message de succès
            showInfoAlert(
                    "Commande validée !",
                    "Votre commande n°" + idCommande + " a été enregistrée avec succès.\nMerci de votre visite !"
            );

            // Optionnel : rediriger vers l'accueil après le clic sur OK
            SceneManager.getInstance().switchScene("accueil");

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert(
                    "Erreur technique",
                    "Impossible de valider la commande.\n\nDétail : " + e.getMessage()
            );
        }
    }

    // ==========================
    //  Alertes
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