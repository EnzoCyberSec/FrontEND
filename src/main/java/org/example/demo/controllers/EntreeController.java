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

public class EntreeController {

    @FXML private Label totalLabel; // N'oublie pas d'ajouter fx:id="totalLabel" dans ton FXML entree.fxml

    @FXML
    public void initialize() {
        updateTotal();
    }

    private void updateTotal() {
        double total = Cart.getInstance().getTotal();
        if (totalLabel != null) {
            totalLabel.setText(String.format("Total: %.2f €", total));
        }
    }

    // --- LOGIQUE D'AJOUT PRODUIT (Celle qui manquait !) ---

    @FXML
    public void onSelectMenu(ActionEvent event) {
        // 1. Récupérer le bouton cliqué
        Button clickedButton = (Button) event.getSource();

        String name = "Produit Inconnu";
        double price = 0.0;
        String imageUrl = "/org/example/demo/images/logo.jpg";

        try {
            // 2. Analyser le bouton (Image, Nom, Prix)
            if (clickedButton.getGraphic() instanceof VBox) {
                VBox vbox = (VBox) clickedButton.getGraphic();
                if (vbox.getChildren().size() >= 3) {
                    // Nom (Index 1)
                    if (vbox.getChildren().get(1) instanceof Label) {
                        name = ((Label) vbox.getChildren().get(1)).getText();
                    }
                    // Prix (Index 2)
                    if (vbox.getChildren().get(2) instanceof Label) {
                        String priceText = ((Label) vbox.getChildren().get(2)).getText()
                                .replace(" €", "").replace(",", ".").trim();
                        price = Double.parseDouble(priceText);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Créer le produit (Catégorie Entrée par défaut ici)
        Product product = new Product(
                (int)(Math.random() * 1000), // ID aléatoire
                name,
                "Une délicieuse entrée pour commencer le repas.",
                price,
                imageUrl,
                "Entree"
        );

        // 4. Ouvrir la POPUP SIMPLE (pas le wizard)
        SceneManager.getInstance().showProductDetails(product);

        // 5. Mettre à jour le total au retour
        updateTotal();
    }

    // --- NAVIGATION (Reste inchangé) ---

    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("hello-view"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }
    @FXML public void goToMenus() throws IOException { SceneManager.getInstance().switchScene("menu"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); } // Recharge page actuelle
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToSnacks() throws IOException { SceneManager.getInstance().switchScene("petite-faim"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
}