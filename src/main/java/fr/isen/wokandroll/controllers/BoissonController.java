package fr.isen.wokandroll.controllers;

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
import fr.isen.wokandroll.managers.SceneManager;
import fr.isen.wokandroll.models.Cart;
import fr.isen.wokandroll.models.Product;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class BoissonController {

    @FXML private Label totalLabel;
    @FXML private TilePane grid;

    // --- DTOs ---
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

    // Gestion des images
    private static final String BASE = "/fr/isen/wokandroll/images/boissons/";
    private static final String DEFAULT_IMG = BASE + "default.png";

    // Mapping Nom exact BDD -> Nom fichier image
    private static final Map<String, String> IMG_BY_NAME = Map.of(
            "Homemade iced tea", "Homemadeicedtea.png",
            "Mineral water", "Mineralwater.png",
            "Asian beer", "Asianbeer.png",
            "Lychee juice", "Lycheejuice.png"
    );

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

    // --- Logique de récupération d'image ---

    // Vérifie si un fichier existe dans les ressources
    private String firstExisting(String... fileNames) {
        for (String f : fileNames) {
            String path = BASE + f;
            if (getClass().getResource(path) != null) return path;
        }
        return DEFAULT_IMG;
    }

    // Normalise une chaîne (minuscule, lettres uniquement)
    private String keyOf(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("[^a-z]", "");
    }

    private String imagePathFor(BoissonDto b) {
        if (b == null || b.nom == null) return DEFAULT_IMG;

        String key = keyOf(b.nom);

        // Cas spécial : Coca-Cola (Gère les tirets, espaces, majuscules)
        if (key.contains("cocacola")) {
            return firstExisting(
                    "CocaCola.png",
                    "Coca-Cola.png",
                    "Cocacola.png",
                    "Coca-cola.png",
                    "cocacola.png"
            );
        }

        // Cas standard : via la Map
        String file = IMG_BY_NAME.get(b.nom);
        if (file != null) {
            String path = BASE + file;
            if (getClass().getResource(path) != null) return path;
        }

        return DEFAULT_IMG;
    }

    // Crée une ImageView carrée et centrée
    private ImageView squareImage(String resourcePath, double size) {
        ImageView iv = new ImageView();
        iv.getStyleClass().add("productImg");
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);

        Image img = null;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            img = new Image(Objects.requireNonNull(is));
        } catch (Exception ignored) {}

        // Fallback si l'image n'est pas chargée
        if (img == null) {
            try (InputStream is2 = getClass().getResourceAsStream(DEFAULT_IMG)) {
                img = new Image(Objects.requireNonNull(is2));
            } catch (Exception ignored) {}
        }

        if (img != null) {
            iv.setImage(img);
            double w = img.getWidth();
            double h = img.getHeight();
            double side = Math.min(w, h);
            iv.setViewport(new Rectangle2D((w - side) / 2, (h - side) / 2, side, side));
        }

        return iv;
    }

    // --- Chargement API ---

    private void loadBoissonsFromApi() {
        if (grid == null) {
            System.err.println("TilePane 'grid' non injecté.");
            return;
        }
        grid.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/categories/3/plats");
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
                BoissonDto[] boissons = gson.fromJson(reader, BoissonDto[].class);

                if (boissons != null) {
                    for (BoissonDto b : boissons) {
                        if (b.disponible) {
                            grid.getChildren().add(createBoissonCard(b));
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

    // --- Création de la carte (UI) ---

    private StackPane createBoissonCard(BoissonDto b) {
        StackPane root = new StackPane();

        Button btn = new Button();
        btn.getStyleClass().add("menuCardBtn");

        VBox card = new VBox();
        card.getStyleClass().add("productCard");

        // 1. Image intelligente
        String imgPath = imagePathFor(b);
        ImageView imgView = squareImage(imgPath, 100);

        // 2. Textes
        Label nameLbl = new Label(b.nom != null ? b.nom : "Boisson");
        nameLbl.getStyleClass().add("productName");

        Label priceLbl = new Label(String.format("%.2f €", b.prix));
        priceLbl.getStyleClass().add("productPrice");

        card.getChildren().addAll(imgView, nameLbl, priceLbl);
        btn.setGraphic(card);

        // 3. Stockage des données (DTO + Chemin image)
        btn.setUserData(new Object[]{imgPath, b});
        btn.setOnAction(this::onSelectMenu);

        root.getChildren().add(btn);
        return root;
    }

    // --- Clic et Navigation ---

    @FXML
    public void onSelectMenu(ActionEvent event) {
        Button btn = (Button) event.getSource();

        String imgPath = DEFAULT_IMG;
        BoissonDto b = null;

        try {
            Object[] data = (Object[]) btn.getUserData();
            imgPath = (String) data[0];
            b = (BoissonDto) data[1];
        } catch (Exception ignored) {}

        String name = (b != null && b.nom != null) ? b.nom : "Boisson";
        double price = (b != null) ? b.prix : 0.0;
        String desc = (b != null && b.description != null && !b.description.isBlank())
                ? b.description
                : "Rafraîchissant.";

        int id = (b != null) ? b.idPlat : 0;

        Product product = new Product(
                id,
                name,
                desc,
                price,
                imgPath,
                "Boisson"
        );

        SceneManager.getInstance().showProductDetails(product);
        updateTotal();
    }

    // Navigation
    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("home"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }
    @FXML public void goToAccueil() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
}