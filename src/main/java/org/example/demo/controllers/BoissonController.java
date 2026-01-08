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

public class BoissonController {

    @FXML private Label totalLabel;
    @FXML private TilePane grid;

    // DTO pour mapper la réponse JSON /categories/{idBoissons}/plats
    public static class CategorieDto {
        public int idCategorie;
        public String nom;
    }

    public static class BoissonDto {
        public int idPlat;
        public String nom;
        public String description;
        public double prix;
        public boolean disponible;
        public CategorieDto categorie;
    }

    @FXML
    public void initialize() {
        loadBoissonsFromApi();
        updateTotal();
    }

    private void updateTotal() {
        double total = Cart.getInstance().getTotal();
        if (totalLabel != null) {
            totalLabel.setText(String.format("Total: %.2f €", total));
        }
    }

    /**
     * Appel à l’API pour récupérer les boissons et remplir le TilePane grid.
     */
    private void loadBoissonsFromApi() {
        if (grid == null) {
            System.err.println("TilePane 'grid' non injecté (fx:id manquant ou erreur FXML ?)");
            return;
        }

        // On vide le TilePane pour ne garder que les résultats de l’API
        grid.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            // 🡺 adapte l’ID de catégorie à ta config (ex: 5 pour Boissons)
            URL url = new URL("http://localhost:7001/categories/3/plats");

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("Erreur API /categories/5/plats : HTTP " + status);
                return;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                Gson gson = new GsonBuilder().create();
                BoissonDto[] boissons = gson.fromJson(reader, BoissonDto[].class);

                if (boissons == null || boissons.length == 0) {
                    System.out.println("Aucune boisson reçue depuis l’API.");
                    return;
                }

                for (BoissonDto boisson : boissons) {
                    grid.getChildren().add(createBoissonCard(boisson));
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel API /categories/3/plats (Boissons)");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Crée une carte (StackPane) pour une boisson donnée.
     * Structure : Button -> VBox(productCard) -> ImageView + Label nom + Label prix
     */
    private StackPane createBoissonCard(BoissonDto boisson) {
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
        Label nameLbl = new Label(boisson.nom != null ? boisson.nom : "Boisson");
        nameLbl.getStyleClass().add("productName");

        // Prix
        double prix = boisson.prix;
        Label priceLbl = new Label(String.format("%.2f €", prix));
        priceLbl.getStyleClass().add("productPrice");

        vbox.getChildren().addAll(imgView, nameLbl, priceLbl);

        btn.setGraphic(vbox);
        btn.setOnAction(this::onSelectMenu);  // même handler que tes cartes statiques

        root.getChildren().add(btn);
        return root;
    }

    // --- Sélection d’une boisson (ouvre la popup détails) ---
    @FXML
    public void onSelectMenu(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String name = "Boisson";
        double price = 0.0;
        String imageUrl = "/org/example/demo/images/logo.jpg";

        try {
            if (clickedButton.getGraphic() instanceof VBox vbox) {
                if (vbox.getChildren().size() >= 3) {
                    if (vbox.getChildren().get(1) instanceof Label nameLabel) {
                        name = nameLabel.getText();
                    }
                    if (vbox.getChildren().get(2) instanceof Label priceLabel) {
                        String priceText = priceLabel.getText()
                                .replace(" €", "")
                                .replace(",", ".")
                                .trim();
                        price = Double.parseDouble(priceText);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Product product = new Product(
                400 + (int) (Math.random() * 100),
                name,
                "Rafraîchissant.",
                price,
                imageUrl,
                "Boisson"
        );
        SceneManager.getInstance().showProductDetails(product);
        updateTotal();
    }

    // --- Navigation ---
    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("hello-view"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }
    @FXML public void goToMenus() throws IOException { SceneManager.getInstance().switchScene("menu"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
}
