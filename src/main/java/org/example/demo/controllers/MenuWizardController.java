package org.example.demo.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.demo.models.Cart;
import org.example.demo.models.Product;

import java.util.ArrayList;
import java.util.List;

public class MenuWizardController {

    @FXML private Label stepTitle;
    @FXML private Label stepProgress;
    @FXML private TilePane optionsGrid;

    // Configuration du menu en cours
    private String menuName;
    private double menuBasePrice;
    private List<String> steps; // Liste des étapes (ex: ["Plat", "Boisson", "Dessert"])

    // État actuel
    private int currentStepIndex = 0;
    private List<Product> selectedItems = new ArrayList<>(); // Ce que le client a choisi

    /**
     * Initialise le Wizard avec le type de menu choisi.
     */
    public void startWizard(String menuType) {
        this.menuName = menuType;
        this.steps = new ArrayList<>();
        this.selectedItems.clear();
        this.currentStepIndex = 0;

        // Configuration des étapes selon le menu
        switch (menuType) {
            case "Menu Duo": // Plat + Boisson
                this.menuBasePrice = 12.50;
                steps.add("Plat");
                steps.add("Boisson");
                break;
            case "Menu Trio": // Plat + Boisson + Dessert
                this.menuBasePrice = 15.00;
                steps.add("Plat");
                steps.add("Boisson");
                steps.add("Dessert");
                break;
            case "Menu Maxi": // Plat + Boisson + Dessert + Snack
                this.menuBasePrice = 18.50;
                steps.add("Plat");
                steps.add("Boisson");
                steps.add("Dessert");
                steps.add("Snack");
                break;
        }

        loadStep();
    }

    private void loadStep() {
        optionsGrid.getChildren().clear();
        String currentCategory = steps.get(currentStepIndex);

        // Mise à jour des titres
        stepTitle.setText("Choisissez votre : " + currentCategory);
        stepProgress.setText("Étape " + (currentStepIndex + 1) + " / " + steps.size());

        // Récupérer les produits (Hardcodés pour l'instant)
        List<Product> options = getProductsForCategory(currentCategory);

        // Créer les boutons pour chaque option
        for (Product p : options) {
            Button btn = createOptionButton(p);
            optionsGrid.getChildren().add(btn);
        }
    }

    private Button createOptionButton(Product p) {
        Button btn = new Button();
        btn.setPrefSize(200, 150);
        btn.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); -fx-cursor: hand;");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        // Label Nom
        Label nameLbl = new Label(p.getName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-wrap-text: true; -fx-text-alignment: center;");

        // (On ajoutera l'image ici plus tard)

        vbox.getChildren().add(nameLbl);
        btn.setGraphic(vbox);

        // Action au clic : On choisit ce produit
        btn.setOnAction(e -> selectItem(p));

        return btn;
    }

    private void selectItem(Product p) {
        selectedItems.add(p);

        // Passer à l'étape suivante ou finir
        if (currentStepIndex < steps.size() - 1) {
            currentStepIndex++;
            loadStep();
        } else {
            finalizeMenu();
        }
    }

    private void finalizeMenu() {
        // Créer un string qui liste le contenu (ex: "Big Mac, Coca, Cookie")
        StringBuilder description = new StringBuilder("Contient : ");
        for (Product p : selectedItems) {
            description.append(p.getName()).append(", ");
        }

        // Créer le produit final "Menu X"
        Product finalMenu = new Product(
                999, // ID fictif
                menuName,
                description.toString(),
                menuBasePrice,
                "/org/example/demo/images/logo.jpg",
                "Menu"
        );

        // Ajouter au panier
        Cart.getInstance().addItem(finalMenu, 1);
        System.out.println("Menu ajouté : " + menuName + " avec " + selectedItems.size() + " éléments.");

        closeWindow();
    }

    @FXML
    public void cancelMenu() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) stepTitle.getScene().getWindow();
        stage.close();
    }

    // --- DONNÉES HARDCODÉES (Simule la DB) ---
    private List<Product> getProductsForCategory(String category) {
        List<Product> list = new ArrayList<>();

        if (category.equals("Plat")) {
            list.add(new Product(1, "Big Burger", "", 0, "", "Plat"));
            list.add(new Product(2, "Cheeseburger", "", 0, "", "Plat"));
            list.add(new Product(3, "Chicken Burger", "", 0, "", "Plat"));
            list.add(new Product(4, "Veggie Burger", "", 0, "", "Plat"));
        }
        else if (category.equals("Boisson")) {
            list.add(new Product(10, "Coca Cola", "", 0, "", "Boisson"));
            list.add(new Product(11, "Fanta", "", 0, "", "Boisson"));
            list.add(new Product(12, "Eau Minérale", "", 0, "", "Boisson"));
            list.add(new Product(13, "Jus d'Orange", "", 0, "", "Boisson"));
        }
        else if (category.equals("Dessert")) {
            list.add(new Product(20, "Glace Vanille", "", 0, "", "Dessert"));
            list.add(new Product(21, "Muffin Choco", "", 0, "", "Dessert"));
            list.add(new Product(22, "Cookie", "", 0, "", "Dessert"));
            list.add(new Product(23, "Fruits", "", 0, "", "Dessert"));
        }
        else if (category.equals("Snack")) {
            list.add(new Product(30, "Frites Moyennes", "", 0, "", "Snack"));
            list.add(new Product(31, "Potatoes", "", 0, "", "Snack"));
            list.add(new Product(32, "Nuggets x4", "", 0, "", "Snack"));
            list.add(new Product(33, "Salade Side", "", 0, "", "Snack"));
        }

        return list;
    }
}