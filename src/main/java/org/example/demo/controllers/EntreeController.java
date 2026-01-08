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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class EntreeController {

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

    // --- Cycle de vie ---

    @FXML
    public void initialize() {
        loadEntreesFromApi();  // charge toutes les entrées
        updateTotal();
    }

    private void updateTotal() {
        double total = Cart.getInstance().getTotal();
        if (totalLabel != null) {
            totalLabel.setText(String.format("Total: %.2f €", total));
        }
    }

    // --- Chargement des entrées depuis l'API ---

    private void loadEntreesFromApi() {
        if (grid == null) {
            System.err.println("TilePane 'grid' non injecté (fx:id manquant dans le FXML ?)");
            return;
        }

        grid.getChildren().clear();

        PlatDto[] plats = fetchPlatsFromCategory(1); // 1 = Entrées

        if (plats == null || plats.length == 0) {
            System.out.println("Aucune entrée trouvée pour la catégorie 1");
            return;
        }

        for (PlatDto plat : plats) {
            if (plat != null && plat.disponible) {
                grid.getChildren().add(createEntreeCard(plat));
            }
        }
    }

    /**
     * Appelle /categories/{idCategorie}/plats et retourne le tableau de plats.
     */
    private PlatDto[] fetchPlatsFromCategory(int idCategorie) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/categories/" + idCategorie + "/plats");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("Erreur API /categories/" + idCategorie + "/plats : HTTP " + status);
                return null;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                Gson gson = new GsonBuilder().create();
                return gson.fromJson(reader, PlatDto[].class);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel API /categories/" + idCategorie + "/plats :");
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // --- Création d'une carte pour une entrée ---

    private StackPane createEntreeCard(PlatDto plat) {
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
        String nom = (plat.nom != null) ? plat.nom : "Entrée";
        Label nameLbl = new Label(nom);
        nameLbl.getStyleClass().add("productName");

        // Prix
        double prix = plat.prix;
        Label priceLbl = new Label(String.format("%.2f €", prix));
        priceLbl.getStyleClass().add("productPrice");

        vbox.getChildren().addAll(imgView, nameLbl, priceLbl);

        btn.setGraphic(vbox);
        btn.setUserData(plat);

        // Clic = ouvrir la popup détail
        btn.setOnAction(this::onSelectMenu);

        root.getChildren().add(btn);
        return root;
    }

    // --- Clic sur une carte => popup détail produit ---

    @FXML
    public void onSelectMenu(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        PlatDto plat = (PlatDto) clickedButton.getUserData();

        String imageUrl = "/org/example/demo/images/logo.jpg";

        String name = (plat != null && plat.nom != null) ? plat.nom : "Entrée";
        double price = (plat != null) ? plat.prix : 0.0;

        // ✅ Description venant de la BDD si présente
        String description;
        if (plat != null && plat.description != null && !plat.description.isBlank()) {
            description = plat.description;
        } else {
            description = "Une entrée savoureuse pour bien commencer.";
        }

        String categoryLabel = "Entrée";
        if (plat != null && plat.categorie != null) {
            if (plat.categorie.idCategorie == 1) categoryLabel = "Entrée";
            else if (plat.categorie.idCategorie == 2) categoryLabel = "Plat";
            else if (plat.categorie.idCategorie == 3) categoryLabel = "Boisson";
            else if (plat.categorie.idCategorie == 4) categoryLabel = "Dessert";
        }

        int id = (plat != null) ? plat.idPlat : 0;

        Product product = new Product(
                id,
                name,
                description,
                price,
                imageUrl,
                categoryLabel
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
    public void goToAccueil() throws IOException {
        SceneManager.getInstance().switchScene("accueil");
    }

    @FXML
    public void goToStarters() throws IOException {
        // si ta scène FXML des entrées s'appelle autrement, adapte ce nom
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
