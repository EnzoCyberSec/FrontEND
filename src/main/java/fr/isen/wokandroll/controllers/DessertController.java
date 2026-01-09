package fr.isen.wokandroll.controllers;

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
import fr.isen.wokandroll.managers.SceneManager;
import fr.isen.wokandroll.models.Cart;
import fr.isen.wokandroll.models.Product;

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

    // --- DTOs ---
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

    // --- Chemins ---
    private static final String BASE_PATH = "/fr/isen/wokandroll/images/desserts/";
    private static final String DEFAULT_IMG = BASE_PATH + "default.png";

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

    // =================================================================================
    // LOGIQUE IMAGE
    // =================================================================================

    /**
     * Transforme le nom du dessert en nom de fichier.
     * Exemple : "Coconut pearls" -> "coconut pearls" -> "coconutpearls" -> "Coconutpearls.png"
     */
    private String imageForDessert(DessertDto d) {
        if (d == null || d.nom == null || d.nom.isBlank()) {
            return DEFAULT_IMG;
        }

        // 1. Minuscules
        String s = d.nom.toLowerCase(Locale.ROOT);
        // 2. Retire les accents
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        // 3. Garde uniquement les lettres
        s = s.replaceAll("[^a-z]", "");

        if (s.isEmpty()) return DEFAULT_IMG;

        // 4. Première lettre majuscule
        String fileName = Character.toUpperCase(s.charAt(0)) + s.substring(1);
        String finalPath = BASE_PATH + fileName + ".png";

        // 5. Vérifie si le fichier existe
        if (getClass().getResource(finalPath) != null) {
            return finalPath;
        }

        return DEFAULT_IMG;
    }

    /**
     * Crée une ImageView carrée et centrée (crop).
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
            // Fallback sur le logo si l'image par défaut manque aussi
            try (InputStream is2 = getClass().getResourceAsStream("/fr/isen/wokandroll/images/logo.jpg")) {
                if (is2 != null) img = new Image(is2);
            } catch (Exception ignored) {}
        }

        if (img != null) {
            imgView.setImage(img);
            double w = img.getWidth();
            double h = img.getHeight();
            double side = Math.min(w, h);
            // Centrage
            imgView.setViewport(new Rectangle2D((w - side) / 2.0, (h - side) / 2.0, side, side));
        }

        return imgView;
    }

    // =================================================================================
    // CHARGEMENT API
    // =================================================================================

    private void loadDessertsFromApi() {
        if (grid == null) return;
        grid.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/categories/4/plats");
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
                DessertDto[] desserts = gson.fromJson(reader, DessertDto[].class);

                if (desserts != null) {
                    for (DessertDto d : desserts) {
                        if (d.disponible) {
                            grid.getChildren().add(createDessertCard(d));
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

    private StackPane createDessertCard(DessertDto d) {
        StackPane root = new StackPane();

        Button btn = new Button();
        btn.getStyleClass().add("menuCardBtn");

        VBox card = new VBox();
        card.getStyleClass().add("productCard");

        // Image intelligente
        String imagePath = imageForDessert(d);
        ImageView imgView = squareImage(imagePath, 100);

        Label nameLabel = new Label(d.nom != null ? d.nom : "Dessert");
        nameLabel.getStyleClass().add("productName");

        Label priceLabel = new Label(String.format("%.2f €", d.prix));
        priceLabel.getStyleClass().add("productPrice");

        card.getChildren().addAll(imgView, nameLabel, priceLabel);
        btn.setGraphic(card);

        // On stocke les données pour le clic
        btn.setUserData(new Object[]{imagePath, d});
        btn.setOnAction(this::onSelectMenu);

        root.getChildren().add(btn);
        return root;
    }

    // =================================================================================
    // NAVIGATION & CLIKS
    // =================================================================================

    @FXML
    public void onSelectMenu(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        String imagePath = DEFAULT_IMG;
        DessertDto d = null;

        try {
            Object[] data = (Object[]) clickedButton.getUserData();
            imagePath = (String) data[0];
            d = (DessertDto) data[1];
        } catch (Exception ignored) {}

        String name = (d != null && d.nom != null) ? d.nom : "Dessert";
        double price = (d != null) ? d.prix : 0.0;
        String desc = (d != null && d.description != null && !d.description.isBlank())
                ? d.description
                : "Une touche sucrée.";

        int id = (d != null) ? d.idPlat : 0;

        Product product = new Product(
                id,
                name,
                desc,
                price,
                imagePath,
                "Dessert"
        );

        SceneManager.getInstance().showProductDetails(product);
        updateTotal();
    }

    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("home"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }
    @FXML public void goToAccueil() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
}