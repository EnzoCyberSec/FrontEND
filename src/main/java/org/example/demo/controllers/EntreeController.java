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

    // DTO pour mapper la réponse JSON de l'API
    private static class PlatDto {
        int idPlat;
        String nom;
        String description;
        double prix;
        boolean disponible;
        CategorieDto categorie;
    }

    private static class CategorieDto {
        int idCategorie;
        String nom;
    }

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

    /**
     * Associe automatiquement le NOM du plat à son image locale.
     * Convention: plat.nom nettoyé -> NomDeFichier.png
     *
     * Exemple:
     *  "Chicken gyoza" -> "/org/example/demo/images/entrees/Chickengyoza.png"
     *  "Miso soup"     -> "/org/example/demo/images/entrees/Misosoup.png"
     *
     * Place tes images ici:
     *  src/main/resources/org/example/demo/images/entrees/
     *
     * Ajoute un fallback:
     *  default.png
     */
    private String imageForPlat(PlatDto plat) {
        String basePath = "/org/example/demo/images/entrees/";

        if (plat == null || plat.nom == null || plat.nom.isBlank()) {
            return basePath + "default.png";
        }

        // 1) lower
        String s = plat.nom.toLowerCase(Locale.ROOT);

        // 2) retire accents
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        // 3) garde lettres + espaces uniquement, vire ponctuation/chiffres
        s = s.replaceAll("[^a-z ]", " ");

        // 4) enlève espaces (Chicken gyoza -> chickengyoza)
        s = s.replaceAll("\\s+", "").trim();

        if (s.isEmpty()) {
            return basePath + "default.png";
        }

        // 5) Majuscule 1ère lettre (Chickengyoza)
        String fileName = Character.toUpperCase(s.charAt(0)) + s.substring(1);

        // 6) construit chemin final
        String imagePath = basePath + fileName + ".png";

        // 7) vérifie existence (sinon fallback)
        if (getClass().getResource(imagePath) != null) {
            return imagePath;
        }
        return basePath + "default.png";
    }

    /**
     * Construit un ImageView carré (sans déformation) avec crop centré.
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

            // viewport carré centré
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
     * Appel à l’API Javalin pour récupérer les entrées et remplir le TilePane grid.
     */
    private void loadEntreesFromApi() {
        if (grid == null) {
            System.err.println("TilePane 'grid' non injecté (fx:id manquant dans le FXML ?)");
            return;
        }

        grid.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/categories/1/plats");

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("Erreur API entrées : HTTP " + status);
                return;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                Gson gson = new GsonBuilder().create();
                PlatDto[] plats = gson.fromJson(reader, PlatDto[].class);

                if (plats != null) {
                    for (PlatDto plat : plats) {
                        grid.getChildren().add(createEntreeCard(plat));
                    }
                } else {
                    System.err.println("Réponse API vide ou invalide pour /categories/1/plats");
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel API /categories/1/plats :");
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private StackPane createEntreeCard(PlatDto plat) {
        StackPane root = new StackPane();

        Button btn = new Button();
        btn.getStyleClass().add("menuCardBtn");

        VBox vbox = new VBox();
        vbox.getStyleClass().add("productCard");

        // Image (auto: nom -> fichier)
        String imagePath = imageForPlat(plat);
        ImageView imgView = buildImageViewSquareCropped(imagePath, 100);

        // Nom
        String nom = (plat.nom != null) ? plat.nom : "Entrée";
        Label nameLbl = new Label(nom);
        nameLbl.getStyleClass().add("productName");

        // Prix
        double prix = plat.prix;
        Label priceLbl = new Label(String.format("%.2f €", prix));
        priceLbl.getStyleClass().add("productPrice");

        vbox.getChildren().addAll(imgView, nameLbl, priceLbl);

        btn.setGraphic(vbox);

        // stocke l'image pour le popup
        btn.setUserData(imagePath);

        btn.setOnAction(this::onSelectMenu);

        root.getChildren().add(btn);
        return root;
    }

    // --- Action Article (Popup) ---
    @FXML
    public void onSelectMenu(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        String name = "Entrée";
        double price = 0.0;

        // récupère l’image associée à la card
        String imageUrl = "/org/example/demo/images/entrees/default.png";
        if (clickedButton.getUserData() instanceof String) {
            imageUrl = (String) clickedButton.getUserData();
        }

        // récupère nom/prix depuis le VBox (graphic)
        try {
            if (clickedButton.getGraphic() instanceof VBox) {
                VBox vbox = (VBox) clickedButton.getGraphic();
                if (vbox.getChildren().size() >= 3) {
                    if (vbox.getChildren().get(1) instanceof Label) {
                        name = ((Label) vbox.getChildren().get(1)).getText();
                    }
                    if (vbox.getChildren().get(2) instanceof Label) {
                        String priceText = ((Label) vbox.getChildren().get(2)).getText()
                                .replace(" €", "").replace(",", ".").trim();
                        price = Double.parseDouble(priceText);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Product product = new Product(
                100 + (int) (Math.random() * 100),
                name,
                "Une entrée savoureuse pour bien commencer.",
                price,
                imageUrl,
                "Entrée"
        );

        SceneManager.getInstance().showProductDetails(product);
        updateTotal();
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

    @FXML public void goToAccueil() throws IOException { SceneManager.getInstance().switchScene("accueil"); }

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
    public void goToDrinks() throws IOException {
        SceneManager.getInstance().switchScene("boissons");
    }
}
