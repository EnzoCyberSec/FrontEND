package org.example.demo.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.demo.managers.SceneManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class StatsController {

    @FXML private Label nbCommandesLabel;
    @FXML private Label panierMoyenLabel;
    @FXML private VBox bestSellersList;

    // DTO pour les plats les plus vendus
    public static class BestSellerDto {
        public String nom;
        public int quantite_vendue;
    }

    @FXML
    public void initialize() {
        loadStats();
    }

    private void loadStats() {
        // 1. Charger le nombre de commandes
        fetchNbCommandes();

        // 2. Charger le panier moyen
        fetchPanierMoyen();

        // 3. Charger les plats les plus vendus
        fetchBestSellers();
    }

    private void fetchNbCommandes() {
        try {
            // Appel API Backend (à implémenter avec votre SQL)
            String result = callApi("http://localhost:7001/stats/orders");
            if (result != null) {
                // On suppose que l'API renvoie juste un entier ou un JSON simple
                // Pour simplifier, on affiche le résultat brut s'il est simple
                nbCommandesLabel.setText(result);
            }
        } catch (Exception e) {
            nbCommandesLabel.setText("-");
            System.err.println("Erreur fetchNbCommandes: " + e.getMessage());
        }
    }

    private void fetchPanierMoyen() {
        try {
            String result = callApi("http://localhost:7001/stats/average");
            if (result != null) {
                double avg = Double.parseDouble(result);
                panierMoyenLabel.setText(String.format("%.2f €", avg));
            }
        } catch (Exception e) {
            panierMoyenLabel.setText("-");
            System.err.println("Erreur fetchPanierMoyen: " + e.getMessage());
        }
    }

    private void fetchBestSellers() {
        try {
            String json = callApi("http://localhost:7001/stats/bestsellers");
            if (json != null) {
                Gson gson = new GsonBuilder().create();
                BestSellerDto[] sellers = gson.fromJson(json, BestSellerDto[].class);

                bestSellersList.getChildren().clear();
                if (sellers != null) {
                    for (int i = 0; i < sellers.length; i++) {
                        BestSellerDto bs = sellers[i];
                        Label l = new Label((i + 1) + ". " + bs.nom + " (" + bs.quantite_vendue + " ventes)");
                        l.setStyle("-fx-font-size: 16px; -fx-text-fill: #333; -fx-padding: 5;");
                        bestSellersList.getChildren().add(l);
                    }
                }
            }
        } catch (Exception e) {
            bestSellersList.getChildren().clear();
            bestSellersList.getChildren().add(new Label("Erreur chargement..."));
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour appel HTTP GET
    private String callApi(String urlString) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                return null;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = reader.read()) != -1) {
                    sb.append((char) ch);
                }
                return sb.toString();
            }
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // Navigation
    @FXML public void goBack() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToCart() throws IOException { SceneManager.getInstance().switchScene("cart"); }

    // Liens Sidebar (si réutilisée)
    @FXML public void goToAccueil() throws IOException { SceneManager.getInstance().switchScene("accueil"); }
    @FXML public void goToStarters() throws IOException { SceneManager.getInstance().switchScene("entree"); }
    @FXML public void goToMainDishes() throws IOException { SceneManager.getInstance().switchScene("plats"); }
    @FXML public void goToDesserts() throws IOException { SceneManager.getInstance().switchScene("desserts"); }
    @FXML public void goToDrinks() throws IOException { SceneManager.getInstance().switchScene("boissons"); }
}