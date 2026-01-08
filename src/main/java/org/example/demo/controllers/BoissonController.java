package org.example.demo.controllers;

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
import java.util.Map;
import java.util.Objects;

public class BoissonController {

    @FXML private Label totalLabel;
    @FXML private TilePane grid;

    public static class CategorieDto { public int idCategorie; public String nom; }

    public static class BoissonDto {
        public int idPlat;
        public String nom;
        public String description;
        public double prix;
        public boolean disponible;
        public CategorieDto categorie;
    }

    private static final String BASE = "/org/example/demo/images/boissons/";
    private static final String DEFAULT_IMG = BASE + "default.png";

    // ✅ NOMS EXACTS BDD/API -> FICHIERS IMAGES
    private static final Map<String, String> IMG_BY_NAME = Map.of(
            "Homemade iced tea", "Homemadeicedtea.png",
            "Coca Cola", "Cocacola.png",
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
        if (totalLabel != null) totalLabel.setText(String.format("Total: %.2f €", total));
    }

    private String imagePathFor(BoissonDto b) {
        if (b == null || b.nom == null) return DEFAULT_IMG;

        String file = IMG_BY_NAME.get(b.nom);
        if (file == null) return DEFAULT_IMG;

        String path = BASE + file;
        return (getClass().getResource(path) != null) ? path : DEFAULT_IMG;
    }

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

        if (img == null) {
            try (InputStream is2 = getClass().getResourceAsStream(DEFAULT_IMG)) {
                img = new Image(Objects.requireNonNull(is2));
            } catch (Exception ignored) {}
        }

        if (img != null) {
            iv.setImage(img);
            double w = img.getWidth(), h = img.getHeight();
            double side = Math.min(w, h);
            iv.setViewport(new Rectangle2D((w - side) / 2, (h - side) / 2, side, side));
        }

        return iv;
    }

    private void loadBoissonsFromApi() {
        if (grid == null) return;
        grid.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/categories/3/plats");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.err.println("Erreur API /categories/3/plats : HTTP " + conn.getResponseCode());
                return;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                BoissonDto[] boissons = new GsonBuilder().create().fromJson(reader, BoissonDto[].class);
                if (boissons == null) return;

                for (BoissonDto b : boissons) {
                    if (!b.disponible) continue;
                    grid.getChildren().add(createBoissonCard(b));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private StackPane createBoissonCard(BoissonDto b) {
        StackPane root = new StackPane();

        Button btn = new Button();
        btn.getStyleClass().add("menuCardBtn");

        VBox card = new VBox();
        card.getStyleClass().add("productCard");

        String imgPath = imagePathFor(b);
        ImageView imgView = squareImage(imgPath, 100);

        Label nameLbl = new Label(b.nom != null ? b.nom : "Boisson");
        nameLbl.getStyleClass().add("productName");

        Label priceLbl = new Label(String.format("%.2f €", b.prix));
        priceLbl.getStyleClass().add("productPrice");

        card.getChildren().addAll(imgView, nameLbl, priceLbl);
        btn.setGraphic(card);

        // stocke pour la popup
        btn.setUserData(new Object[]{imgPath, b});
        btn.setOnAction(this::onSelectMenu);

        root.getChildren().add(btn);
        return root;
    }

    @FXML
    public void onSelectMenu(ActionEvent event) {
        Button btn = (Button) event.getSource();

        String imgPath = DEFAULT_IMG;
        String name = "Boisson";
        double price = 0.0;
        String desc = "Rafraîchissant.";

        try {
            Object[] data = (Object[]) btn.getUserData();
            imgPath = (String) data[0];
            BoissonDto b = (BoissonDto) data[1];
            if (b != null) {
                name = (b.nom != null) ? b.nom : name;
                price = b.prix;
                if (b.description != null && !b.description.isBlank()) desc = b.description;
            }
        } catch (Exception ignored) {}

        Product product = new Product(
                400 + (int) (Math.random() * 100),
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
    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("hello-view"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }
    @FXML public void goToAccueil() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
}
