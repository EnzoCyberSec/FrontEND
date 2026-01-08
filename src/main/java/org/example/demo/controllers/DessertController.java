package org.example.demo.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
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
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

public class DessertController {

    @FXML private Label totalLabel;
    @FXML private TilePane grid;

    // DTO
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
     * Même méthode que EntreeController:
     * plat.nom nettoyé -> NomDeFichier.png (Majuscule 1ère lettre)
     *
     * Dossier:
     *  src/main/resources/org/example/demo/images/desserts/
     *
     * Fallback:
     *  default.png
     */
    private String imageForDessert(DessertDto d) {
        String basePath = "/org/example/demo/images/desserts/";

        if (d == null || d.nom == null || d.nom.isBlank()) {
            return basePath + "default.png";
        }

        // 1) lower
        String s = d.nom.toLowerCase(Locale.ROOT);

        // 2) retire accents
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        // 3) garde lettres + espaces uniquement, vire ponctuation/chiffres
        s = s.replaceAll("[^a-z ]", " ");

        // 4) enlève espaces
        s = s.replaceAll("\\s+", "").trim();

        if (s.isEmpty()) {
            return basePath + "default.png";
        }

        // 5) Majuscule 1ère lettre
        String fileName = Character.toUpperCase(s.charAt(0)) + s.substring(1);

        // 6) chemin final
        String imagePath = basePath + fileName + ".png";

        // 7) vérifie existence
        if (getClass().getResource(imagePath) != null) {
            return imagePath;
        }

        return basePath + "default.png";
    }

    /**
     * Même méthode que EntreeController:
     * carré sans déformation + crop centré.
     */
    private ImageView buildImageViewSquareCropped(String resourcePath, double size) {
        ImageView imgView = new ImageView();
        imgView.getStyleClass().add("productImg");
        imgView.setFitWidth(size);
        imgView.setFitHeight(size);
        imgView.setPreserveRatio(true);

        Image img = null;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            img = new Image(Objects.requireNonNull(is));
        } catch (Exception e) {
            // fallback
            try (InputStream is2 = getClass().getResourceAsStream("/org/example/demo/images/logo.jpg")) {
                if (is2 != null) img = new Image(is2);
            } catch (Exception ignored) {}
        }

        if (img != null) {
            imgView.setImage(img);

            double w = img.getWidth();
            double h = img.getHeight();
            double side = Math.min(w, h);
            double x = (w - side) / 2.0;
            double y = (h - side) / 2.0;

            imgView.setViewport(new Rectangle2D(x, y, side, side));
        }

        return imgView;
    }

    /**
     * Appel API: desserts = categorie 4
     */
    private void loadDessertsFromApi() {
        if (grid == null) {
            System.err.println("TilePane 'grid' non injecté (fx:id manquant ou erreur FXML ?)");
            return;
        }

        grid.getChildren().clear();

        HttpURLConnection conn = null;
        try {
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
                    if (!d.disponible) continue;
                    grid.getChildren().add(createDessertCard(d));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private StackPane createDessertCard(DessertDto d) {
        StackPane root = new StackPane();

        Button btn = new Button();
        btn.getStyleClass().add("menuCardBtn");

        VBox card = new VBox();
        card.getStyleClass().add("productCard");

        // Image auto: nom -> fichier
        String imagePath = imageForDessert(d);
        ImageView imgView = buildImageViewSquareCropped(imagePath, 100);

        Label nameLabel = new Label(d.nom != null ? d.nom : "Dessert");
        nameLabel.getStyleClass().add("productName");

        Label priceLabel = new Label(String.format("%.2f €", d.prix));
        priceLabel.getStyleClass().add("productPrice");

        card.getChildren().addAll(imgView, nameLabel, priceLabel);
        btn.setGraphic(card);

        // stocke l'image pour le popup (comme Entree)
        btn.setUserData(imagePath);

        // même logique que Entree: on réutilise le handler
        btn.setOnAction(e -> handleSelectDessert(btn, d));

        root.getChildren().add(btn);
        return root;
    }

    private void handleSelectDessert(Button clickedButton, DessertDto d) {
        String name = (d.nom != null) ? d.nom : "Dessert";
        double price = d.prix;

        // récupère l’image associée à la card
        String imageUrl = "/org/example/demo/images/desserts/default.png";
        if (clickedButton.getUserData() instanceof String) {
            imageUrl = (String) clickedButton.getUserData();
        }

        String description = (d.description != null && !d.description.isBlank())
                ? d.description
                : "Une touche sucrée.";

        Product product = new Product(
                d.idPlat,
                name,
                description,
                price,
                imageUrl,
                "Dessert"
        );

        SceneManager.getInstance().showProductDetails(product);
        updateTotal();
    }

    // Navigation
    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("hello-view"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }
    @FXML public void goToAccueil() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
}
