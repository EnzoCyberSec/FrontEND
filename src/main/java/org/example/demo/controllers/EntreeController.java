package org.example.demo.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.event.ActionEvent;
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

public class EntreeController {

    @FXML private Label totalLabel;
    @FXML private TilePane grid;

    // --- DTOs ---
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

    // --- Chemins Images ---
    private static final String BASE_PATH = "/org/example/demo/images/entrees/";
    private static final String DEFAULT_IMG = BASE_PATH + "default.png";

    @FXML
    public void initialize() {
        loadEntreesFromApi();
        updateTotal();
    }

    private void updateTotal() {
        double total = Cart.getInstance().getTotal();
        if (totalLabel != null) {
            totalLabel.setText(String.format("Total: %.2f €", total));
        }
    }

    // =================================================================================
    // LOGIQUE IMAGE (Apport de JB)
    // =================================================================================

    /**
     * Transforme "Chicken gyoza" -> "Chickengyoza.png" et vérifie si le fichier existe.
     */
    private String imageForPlat(PlatDto plat) {
        if (plat == null || plat.nom == null || plat.nom.isBlank()) {
            return DEFAULT_IMG;
        }

        // 1. Minuscules
        String s = plat.nom.toLowerCase(Locale.ROOT);
        // 2. Retire les accents
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        // 3. Garde lettres + espaces
        s = s.replaceAll("[^a-z ]", " ");
        // 4. Enlève les espaces
        s = s.replaceAll("\\s+", "").trim();

        if (s.isEmpty()) return DEFAULT_IMG;

        // 5. Première lettre majuscule (Convention JB)
        String fileName = Character.toUpperCase(s.charAt(0)) + s.substring(1);
        String imagePath = BASE_PATH + fileName + ".png";

        // 6. Vérification existence
        if (getClass().getResource(imagePath) != null) {
            return imagePath;
        }
        return DEFAULT_IMG;
    }

    /**
     * Crée une image carrée et centrée (crop).
     */
    private ImageView squareImage(String resourcePath, double size) {
        ImageView imgView = new ImageView();
        imgView.getStyleClass().add("productImg");
        imgView.setFitWidth(size);
        imgView.setFitHeight(size);
        imgView.setPreserveRatio(true);

        Image img = null;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            img = new Image(Objects.requireNonNull(is));
        } catch (Exception e) {
            // Fallback logo
            try (InputStream is2 = getClass().getResourceAsStream("/org/example/demo/images/logo.jpg")) {
                if (is2 != null) img = new Image(is2);
            } catch (Exception ignored) {}
        }

        if (img != null) {
            imgView.setImage(img);
            double w = img.getWidth();
            double h = img.getHeight();
            double side = Math.min(w, h);
            // Crop centré
            imgView.setViewport(new Rectangle2D((w - side) / 2.0, (h - side) / 2.0, side, side));
        }

        return imgView;
    }

    // =================================================================================
    // CHARGEMENT API (Enzo)
    // =================================================================================

    private void loadEntreesFromApi() {
        if (grid == null) return;
        grid.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            // Catégorie 1 = Entrées
            URL url = new URL("http://localhost:7001/categories/1/plats");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.err.println("Erreur API : HTTP " + conn.getResponseCode());
                return;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                Gson gson = new GsonBuilder().create();
                PlatDto[] plats = gson.fromJson(reader, PlatDto[].class);

                if (plats != null) {
                    for (PlatDto plat : plats) {
                        if (plat.disponible) {
                            grid.getChildren().add(createEntreeCard(plat));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // =================================================================================
    // CRÉATION UI
    // =================================================================================

    private StackPane createEntreeCard(PlatDto plat) {
        StackPane root = new StackPane();

        Button btn = new Button();
        btn.getStyleClass().add("menuCardBtn");

        VBox vbox = new VBox();
        vbox.getStyleClass().add("productCard");

        // Image intelligente
        String imagePath = imageForPlat(plat);
        ImageView imgView = squareImage(imagePath, 100);

        // Nom
        Label nameLbl = new Label(plat.nom != null ? plat.nom : "Entrée");
        nameLbl.getStyleClass().add("productName");

        // Prix
        Label priceLbl = new Label(String.format("%.2f €", plat.prix));
        priceLbl.getStyleClass().add("productPrice");

        vbox.getChildren().addAll(imgView, nameLbl, priceLbl);
        btn.setGraphic(vbox);

        // Stockage des données (Image path + Objet DTO complet)
        btn.setUserData(new Object[]{imagePath, plat});
        btn.setOnAction(this::onSelectMenu);

        root.getChildren().add(btn);
        return root;
    }

    // =================================================================================
    // NAVIGATION & CLIKS
    // =================================================================================

    @FXML
    public void onSelectMenu(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        String imagePath = DEFAULT_IMG;
        PlatDto plat = null;

        try {
            Object[] data = (Object[]) clickedButton.getUserData();
            imagePath = (String) data[0];
            plat = (PlatDto) data[1];
        } catch (Exception ignored) {}

        String name = (plat != null && plat.nom != null) ? plat.nom : "Entrée";
        double price = (plat != null) ? plat.prix : 0.0;

        // Utilisation de la vraie description BDD
        String description = (plat != null && plat.description != null && !plat.description.isBlank())
                ? plat.description
                : "Une entrée savoureuse pour bien commencer.";

        int id = (plat != null) ? plat.idPlat : 0;

        Product product = new Product(
                id,
                name,
                description,
                price,
                imagePath,
                "Entrée"
        );

        SceneManager.getInstance().showProductDetails(product);
        updateTotal();
    }

    // Navigation standard
    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("hello-view"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }
    @FXML public void goToAccueil() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
}