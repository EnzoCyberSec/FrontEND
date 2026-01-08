package org.example.demo.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.demo.managers.SceneManager;
import org.example.demo.models.Product;
import org.example.demo.services.OrderApiService;

import java.io.IOException;
import java.util.List;

public class StatsController {

    @FXML private Label nbCommandesLabel;
    @FXML private Label panierMoyenLabel;
    @FXML private VBox bestSellersList;

    private final OrderApiService apiService = new OrderApiService();

    @FXML
    public void initialize() {
        // Lancer le chargement dans un thread séparé
        new Thread(this::loadStats).start();
    }

    private void loadStats() {
        // 1. Appels réseaux (bloquants)
        long nbCommandes = apiService.getNombreCommandes();
        double panierMoyen = apiService.getPanierMoyen();
        List<Product> topPlats = apiService.getTopPlats();

        // 2. Mise à jour de l'interface (Thread JavaFX)
        Platform.runLater(() -> {
            // Affichage des chiffres
            nbCommandesLabel.setText(String.valueOf(nbCommandes));
            panierMoyenLabel.setText(String.format("%.2f €", panierMoyen));

            // Affichage de la liste
            bestSellersList.getChildren().clear();

            if (topPlats.isEmpty()) {
                Label emptyLabel = new Label("Aucune donnée disponible.");
                emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                bestSellersList.getChildren().add(emptyLabel);
            } else {
                int rank = 1;
                for (Product plat : topPlats) {
                    // Format du texte : "1. Nom du Plat - 12.50 €"
                    String text = String.format("#%d  %s  -  %.2f €", rank++, plat.getName(), plat.getPrice());

                    Label l = new Label(text);
                    l.setStyle("-fx-font-size: 16px; -fx-text-fill: #333; -fx-padding: 5; -fx-font-weight: bold;");
                    bestSellersList.getChildren().add(l);
                }
            }
        });
    }

    // ========================
    //       NAVIGATION
    // ========================

    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }

    // Méthodes menu (si utilisées dans le FXML)
    @FXML public void goToAccueil() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
}