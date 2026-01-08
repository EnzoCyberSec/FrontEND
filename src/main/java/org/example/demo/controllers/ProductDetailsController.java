package org.example.demo.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button; // AJOUT
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.demo.models.Cart;
import org.example.demo.models.Option;
import org.example.demo.models.Product;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ProductDetailsController {

    @FXML private ImageView productImage;
    @FXML private Label productName;
    @FXML private Label productPrice;
    @FXML private Label productDescription;
    @FXML private Label quantityLabel;
    @FXML private VBox optionsContainer;

    // AJOUT : Référence au bouton d'ajout (nécessite fx:id="addToCartBtn" dans le FXML)
    @FXML private Button addToCartBtn;

    private Product product;
    private int quantity = 1;
    private double basePrice;

    private final List<CheckBox> optionCheckBoxes = new ArrayList<>();
    private Label optionsTitleLabel;

    @FXML
    public void initialize() {
        quantityLabel.setText(String.valueOf(quantity));
        if (optionsContainer != null) {
            optionsTitleLabel = new Label("Customize your order:");
            optionsTitleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 0 10 0;");
            optionsContainer.getChildren().add(optionsTitleLabel);
        }
    }

    public void setProduct(Product product) {
        this.product = product;
        this.basePrice = product.getPrice();
        this.quantity = 1;
        quantityLabel.setText("1");
        productName.setText(product.getName());

        // --- LOGIQUE DE DISPONIBILITÉ (AJOUT) ---
        if (!product.isAvailable()) {
            // Cas INDISPONIBLE
            productPrice.setText("Unavailable");
            // Style rouge pour alerter l'utilisateur
            productPrice.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: red;");

            if (addToCartBtn != null) {
                addToCartBtn.setDisable(true); // Désactive le bouton
                addToCartBtn.setText("Out of stock");
            }
        } else {
            // Cas DISPONIBLE
            productPrice.setText(String.format("%.2f €", basePrice));
            // On restaure le style normal (orange/défaut)
            productPrice.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e46725;");

            if (addToCartBtn != null) {
                addToCartBtn.setDisable(false); // Active le bouton
                addToCartBtn.setText("Add to cart");
            }
        }
        // ----------------------------------------

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            productDescription.setText(product.getDescription());
        } else {
            productDescription.setText("No description available.");
        }

        loadImage(product.getImageUrl());

        String category = product.getCategory();
        boolean isExcluded = category != null && (
                category.toLowerCase().contains("dessert") ||
                        category.toLowerCase().contains("boisson") ||
                        category.toLowerCase().contains("drink")
        );

        if (isExcluded) {
            if (optionsContainer != null) optionsContainer.getChildren().clear();
        } else {
            // On ne charge les options que si le produit est disponible (optionnel, mais plus propre)
            if (product.isAvailable()) {
                loadOptionsFromApi(product.getId());
            } else if (optionsContainer != null) {
                optionsContainer.getChildren().clear();
            }
        }
    }

    private void loadImage(String url) {
        try {
            if (url != null && !url.isBlank()) {
                InputStream is = getClass().getResourceAsStream(url);
                if (is != null) productImage.setImage(new Image(is));
                else loadDefaultImage();
            } else loadDefaultImage();
        } catch (Exception e) { loadDefaultImage(); }
    }

    private void loadDefaultImage() {
        try (InputStream is = getClass().getResourceAsStream("/org/example/demo/images/logo.jpg")) {
            if (is != null) productImage.setImage(new Image(is));
        } catch (IOException ignored) {}
    }

    private void loadOptionsFromApi(int platId) {
        if (optionsContainer == null) return;
        optionsContainer.getChildren().setAll(optionsTitleLabel);
        optionCheckBoxes.clear();

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/plats/" + platId + "/options");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return;

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Gson gson = new GsonBuilder().create();
                // On charge directement dans notre modèle Option
                Option[] options = gson.fromJson(reader, Option[].class);
                if (options != null && options.length > 0) {
                    displayOptions(options);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading options: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private void displayOptions(Option[] options) {
        Map<String, VBox> groups = new LinkedHashMap<>();

        for (Option opt : options) {
            if (opt == null) continue;
            String typeKey = (opt.getType() != null) ? opt.getType() : "OTHER";

            VBox groupBox = groups.get(typeKey);
            if (groupBox == null) {
                groupBox = new VBox(5);
                groupBox.setAlignment(Pos.CENTER_LEFT);
                groupBox.setPadding(new Insets(10, 0, 5, 0));
                Label groupLabel = new Label(getLabelForType(typeKey));
                groupLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
                groupBox.getChildren().add(groupLabel);
                groups.put(typeKey, groupBox);
                optionsContainer.getChildren().add(groupBox);
            }

            CheckBox cb = new CheckBox(formatOptionLabel(opt));
            cb.setUserData(opt); // IMPORTANT : On stocke l'objet Option
            cb.setOnAction(e -> updateDisplayedPrice());

            groupBox.getChildren().add(cb);
            optionCheckBoxes.add(cb);
        }
    }

    private String getLabelForType(String typeKey) {
        return switch (typeKey) {
            case "SPICE_LEVEL" -> "Spice level:";
            case "SIDE" -> "Side dishes:";
            case "EXTRA" -> "Extras:";
            default -> "Other options:";
        };
    }

    private String formatOptionLabel(Option opt) {
        if (opt.getPrix() > 0.0) {
            return String.format("%s (+%.2f €)", opt.getLibelle(), opt.getPrix());
        }
        return opt.getLibelle();
    }

    private void updateDisplayedPrice() {
        double extra = computeSelectedOptionsExtra();
        double unitPrice = basePrice + extra;
        productPrice.setText(String.format("%.2f €", unitPrice));
    }

    private double computeSelectedOptionsExtra() {
        double extra = 0.0;
        for (CheckBox cb : optionCheckBoxes) {
            if (cb.isSelected()) {
                Option opt = (Option) cb.getUserData();
                extra += opt.getPrix();
            }
        }
        return extra;
    }

    @FXML private void increaseQuantity() {
        quantity++;
        quantityLabel.setText(String.valueOf(quantity));
    }

    @FXML private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            quantityLabel.setText(String.valueOf(quantity));
        }
    }

    @FXML
    private void addToCart() {
        // AJOUT : Sécurité supplémentaire
        if (product == null || !product.isAvailable()) return;

        double extra = computeSelectedOptionsExtra();
        double unitPrice = basePrice + extra;

        List<Option> selectedOptions = new ArrayList<>();
        List<String> selectedLabels = new ArrayList<>();

        // 1. Récupération des options cochées
        for (CheckBox cb : optionCheckBoxes) {
            if (cb.isSelected()) {
                Option opt = (Option) cb.getUserData();
                selectedOptions.add(opt);
                selectedLabels.add(opt.getLibelle());
            }
        }

        // 2. Mise à jour de la description pour l'affichage
        String finalDescription = product.getDescription();
        if (!selectedLabels.isEmpty()) {
            String optionsText = "\n[Options: " + String.join(", ", selectedLabels) + "]";
            finalDescription = (finalDescription == null ? "" : finalDescription) + optionsText;
        }

        // On crée un clone pour le panier
        // Note: on réutilise product.getCategory() et autres
        Product pWithOptions = new Product(
                product.getId(),
                product.getName(),
                finalDescription,
                unitPrice,
                product.getImageUrl(),
                product.getCategory(),
                true // Dans le panier, l'article est considéré comme "validé/disponible" à l'instant T
        );

        // 3. IMPORTANT : On passe la liste des options au panier
        Cart.getInstance().addItem(pWithOptions, quantity, selectedOptions);

        closeWindow();
    }

    @FXML private void closeWindow() {
        Stage stage = (Stage) quantityLabel.getScene().getWindow();
        stage.close();
    }
}