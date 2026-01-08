package org.example.demo.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public class DessertController {

    @FXML private Label totalLabel;
    @FXML private TilePane grid;

    // DTO pour mapper la réponse JSON /categories/4/plats
    public static class CategorieDto {
        public int idCategorie;
        public String nom;
    }

    public static class DessertDto {
        public int idPlat;
        public String nom;
        public String description;
        public double prix;
        public boolean disponible;
        public CategorieDto categorie;
    }

    @FXML
    public void initialize() {
        loadDessertsFromApi();
        updateTotal();
    }

    private void updateTotal() {
        double total = Cart.getInstance().getTotal();
        if (totalLabel != null) {
            totalLabel.setText(String.format("Total: %.2f €", total));
        }
    }

    /**
     * Appel à l’API pour récupérer les desserts et remplir le TilePane grid.
     */
    private void loadDessertsFromApi() {
        if (grid == null) {
            System.err.println("TilePane 'grid' non injecté (fx:id manquant ou erreur FXML ?)");
            return;
        }

        grid.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            // 4 = Desserts dans ta table categorie
            URL url = new URL("http://localhost:7001/categories/4/plats");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("Erreur API /categories/4/plats : HTTP " + status);
                return;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                Gson gson = new GsonBuilder().create();
                DessertDto[] desserts = gson.fromJson(reader, DessertDto[].class);

                if (desserts == null || desserts.length == 0) {
                    System.out.println("Aucun dessert retourné par l'API.");
                    return;
                }

                for (DessertDto d : desserts) {
                    if (!d.disponible) continue; // si tu gères la dispo
                    StackPane card = createDessertCard(d);
                    grid.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private StackPane createDessertCard(DessertDto d) {
        StackPane root = new StackPane();

        Button button = new Button();
        button.getStyleClass().add("menuCardBtn");

        VBox card = new VBox();
        card.getStyleClass().add("productCard");

        // Image du dessert (par défaut ton logo)
        String imagePath = "/org/example/demo/images/logo.jpg";
        Image image = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(imagePath)
        ));
        ImageView imageView = new ImageView(image);
        imageView.getStyleClass().add("productImg");
        imageView.setFitHeight(90);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(d.nom != null ? d.nom : "Dessert");
        nameLabel.getStyleClass().add("productName");

        Label priceLabel = new Label(String.format("%.2f €", d.prix));
        priceLabel.getStyleClass().add("productPrice");

        card.getChildren().addAll(imageView, nameLabel, priceLabel);
        button.setGraphic(card);

        button.setOnAction(e -> handleSelectDessert(d));

        root.getChildren().add(button);
        return root;
    }

    private void handleSelectDessert(DessertDto d) {
        String imageUrl = "/org/example/demo/images/logo.jpg";
        String description = (d.description != null && !d.description.isBlank())
                ? d.description
                : "Une touche sucrée.";

        Product product = new Product(
                d.idPlat,
                d.nom,
                description,
                d.prix,
                imageUrl,
                "Dessert"
        );

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
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
}
