package org.example.demo.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.example.demo.managers.SceneManager;
import org.example.demo.models.Cart;
import org.example.demo.models.Product;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AccueilController {

    @FXML private Label totalLabel;
    @FXML private TilePane grid;

    // --- DTOs pour l'API ---

    private static class CategorieDto {
        int idCategorie;
        String nom;
    }

    private static class PlatDto {
        int idPlat;
        String nom;
        String description;
        double prix;
        boolean disponible;
        CategorieDto categorie;
    }

    @FXML
    public void initialize() {
        loadRandomProductsFromApi();   // charge des produits random
        updateTotal();
    }

    private void updateTotal() {
        double total = Cart.getInstance().getTotal();
        if (totalLabel != null) {
            totalLabel.setText(String.format("Total: %.2f €", total));
        }
    }

    // --- Chargement de produits random ---

    private void loadRandomProductsFromApi() {
        if (grid == null) {
            System.err.println("TilePane 'grid' non injecté (fx:id manquant dans le FXML ?)");
            return;
        }

        grid.getChildren().clear();

        // 1 = Entrées, 2 = Plats, 3 = Boissons, 4 = Desserts
        PlatDto entree  = fetchRandomPlatFromCategory(1);
        PlatDto plat    = fetchRandomPlatFromCategory(2);
        PlatDto boisson = fetchRandomPlatFromCategory(3);
        PlatDto dessert = fetchRandomPlatFromCategory(4);

        if (entree  != null)  grid.getChildren().add(createProductCard(entree));
        if (plat    != null)  grid.getChildren().add(createProductCard(plat));
        if (boisson != null)  grid.getChildren().add(createProductCard(boisson));
        if (dessert != null)  grid.getChildren().add(createProductCard(dessert));
    }

    /**
     * Appelle /categories/{idCategorie}/plats, puis retourne un plat aléatoire.
     */
    private PlatDto fetchRandomPlatFromCategory(int idCategorie) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/categories/" + idCategorie + "/plats");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("Erreur API /categories/" + idCategorie + "/plats : HTTP " + status);
                return null;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                Gson gson = new GsonBuilder().create();
                PlatDto[] plats = gson.fromJson(reader, PlatDto[].class);

                if (plats == null || plats.length == 0) {
                    System.out.println("Aucun produit dans la catégorie " + idCategorie);
                    return null;
                }

                // Retourne un plat aléatoire dans cette catégorie
                int index = (int) (Math.random() * plats.length);
                return plats[index];
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel API /categories/" + idCategorie + "/plats :");
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Crée une carte (StackPane) pour un produit (image + nom + prix).
     */
    private StackPane createProductCard(PlatDto plat) {
        StackPane root = new StackPane();

        Button btn = new Button();
        btn.getStyleClass().add("menuCardBtn");

        VBox vbox = new VBox();
        vbox.getStyleClass().add("productCard");

        // Image
        ImageView imgView;
        try (InputStream imgStream = getClass()
                .getResourceAsStream("/org/example/demo/images/logo.jpg")) {
            Image img = new Image(Objects.requireNonNull(imgStream));
            imgView = new ImageView(img);
        } catch (Exception e) {
            imgView = new ImageView();
        }
        imgView.getStyleClass().add("productImg");
        imgView.setFitHeight(90);
        imgView.setFitWidth(120);
        imgView.setPreserveRatio(true);

        // Nom
        String nom = (plat.nom != null) ? plat.nom : "Produit";
        Label nameLbl = new Label(nom);
        nameLbl.getStyleClass().add("productName");

        // Prix
        double prix = plat.prix;
        Label priceLbl = new Label(String.format("%.2f €", prix));
        priceLbl.getStyleClass().add("productPrice");

        vbox.getChildren().addAll(imgView, nameLbl, priceLbl);

        btn.setUserData(plat);
        btn.setGraphic(vbox);

        // Clic = ouvrir la popup détail
        btn.setOnAction(this::onSelectMenu);

        root.getChildren().add(btn);
        return root;
    }

    // --- Clic sur une carte => popup ---

    @FXML
    public void onSelectMenu(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        PlatDto plat = (PlatDto) clickedButton.getUserData();

        String imageUrl = "/org/example/demo/images/logo.jpg";

        String name = (plat != null && plat.nom != null) ? plat.nom : "Produit";
        double price = (plat != null) ? plat.prix : 0.0;

        String description;
        if (plat != null && plat.description != null && !plat.description.isBlank()) {
            description = plat.description;
        } else {
            description = "Un délicieux " + name + " préparé avec soin.";
        }

        String categoryLabel = "Produit";
        if (plat != null && plat.categorie != null) {
            if (plat.categorie.idCategorie == 1) categoryLabel = "Entrée";
            else if (plat.categorie.idCategorie == 2) categoryLabel = "Plat";
            else if (plat.categorie.idCategorie == 3) categoryLabel = "Boisson";
            else if (plat.categorie.idCategorie == 4) categoryLabel = "Dessert";
        }

        int id = (plat != null) ? plat.idPlat : 0;

        Product product = new Product(
                id,
                name,
                description,
                price,
                imageUrl,
                categoryLabel
        );

        SceneManager.getInstance().showProductDetails(product);
        updateTotal();
    }

    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("hello-view"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }
    @FXML public void goToAccueil() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
}
