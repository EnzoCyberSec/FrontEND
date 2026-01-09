package fr.isen.wokandroll.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import fr.isen.wokandroll.models.Cart;
import fr.isen.wokandroll.models.Option;
import fr.isen.wokandroll.models.Product;

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

    @FXML private Button addToCartBtn;

    private Product product;
    private int quantity = 1;
    private double basePrice;

    private final List<ButtonBase> optionControls = new ArrayList<>();

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

        if (!product.isAvailable()) {
            productPrice.setText("Unavailable");
            productPrice.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: red;");
            if (addToCartBtn != null) {
                addToCartBtn.setDisable(true);
                addToCartBtn.setText("Out of stock");
            }
        } else {
            productPrice.setText(String.format("%.2f €", basePrice));
            productPrice.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e46725;");
            if (addToCartBtn != null) {
                addToCartBtn.setDisable(false);
                addToCartBtn.setText("Add to cart");
            }
        }

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
        try (InputStream is = getClass().getResourceAsStream("/fr/isen/wokandroll/images/logo.jpg")) {
            if (is != null) productImage.setImage(new Image(is));
        } catch (IOException ignored) {}
    }

    private void loadOptionsFromApi(int platId) {
        if (optionsContainer == null) return;
        optionsContainer.getChildren().setAll(optionsTitleLabel);
        optionControls.clear(); // On vide la nouvelle liste

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
        Map<String, ToggleGroup> toggleGroups = new HashMap<>();

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

            ButtonBase selector;

            if ("SPICE_LEVEL".equals(typeKey)) {
                // Pour le piment, on utilise un RadioButton
                RadioButton rb = new RadioButton(formatOptionLabel(opt));

                // On récupère ou crée le groupe pour ce type d'option
                ToggleGroup tg = toggleGroups.computeIfAbsent(typeKey, k -> new ToggleGroup());
                rb.setToggleGroup(tg);

                selector = rb;
            } else {
                // Pour le reste, on garde la CheckBox
                selector = new CheckBox(formatOptionLabel(opt));
            }

            selector.setUserData(opt);
            selector.setOnAction(e -> updateDisplayedPrice());

            groupBox.getChildren().add(selector);
            optionControls.add(selector); // On ajoute à la liste générique
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

    private boolean isControlSelected(ButtonBase control) {
        if (control instanceof CheckBox) {
            return ((CheckBox) control).isSelected();
        } else if (control instanceof RadioButton) {
            return ((RadioButton) control).isSelected();
        }
        return false;
    }

    private double computeSelectedOptionsExtra() {
        double extra = 0.0;
        // On parcourt la liste générique
        for (ButtonBase control : optionControls) {
            if (isControlSelected(control)) {
                Option opt = (Option) control.getUserData();
                extra += opt.getPrix();
            }
        }
        return extra;
    }

    @FXML private void increaseQuantity() {
        if (quantity < 9) {
            quantity++;
            quantityLabel.setText(String.valueOf(quantity));
        }
    }

    @FXML private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            quantityLabel.setText(String.valueOf(quantity));
        }
    }

    @FXML
    private void addToCart() {
        if (product == null || !product.isAvailable()) return;

        double extra = computeSelectedOptionsExtra();
        double unitPrice = basePrice + extra;

        List<Option> selectedOptions = new ArrayList<>();
        List<String> selectedLabels = new ArrayList<>();

        // 1. Récupération des options cochées (générique)
        for (ButtonBase control : optionControls) {
            if (isControlSelected(control)) {
                Option opt = (Option) control.getUserData();
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

        Product pWithOptions = new Product(
                product.getId(),
                product.getName(),
                finalDescription,
                unitPrice,
                product.getImageUrl(),
                product.getCategory(),
                true
        );

        Cart.getInstance().addItem(pWithOptions, quantity, selectedOptions);

        closeWindow();
    }

    @FXML private void closeWindow() {
        Stage stage = (Stage) quantityLabel.getScene().getWindow();
        stage.close();
    }
}