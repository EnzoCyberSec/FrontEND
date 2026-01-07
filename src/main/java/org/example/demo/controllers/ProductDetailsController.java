package org.example.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.demo.models.Cart;
import org.example.demo.models.Product;

public class ProductDetailsController {

    @FXML private ImageView productImage;
    @FXML private Label productName;
    @FXML private Label productPrice;
    @FXML private Label productDescription;
    @FXML private Label quantityLabel;
    @FXML private RadioButton optionSpicy;

    private Product currentProduct;
    private int quantity = 1;

    // Méthode appelée par le SceneManager pour passer les infos du produit
    public void setProduct(Product product) {
        this.currentProduct = product;

        // Mise à jour de l'affichage
        productName.setText(product.getName());
        productPrice.setText(String.format("%.2f €", product.getPrice()));

        // Gestion de l'image (si url vide ou invalide, garde celle par défaut du FXML)
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                productImage.setImage(new Image(getClass().getResource(product.getImageUrl()).toExternalForm()));
            } catch (Exception e) {
                System.out.println("Image introuvable : " + product.getImageUrl());
            }
        }

        // Description placeholder si vide
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            productDescription.setText(product.getDescription());
        }
    }

    @FXML
    private void increaseQuantity() {
        quantity++;
        updateQuantityLabel();
    }

    @FXML
    private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            updateQuantityLabel();
        }
    }

    private void updateQuantityLabel() {
        quantityLabel.setText(String.valueOf(quantity));
    }

    @FXML
    private void addToCart() {
        if (currentProduct != null) {
            // Gestion de l'option Pimenté
            Product finalProduct = currentProduct;
            if (optionSpicy.isSelected()) {
                // Créer une copie ou modifier le nom pour le panier (Solution simple)
                // Idéalement, on aurait un champ "options" dans CartItem
                finalProduct = new Product(
                        currentProduct.getId(),
                        currentProduct.getName() + " (Pimenté 🌶️)",
                        currentProduct.getDescription(),
                        currentProduct.getPrice(),
                        currentProduct.getImageUrl(),
                        currentProduct.getCategory()
                );
            }

            Cart.getInstance().addItem(finalProduct, quantity);
            System.out.println("Ajouté au panier : " + finalProduct.getName() + " x" + quantity);
        }
        closeWindow();
    }

    @FXML
    private void closeWindow() {
        // Fermer la fenêtre popup
        Stage stage = (Stage) productName.getScene().getWindow();
        stage.close();
    }
}