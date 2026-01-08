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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

public class PlatsController {

    @FXML private Label totalLabel;
    @FXML private TilePane grid;

    // DTO pour mapper la réponse JSON /categories/2/plats
    public static class CategorieDto {
        public int idCategorie;
        public String nom;
    }

    public static class PlatDto {
        public int idPlat;
        public String nom;
        public String description;
        public double prix;
        public boolean disponible;
        public CategorieDto categorie;
    }

    @FXML
    public void initialize() {
        loadPlatsFromApi();
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
     *  src/main/resources/org/example/demo/images/plats/
     *
     * Tes images:
     *  Beefloklak.png
     *  Cantoneserice.png
     *  Chickenpadthai.png
     *  Redcurrychicken.png
     *  Vegetablenoodles.png
     *
     * Ajoute un fallback:
     *  default.png
     */
    private String imageForPlat(PlatDto plat) {
        String basePath = "/org/example/demo/images/plats/";

        if (plat == null || plat.nom == null || plat.nom.isBlank()) {
            return basePath + "default.png";
        }

        // 1) lower
        String s = plat.nom.toLowerCase(Locale.ROOT);

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

        // 7) vérifie existence (sinon fallback)
        if (getClass().getResource(imagePath) != null) {
            return imagePath;
        }
        return basePath + "default.png";
    }

    /**
     * Même méthode que EntreeController:
     * ImageView carré (sans déformation) avec crop centré.
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
     * Appel à l’API Javalin pour récupérer les plats et remplir le TilePane grid.
     */
    private void loadPlatsFromApi() {
        if (grid == null) {
            System.err.println("TilePane 'grid' non injecté (fx:id manquant dans le FXML ?)");
            return;
        }

        grid.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/categories/2/plats");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("Erreur API /categories/2/plats : HTTP " + status);
                return;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                Gson gson = new GsonBuilder().create();
                PlatDto[] plats = gson.fromJson(reader, PlatDto[].class);

                if (plats == null || plats.length == 0) {
                    System.out.println("Aucun plat reçu depuis l’API.");
                    return;
                }

                for (PlatDto plat : plats) {
                    if (!plat.disponible) continue;
                    grid.getChildren().add(createPlatCard(plat));
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel API /categories/2/plats :");
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private StackPane createPlatCard(PlatDto plat) {
        StackPane root = new StackPane();

        Button btn = new Button();
        btn.getStyleClass().add("menuCardBtn");

        VBox vbox = new VBox();
        vbox.getStyleClass().add("productCard");

        // Image (auto: nom -> fichier)
        String imagePath = imageForPlat(plat);
        ImageView imgView = buildImageViewSquareCropped(imagePath, 100);

        // Nom
        Label nameLbl = new Label(plat.nom != null ? plat.nom : "Plat");
        nameLbl.getStyleClass().add("productName");

        // Prix
        Label priceLbl = new Label(String.format("%.2f €", plat.prix));
        priceLbl.getStyleClass().add("productPrice");

        vbox.getChildren().addAll(imgView, nameLbl, priceLbl);

        btn.setGraphic(vbox);

        // stocke l'image pour le popup (comme Entree)
        btn.setUserData(imagePath);

        btn.setOnAction(this::onSelectMenu);

        root.getChildren().add(btn);
        return root;
    }

    // --- Action Article (Popup) ---
    @FXML
    public void onSelectMenu(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        String name = "Plat";
        double price = 0.0;

        // récupère l’image associée à la card
        String imageUrl = "/org/example/demo/images/plats/default.png";
        if (clickedButton.getUserData() instanceof String) {
            imageUrl = (String) clickedButton.getUserData();
        }

        try {
            if (clickedButton.getGraphic() instanceof VBox vbox && vbox.getChildren().size() >= 3) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        Product product = new Product(
                300 + (int) (Math.random() * 100),
                name,
                "Un plat savoureux et copieux.",
                price,
                imageUrl,
                "Plat"
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
