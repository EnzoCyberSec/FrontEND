package fr.isen.wokandroll.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.isen.wokandroll.models.Cart;
import fr.isen.wokandroll.models.CartItem;
import fr.isen.wokandroll.models.Option;
import fr.isen.wokandroll.models.Product;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderApiService {

    private static final String BASE_URL = "http://localhost:7001";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new GsonBuilder().create();

    // ========================
    //      STATISTIQUES (GET)
    // ========================

    public long getNombreCommandes() {
        try {
            Map<String, Double> map = sendRequest("/stats/commandes/count", "GET", null, new TypeToken<Map<String, Double>>(){}.getType());
            return map != null ? map.get("nombre_commandes").longValue() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double getPanierMoyen() {
        try {
            Map<String, Double> map = sendRequest("/stats/panier-moyen", "GET", null, new TypeToken<Map<String, Double>>(){}.getType());
            return map != null ? map.get("panier_moyen") : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public List<Product> getTopPlats() {
        try {
            List<PlatBackendDto> rawList = sendRequest("/stats/top-plats?limit=5", "GET", null, new TypeToken<ArrayList<PlatBackendDto>>(){}.getType());
            if (rawList == null) return new ArrayList<>();

            return rawList.stream().map(dto -> {
                String catName = (dto.categorie != null && dto.categorie.nom != null) ? dto.categorie.nom : "Plat";
                return new Product(dto.idPlat, dto.nom, dto.description, dto.prix, "fr/isen/wokandroll/images/logo.jpg", catName);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ========================
    //      COMMANDE (POST)
    // ========================

    public static int createCommande(double montantTotal) throws IOException, InterruptedException {
        CommandeRequest requestBody = new CommandeRequest();
        requestBody.setMontantTotal(montantTotal);

        CommandeResponse response = sendRequest("/commandes", "POST", requestBody, CommandeResponse.class);
        return response.getIdCommande();
    }

    public static void createLigneCommande(int idCommande, CartItem item) throws IOException, InterruptedException {
        LigneCommandeRequest body = new LigneCommandeRequest();
        body.setCommande(new CommandeRef(idCommande));
        body.setPlat(new PlatRef(item.getProduct().getId()));
        body.setQuantite(item.getQuantity());
        body.setPrixUnitaire(item.getProduct().getPrice());

        if (item.getOptions() != null && !item.getOptions().isEmpty()) {
            body.setOptions(item.getOptions());
        }

        sendRequest("/lignes", "POST", body, null);
    }

    public static int createCommandeWithLinesFromCart(Cart cart) throws IOException, InterruptedException {
        int idCommande = createCommande(cart.getTotal());
        for (CartItem item : cart.getItems()) {
            createLigneCommande(idCommande, item);
        }
        return idCommande;
    }

    // ========================
    //    MÉTHODE GÉNÉRIQUE
    // ========================

    /**
     * Méthode centrale pour exécuter les requêtes HTTP.
     * @param endpoint Le chemin (ex: "/commandes")
     * @param method "GET" ou "POST"
     * @param body L'objet à envoyer (null pour GET)
     * @param responseType Le type de retour attendu pour GSON (null si on ignore la réponse)
     */
    private static <T> T sendRequest(String endpoint, String method, Object body, Type responseType) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint));

        if ("POST".equalsIgnoreCase(method)) {
            String jsonBody = gson.toJson(body);
            builder.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            builder.GET();
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        // Vérification basique du statut 2xx
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Erreur HTTP : " + response.statusCode() + " Body: " + response.body());
        }

        if (responseType != null && response.body() != null && !response.body().isEmpty()) {
            return gson.fromJson(response.body(), responseType);
        }
        return null;
    }

    // ==============================
    //     DTOs INTERNES
    // ==============================

    private static class PlatBackendDto {
        int idPlat; String nom; String description; double prix; CategorieDto categorie;
    }
    private static class CategorieDto { int idCategorie; String nom; }

    static class CommandeRequest {
        private double montantTotal;
        void setMontantTotal(double d) {this.montantTotal=d;}
    }
    static class CommandeResponse {
        private int idCommande;
        int getIdCommande() {return idCommande;}
    }

    static class LigneCommandeRequest {
        private CommandeRef commande;
        private PlatRef plat;
        private int quantite;
        private double prixUnitaire;
        private List<Option> options;

        void setCommande(CommandeRef c) {this.commande=c;}
        void setPlat(PlatRef p) {this.plat=p;}
        void setQuantite(int q) {this.quantite=q;}
        void setPrixUnitaire(double p) {this.prixUnitaire=p;}
        void setOptions(List<Option> opts) {this.options = opts;}
    }

    static class CommandeRef {
        private int idCommande;
        CommandeRef(int id) { this.idCommande = id; }
    }
    static class PlatRef {
        private int idPlat;
        PlatRef(int id) { this.idPlat = id; }
    }
}