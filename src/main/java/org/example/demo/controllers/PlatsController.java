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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        loadPlatsFromApi();  // charge les plats depuis l’API
        updateTotal();
    }

    private void updateTotal() {
        double total = Cart.getInstance().getTotal();
        if (totalLabel != null) {
            totalLabel.setText(String.format("Total: %.2f €", total));
        }
    }

    /**
     * Appel à l’API Javalin pour récupérer les plats et
     * remplir le TilePane grid.
     */
    private void loadPlatsFromApi() {
        if (grid == null) {
            System.err.println("TilePane 'grid' non injecté (fx:id manquant dans le FXML ?)");
            return;
        }

        // On vide le TilePane pour ne garder que les résultats de l’API
        grid.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/categories/2/plats"); // adapte si besoin
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
                    grid.getChildren().add(createPlatCard(plat));
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel API /categories/2/plats :");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Crée une carte (StackPane) pour un plat donné.
     */
    private StackPane createPlatCard(PlatDto plat) {
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
        Label nameLbl = new Label(plat.nom != null ? plat.nom : "Plat");
        nameLbl.getStyleClass().add("productName");

        // Prix
        double prix = plat.prix;
        Label priceLbl = new Label(String.format("%.2f €", prix));
        priceLbl.getStyleClass().add("productPrice");

        vbox.getChildren().addAll(imgView, nameLbl, priceLbl);

        btn.setGraphic(vbox);

        // Clic sur la carte = ouvrir popup détails
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
        String imageUrl = "/org/example/demo/images/logo.jpg";

        try {
            if (clickedButton.getGraphic() instanceof VBox vbox
                    && vbox.getChildren().size() >= 3) {

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

    @FXML
    public void goToMenus() throws IOException {
        SceneManager.getInstance().switchScene("menu");
    }

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
