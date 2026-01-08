package org.example.demo.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.demo.models.Cart;
import org.example.demo.models.Product;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Popup de détails produit avec options dynamiques depuis l'API.
 */
public class ProductDetailsController {

    @FXML private ImageView productImage;
    @FXML private Label productName;
    @FXML private Label productPrice;
    @FXML private Label productDescription;
    @FXML private Label quantityLabel;
    @FXML private VBox optionsContainer;

    private Product product;
    private int quantity = 1;
    private double basePrice;

    private final List<CheckBox> optionCheckBoxes = new ArrayList<>();
    private Label optionsTitleLabel;

    // DTO pour mapper /plats/{id}/options
    private static class OptionDto {
        int idOption;
        String libelle;
        String type;   // SPICE_LEVEL, SIDE, EXTRA, ...
        double prix;
    }

    @FXML
    public void initialize() {
        // Quantité initiale
        quantityLabel.setText(String.valueOf(quantity));

        // Titre de la zone options
        if (optionsContainer != null) {
            optionsTitleLabel = new Label("Personnalisez votre commande :");
            optionsTitleLabel.setStyle("-fx-font-weight: bold;");
            optionsContainer.getChildren().add(optionsTitleLabel);
        }
    }

    /**
     * Appelé par SceneManager.showProductDetails(product)
     */
    public void setProduct(Product product) {
        this.product = product;
        this.basePrice = product.getPrice();

        productName.setText(product.getName());
        productPrice.setText(String.format("%.2f €", basePrice));
        productDescription.setText(product.getDescription());

        // Image : Product.imageUrl est un chemin de ressource (ex: /org/example/demo/images/logo.jpg)
        try {
            if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
                InputStream is = getClass().getResourceAsStream(product.getImageUrl());
                if (is != null) {
                    productImage.setImage(new Image(is));
                } else {
                    loadDefaultImage();
                }
            } else {
                loadDefaultImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadDefaultImage();
        }

        // Charger les options pour ce plat
        loadOptionsFromApi(product.getId());
    }

    private void loadDefaultImage() {
        try (InputStream is = getClass().getResourceAsStream("/org/example/demo/images/logo.jpg")) {
            if (is != null) {
                productImage.setImage(new Image(is));
            }
        } catch (IOException ignored) {}
    }

    /**
     * Appelle GET http://localhost:7001/plats/{id}/options
     */
    private void loadOptionsFromApi(int platId) {
        if (optionsContainer == null) {
            System.err.println("optionsContainer non injecté dans le FXML.");
            return;
        }

        // Nettoyer (mais garder le titre)
        optionsContainer.getChildren().setAll(optionsTitleLabel);
        optionCheckBoxes.clear();

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:7001/plats/" + platId + "/options");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("Erreur API /plats/" + platId + "/options : HTTP " + status);
                return;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                Gson gson = new GsonBuilder().create();
                OptionDto[] options = gson.fromJson(reader, OptionDto[].class);

                if (options == null || options.length == 0) {
                    System.out.println("Aucune option pour ce plat.");
                    return;
                }

                displayOptions(options);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel API /plats/" + platId + "/options :");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Crée des groupes + cases à cocher à partir des OptionDto.
     */
    private void displayOptions(OptionDto[] options) {
        // Grouper par type (SPICE_LEVEL, SIDE, EXTRA ...)
        Map<String, VBox> groups = new LinkedHashMap<>();

        for (OptionDto opt : options) {
            if (opt == null) continue;

            String typeKey = (opt.type != null) ? opt.type : "AUTRE";
            VBox groupBox = groups.get(typeKey);

            if (groupBox == null) {
                groupBox = new VBox(5);
                groupBox.setAlignment(Pos.CENTER_LEFT);
                groupBox.setPadding(new Insets(5, 0, 0, 0));

                Label groupLabel = new Label(getLabelForType(typeKey));
                groupLabel.setStyle("-fx-font-weight: bold;");

                groupBox.getChildren().add(groupLabel);
                groups.put(typeKey, groupBox);
                optionsContainer.getChildren().add(groupBox);
            }

            CheckBox cb = new CheckBox(formatOptionLabel(opt));
            cb.setUserData(opt);
            cb.setOnAction(e -> updateDisplayedPrice());

            groupBox.getChildren().add(cb);
            optionCheckBoxes.add(cb);
        }

        // Mettre à jour une première fois le prix
        updateDisplayedPrice();
    }

    private String getLabelForType(String typeKey) {
        if (typeKey == null) return "Options";

        return switch (typeKey) {
            case "SPICE_LEVEL" -> "Niveau de piment :";
            case "SIDE"        -> "Accompagnements :";
            case "EXTRA"       -> "Suppléments :";
            default            -> "Options :";
        };
    }

    private String formatOptionLabel(OptionDto opt) {
        if (opt.prix > 0.0) {
            return String.format("%s (+%.2f €)", opt.libelle, opt.prix);
        } else {
            return opt.libelle;
        }
    }

    /**
     * Recalcule le prix unitaire affiché en fonction des options cochées.
     */
    private void updateDisplayedPrice() {
        double extra = computeSelectedOptionsExtra();
        double unitPrice = basePrice + extra;
        productPrice.setText(String.format("%.2f €", unitPrice));
    }

    private double computeSelectedOptionsExtra() {
        double extra = 0.0;
        for (CheckBox cb : optionCheckBoxes) {
            if (cb.isSelected()) {
                OptionDto opt = (OptionDto) cb.getUserData();
                extra += opt.prix;
            }
        }
        return extra;
    }

    // ==================== Gestion quantité ====================

    @FXML
    private void increaseQuantity() {
        quantity++;
        quantityLabel.setText(String.valueOf(quantity));
        updateDisplayedPrice();
    }

    @FXML
    private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            quantityLabel.setText(String.valueOf(quantity));
            updateDisplayedPrice();
        }
    }

    // ==================== Boutons bas ====================

    @FXML
    private void addToCart() {
        if (product == null) {
            System.err.println("Produit non défini dans ProductDetailsController.");
            return;
        }

        double extra = computeSelectedOptionsExtra();
        double unitPrice = basePrice + extra;

        // Construire la description finale avec les options sélectionnées
        List<String> selectedLabels = new ArrayList<>();
        for (CheckBox cb : optionCheckBoxes) {
            if (cb.isSelected()) {
                OptionDto opt = (OptionDto) cb.getUserData();
                selectedLabels.add(opt.libelle);
            }
        }

        String finalDescription = product.getDescription();
        if (!selectedLabels.isEmpty()) {
            String optionsText = "Options : " + String.join(", ", selectedLabels);
            finalDescription = finalDescription + "\n" + optionsText;
        }

        // Nouveau Product avec prix ajusté et description enrichie
        Product pWithOptions = new Product(
                product.getId(),
                product.getName(),
                finalDescription,
                unitPrice,
                product.getImageUrl(),
                product.getCategory()
        );

        // ✅ utilisation de ton Cart : une seule ligne
        Cart.getInstance().addItem(pWithOptions, quantity);

        closeWindow();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) quantityLabel.getScene().getWindow();
        stage.close();
    }
}
