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

public class MenusController {

    @FXML
    private Label totalLabel; // Doit correspondre à fx:id="totalLabel" dans ton FXML

    @FXML
    public void initialize() {
        // Met à jour le total affiché dès le chargement de la page
        updateTotal();
    }

    /**
     * Met à jour le label du total en bas de page en récupérant la somme du Panier.
     */
    private void updateTotal() {
        double total = Cart.getInstance().getTotal();
        if (totalLabel != null) {
            totalLabel.setText(String.format("Total: %.2f €", total));
        }
    }

    // --- Navigation ---

    @FXML
    public void goBack() throws IOException {
        SceneManager.getInstance().switchScene("hello-view");
    }

    @FXML
    public void goToCart() throws IOException {
        SceneManager.getInstance().switchScene("cart");
    }

    @FXML
    public void goToMenus() throws IOException {
        SceneManager.getInstance().switchScene("menu");
    }

    @FXML
    public void goToStarters() throws IOException {
        SceneManager.getInstance().switchScene("entree");
    }

    @FXML
    public void goToMainDishes() throws IOException {
        SceneManager.getInstance().switchScene("plats");
    }

    @FXML
    public void goToDesserts() throws IOException {
        SceneManager.getInstance().switchScene("desserts");
    }

    @FXML
    public void goToSnacks() throws IOException {
        SceneManager.getInstance().switchScene("petite-faim");
    }

    @FXML
    public void goToDrinks() throws IOException {
        SceneManager.getInstance().switchScene("boissons");
    }

    // --- NOUVEAU : Logique pour les MENUS COMPOSÉS (Wizard) ---

    @FXML
    public void onSelectMenuDuo() {
        // Lance l'assistant pour un menu Plat + Boisson
        SceneManager.getInstance().showMenuWizard("Menu Duo");
        // Une fois fermé, on met à jour le total immédiatement
        updateTotal();
    }

    @FXML
    public void onSelectMenuTrio() {
        // Lance l'assistant pour un menu Plat + Boisson + Dessert
        SceneManager.getInstance().showMenuWizard("Menu Trio");
        updateTotal();
    }

    @FXML
    public void onSelectMenuMaxi() {
        // Lance l'assistant pour un menu complet (+ Snack)
        SceneManager.getInstance().showMenuWizard("Menu Maxi");
        updateTotal();
    }

    // --- ANCIENNE LOGIQUE : Sélection d'un article unitaire (Générique) ---
    // Utile si tu ajoutes d'autres boutons simples sur cette page ou pour copier vers d'autres contrôleurs
    @FXML
    public void onSelectMenu(ActionEvent event) {
        // 1. Récupérer le bouton cliqué
        Button clickedButton = (Button) event.getSource();

        // Valeurs par défaut
        String name = "Produit Inconnu";
        double price = 0.0;
        String imageUrl = "/org/example/demo/images/logo.jpg"; // Image par défaut

        try {
            // 2. Analyser le contenu graphique du bouton pour trouver le Nom et le Prix
            // Structure FXML : Button -> Graphic (VBox) -> Children [ImageView, Label (Nom), Label (Prix)]
            if (clickedButton.getGraphic() instanceof VBox) {
                VBox vbox = (VBox) clickedButton.getGraphic();

                // On s'attend à trouver au moins 3 éléments : Image (0), Nom (1), Prix (2)
                if (vbox.getChildren().size() >= 3) {

                    // Récupération du Nom (Index 1)
                    if (vbox.getChildren().get(1) instanceof Label) {
                        Label nameLabel = (Label) vbox.getChildren().get(1);
                        name = nameLabel.getText();
                    }

                    // Récupération du Prix (Index 2)
                    if (vbox.getChildren().get(2) instanceof Label) {
                        Label priceLabel = (Label) vbox.getChildren().get(2);
                        // Nettoyage : on enlève " €", on remplace "," par "." et on enlève les espaces
                        String priceText = priceLabel.getText().replace(" €", "").replace(",", ".").trim();
                        try {
                            price = Double.parseDouble(priceText);
                        } catch (NumberFormatException e) {
                            System.err.println("Erreur lecture prix : " + priceText);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Impossible de lire les infos du bouton. Utilisation valeurs défaut.");
            e.printStackTrace();
        }

        // 3. Création de l'objet Produit
        Product product = new Product(
                1, // ID temporaire
                name,
                "Délicieux " + name + " préparé avec soin. Ingrédients frais et de qualité.",
                price,
                imageUrl,
                "Menu"
        );

        // 4. Ouvrir la fenêtre POPUP simple (pas le wizard)
        SceneManager.getInstance().showProductDetails(product);

        // 5. Une fois la fenêtre fermée, on met à jour le total
        updateTotal();
    }
}