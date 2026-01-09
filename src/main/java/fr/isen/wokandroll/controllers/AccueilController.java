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

    // =================================================================================
    // LOGIQUE DE GESTION DES IMAGES (Apport de JB)
    // =================================================================================

    // Dossiers d'images + images par défaut (Fallback)
    private static final String BASE_ENTREES  = "/fr/isen/wokandroll/images/entrees/";
    private static final String BASE_PLATS    = "/fr/isen/wokandroll/images/plats/";
    private static final String BASE_BOISSONS = "/fr/isen/wokandroll/images/boissons/";
    private static final String BASE_DESSERTS = "/fr/isen/wokandroll/images/desserts/";

    private static final String DEFAULT_ENTREES  = BASE_ENTREES  + "default.png";
    private static final String DEFAULT_PLATS    = BASE_PLATS    + "default.png";
    private static final String DEFAULT_BOISSONS = BASE_BOISSONS + "default.png";
    private static final String DEFAULT_DESSERTS = BASE_DESSERTS + "default.png";

    // Mapping Nom BDD -> Fichier image
    private static final Map<String, String> IMG_ENTREES = Map.of(
            "Pork spring rolls", "Porkspringrolls.png",
            "Vegetable spring rolls", "Vegetablespringrolls.png",
            "Miso soup", "Misosoup.png",
            "Chicken gyoza", "Chickengyoza.png",
            "Wakame salad", "Wakamesalad.png"
    );

    private static final Map<String, String> IMG_PLATS = Map.of(
            "Chicken pad thai", "Chickenpadthai.png",
            "Beef lok lak", "Beefloklak.png",
            "Red curry chicken", "Redcurrychicken.png",
            "Cantonese rice", "Cantoneserice.png",
            "Vegetable noodles", "Vegetablenoodles.png"
    );

    private static final Map<String, String> IMG_BOISSONS = Map.of(
            "Homemade iced tea", "Homemadeicedtea.png",
            "Coca-Cola", "CocaCola.png",
            "Mineral water", "Mineralwater.png",
            "Asian beer", "Asianbeer.png",
            "Lychee juice", "Lycheejuice.png"
    );

    private static final Map<String, String> IMG_DESSERTS = Map.of(
            "Coconut pearls", "Coconutpearls.png",
            "Ice cream mochi", "Icecreammochi.png",
            "Fried banana", "Friedbanana.png",
            "Lychees in syrup", "Lycheesinsyrup.png",
            "Fresh pineapple", "Freshpineapple.png"
    );

    // =================================================================================
    // INITIALISATION
    // =================================================================================

    @FXML
    public void initialize() {
        loadRandomProductsFromApi();
        updateTotal();
    }

    private void updateTotal() {
        double total = Cart.getInstance().getTotal();
        if (totalLabel != null) {
            totalLabel.setText(String.format("Total: %.2f €", total));
        }
    }

    // =================================================================================
    // API & CHARGEMENT
    // =================================================================================

    private void loadRandomProductsFromApi() {
        if (grid == null) return;
        grid.getChildren().clear();

        // Récupère un plat aléatoire pour chaque catégorie (1=Entrée, 2=Plat, etc.)
        PlatDto entree  = fetchRandomPlatFromCategory(1);
        PlatDto plat    = fetchRandomPlatFromCategory(2);
        PlatDto boisson = fetchRandomPlatFromCategory(3);
        PlatDto dessert = fetchRandomPlatFromCategory(4);

        if (entree  != null) grid.getChildren().add(createProductCard(entree));
        if (plat    != null) grid.getChildren().add(createProductCard(plat));
        if (boisson != null) grid.getChildren().add(createProductCard(boisson));
        if (dessert != null) grid.getChildren().add(createProductCard(dessert));
    }

    private PlatDto fetchRandomPlatFromCategory(int idCategorie) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/categories/" + idCategorie + "/plats");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                Gson gson = new GsonBuilder().create();
                PlatDto[] plats = gson.fromJson(reader, PlatDto[].class);
                if (plats == null || plats.length == 0) return null;

                // Plat aléatoire
                int index = (int) (Math.random() * plats.length);
                return plats[index];
            }
        } catch (Exception e) {
            System.err.println("Erreur API : " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // =================================================================================
    // CRÉATION DES CARTES PRODUIT (Fusion UI Enzo + Logique Image JB)
    // =================================================================================

    private StackPane createProductCard(PlatDto plat) {
        StackPane root = new StackPane();

        Button btn = new Button();
        btn.getStyleClass().add("menuCardBtn");

        VBox vbox = new VBox();
        vbox.getStyleClass().add("productCard");

        // 1. Déterminer la bonne image grâce à la logique de JB
        String imagePath = imagePathFor(plat);

        // 2. Créer l'ImageView carrée et centrée
        ImageView imgView = squareImage(imagePath, 100);

        // 3. Labels
        String nom = (plat.nom != null) ? plat.nom : "Produit";
        Label nameLbl = new Label(nom);
        nameLbl.getStyleClass().add("productName");

        Label priceLbl = new Label(String.format("%.2f €", plat.prix));
        priceLbl.getStyleClass().add("productPrice");

        vbox.getChildren().addAll(imgView, nameLbl, priceLbl);

        // 4. Stocker le DTO ET le chemin de l'image dans le bouton pour le clic
        btn.setUserData(new Object[]{plat, imagePath});
        btn.setGraphic(vbox);
        btn.setOnAction(this::onSelectMenu);

        root.getChildren().add(btn);
        return root;
    }

    // --- Méthodes utilitaires pour les images (JB) ---

    private String imagePathFor(PlatDto p) {
        int cat = (p != null && p.categorie != null) ? p.categorie.idCategorie : 0;
        String name = (p != null) ? p.nom : null;

        if (cat == 1) return pickFromMap(BASE_ENTREES, DEFAULT_ENTREES, IMG_ENTREES, name);
        if (cat == 2) return pickFromMap(BASE_PLATS, DEFAULT_PLATS, IMG_PLATS, name);
        if (cat == 3) return pickFromMap(BASE_BOISSONS, DEFAULT_BOISSONS, IMG_BOISSONS, name);
        if (cat == 4) return pickFromMap(BASE_DESSERTS, DEFAULT_DESSERTS, IMG_DESSERTS, name);

        return DEFAULT_PLATS;
    }

    private String pickFromMap(String base, String fallback, Map<String, String> map, String name) {
        if (name == null) return fallback;

        // Vérification directe dans la Map
        String file = map.get(name);
        if (file != null) {
            String path = base + file;
            if (getClass().getResource(path) != null) return path;
        }

        // Patch spécial pour Coca-Cola (gestion minuscules/tirets)
        if (base.equals(BASE_BOISSONS)) {
            String key = name.toLowerCase().replaceAll("[^a-z]", "");
            if (key.contains("cocacola")) {
                String p = base + "CocaCola.png"; // Nom exact du fichier
                if (getClass().getResource(p) != null) return p;
            }
        }

        // Patch spécial Mochi
        if (name.equals("Ice cream mochi") && base.equals(BASE_DESSERTS)) {
            String p = base + "Icecreammochi.png";
            if (getClass().getResource(p) != null) return p;
        }

        return fallback;
    }

    // Crée une image rognée en carré pour un rendu uniforme
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

        if (img != null) {
            iv.setImage(img);
            // Calcul pour centrer l'image (crop)
            double w = img.getWidth();
            double h = img.getHeight();
            double side = Math.min(w, h);
            iv.setViewport(new Rectangle2D((w - side) / 2, (h - side) / 2, side, side));
        }
        return iv;
    }

    // =================================================================================
    // NAVIGATION & INTERACTIONS
    // =================================================================================

    @FXML
    public void onSelectMenu(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        PlatDto plat = null;
        String imageUrl = DEFAULT_PLATS;

        // Récupération sécurisée des données stockées dans le bouton
        try {
            Object[] data = (Object[]) clickedButton.getUserData();
            plat = (PlatDto) data[0];
            imageUrl = (String) data[1];
        } catch (Exception ignored) {}

        String name = (plat != null && plat.nom != null) ? plat.nom : "Produit";
        double price = (plat != null) ? plat.prix : 0.0;

        String description = (plat != null && plat.description != null && !plat.description.isBlank())
                ? plat.description
                : "Un délicieux " + name + " préparé avec soin.";

        String categoryLabel = "Produit";
        int catId = (plat != null && plat.categorie != null) ? plat.categorie.idCategorie : 0;
        if (catId == 1) categoryLabel = "Entrée";
        else if (catId == 2) categoryLabel = "Plat";
        else if (catId == 3) categoryLabel = "Boisson";
        else if (catId == 4) categoryLabel = "Dessert";

        int id = (plat != null) ? plat.idPlat : 0;

        Product product = new Product(
                id,
                name,
                description,
                price,
                imageUrl, // On passe la vraie URL d'image ici
                categoryLabel
        );

        SceneManager.getInstance().showProductDetails(product);
        updateTotal();
    }

    // Liens de navigation
    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("home"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }
    @FXML public void goToAccueil() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
}