package org.example.demo.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.demo.models.Cart;
import org.example.demo.models.CartItem;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service qui communique avec l'API Javalin WokAndRoll
 * pour créer une Commande et ses LignesCommande à partir du panier.
 */
public class OrderApiService {

    // Ton serveur Javalin tourne sur ce port, sans préfixe /api
    private static final String BASE_URL = "http://localhost:7001";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();

    // ========================
    //      COMMANDE
    // ========================

    /**
     * Crée une commande via POST /commandes.
     * Envoie {"montantTotal": X}
     * et récupère l'idCommande renvoyé par l'API.
     */
    public static int createCommande(double montantTotal) throws IOException, InterruptedException {
        CommandeRequest requestBody = new CommandeRequest();
        requestBody.setMontantTotal(montantTotal);

        String json = gson.toJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/commandes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IOException("Erreur HTTP lors de la création de la commande : "
                    + response.statusCode() + " - " + response.body());
        }

        // L'API renvoie un objet Commande JSON (idCommande, dateCommande, montantTotal, ...)
        CommandeResponse created = gson.fromJson(response.body(), CommandeResponse.class);
        return created.getIdCommande();
    }

    // ========================
    //    LIGNE COMMANDE
    // ========================

    /**
     * Crée une ligne de commande pour un article donné.
     * Envoie un JSON du type :
     * {
     *   "commande": { "idCommande": ... },
     *   "plat": { "idPlat": ... },
     *   "quantite": ...,
     *   "prixUnitaire": ...
     * }
     */
    public static void createLigneCommande(int idCommande, CartItem item)
            throws IOException, InterruptedException {

        // Référence à la commande
        CommandeRef commandeRef = new CommandeRef();
        commandeRef.setIdCommande(idCommande);

        // Référence au plat : on utilise l'id du Product du front
        // qui correspond à l'id du Plat dans ton backend.
        PlatRef platRef = new PlatRef();
        platRef.setIdPlat(item.getProduct().getId());

        LigneCommandeRequest body = new LigneCommandeRequest();
        body.setCommande(commandeRef);
        body.setPlat(platRef);
        body.setQuantite(item.getQuantity());
        body.setPrixUnitaire(item.getProduct().getPrice());
        // options peut rester null

        String json = gson.toJson(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/lignes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IOException("Erreur HTTP lors de la création d'une ligne commande : "
                    + response.statusCode() + " - " + response.body());
        }
    }

    // ==============================
    // COMMANDE + LIGNES DU PANIER
    // ==============================

    /**
     * Crée une commande avec le montant total du panier,
     * puis crée toutes les lignes de commande correspondantes.
     * Retourne l'id de la commande créée.
     */
    public static int createCommandeWithLinesFromCart(Cart cart)
            throws IOException, InterruptedException {

        int idCommande = createCommande(cart.getTotal());

        for (CartItem item : cart.getItems()) {
            createLigneCommande(idCommande, item);
        }

        return idCommande;
    }

    // ==============================
    //          DTOs internes
    // ==============================

    // Ce qu'on envoie à POST /commandes
    static class CommandeRequest {
        private double montantTotal;

        public double getMontantTotal() {
            return montantTotal;
        }

        public void setMontantTotal(double montantTotal) {
            this.montantTotal = montantTotal;
        }
    }

    // Ce qu'on lit en retour de POST /commandes
    static class CommandeResponse {
        private int idCommande;
        private String dateCommande;
        private double montantTotal;

        public int getIdCommande() {
            return idCommande;
        }

        public void setIdCommande(int idCommande) {
            this.idCommande = idCommande;
        }

        public String getDateCommande() {
            return dateCommande;
        }

        public void setDateCommande(String dateCommande) {
            this.dateCommande = dateCommande;
        }

        public double getMontantTotal() {
            return montantTotal;
        }

        public void setMontantTotal(double montantTotal) {
            this.montantTotal = montantTotal;
        }
    }

    // Ce qu'on envoie à POST /lignes
    static class LigneCommandeRequest {
        private CommandeRef commande;
        private PlatRef plat;
        private int quantite;
        private double prixUnitaire;
        // private OptionRef[] options; // si tu veux gérer les options plus tard

        public CommandeRef getCommande() {
            return commande;
        }

        public void setCommande(CommandeRef commande) {
            this.commande = commande;
        }

        public PlatRef getPlat() {
            return plat;
        }

        public void setPlat(PlatRef plat) {
            this.plat = plat;
        }

        public int getQuantite() {
            return quantite;
        }

        public void setQuantite(int quantite) {
            this.quantite = quantite;
        }

        public double getPrixUnitaire() {
            return prixUnitaire;
        }

        public void setPrixUnitaire(double prixUnitaire) {
            this.prixUnitaire = prixUnitaire;
        }
    }

    // Référence à Commande (correspond à fr.isen.wokandroll.model.Commande)
    static class CommandeRef {
        private int idCommande;

        public int getIdCommande() {
            return idCommande;
        }

        public void setIdCommande(int idCommande) {
            this.idCommande = idCommande;
        }
    }

    // Référence à Plat (correspond à fr.isen.wokandroll.model.Plat)
    static class PlatRef {
        // ⚠ Doit correspondre au champ dans ta classe Plat (souvent idPlat)
        private int idPlat;

        public int getIdPlat() {
            return idPlat;
        }

        public void setIdPlat(int idPlat) {
            this.idPlat = idPlat;
        }
    }
}
