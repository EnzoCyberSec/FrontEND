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

    // ... (Méthodes de stats inchangées : getNombreCommandes, getPanierMoyen, getTopPlats) ...
    // Pour gagner de la place, je ne remets que les méthodes modifiées ci-dessous.
    // Garde tes méthodes getNombreCommandes, getPanierMoyen, getTopPlats telles quelles.

    public long getNombreCommandes() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/stats/commandes/count")).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Double> map = gson.fromJson(response.body(), Map.class);
                return map.get("nombre_commandes").longValue();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public double getPanierMoyen() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/stats/panier-moyen")).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Double> map = gson.fromJson(response.body(), Map.class);
                return map.get("panier_moyen");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    public List<Product> getTopPlats() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/stats/top-plats?limit=5")).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Type listType = new TypeToken<ArrayList<PlatBackendDto>>(){}.getType();
                List<PlatBackendDto> rawList = gson.fromJson(response.body(), listType);
                return rawList.stream().map(dto -> {
                    String catName = (dto.categorie != null && dto.categorie.nom != null) ? dto.categorie.nom : "Plat";
                    return new Product(dto.idPlat, dto.nom, dto.description, dto.prix, "fr/isen/wokandroll/images/logo.jpg", catName);
                }).collect(Collectors.toList());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    // ========================
    //      COMMANDE (MODIFIÉ)
    // ========================

    public static int createCommande(double montantTotal) throws IOException, InterruptedException {
        CommandeRequest requestBody = new CommandeRequest();
        requestBody.setMontantTotal(montantTotal);
        String json = gson.toJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/commandes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) throw new IOException("Erreur HTTP : " + response.statusCode());

        CommandeResponse created = gson.fromJson(response.body(), CommandeResponse.class);
        return created.getIdCommande();
    }

    public static void createLigneCommande(int idCommande, CartItem item) throws IOException, InterruptedException {
        CommandeRef commandeRef = new CommandeRef();
        commandeRef.setIdCommande(idCommande);

        PlatRef platRef = new PlatRef();
        platRef.setIdPlat(item.getProduct().getId());

        LigneCommandeRequest body = new LigneCommandeRequest();
        body.setCommande(commandeRef);
        body.setPlat(platRef);
        body.setQuantite(item.getQuantity());
        body.setPrixUnitaire(item.getProduct().getPrice());

        // AJOUT : On attache les options à la requête
        if (item.getOptions() != null && !item.getOptions().isEmpty()) {
            body.setOptions(item.getOptions());
        }

        String json = gson.toJson(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/lignes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static int createCommandeWithLinesFromCart(Cart cart) throws IOException, InterruptedException {
        int idCommande = createCommande(cart.getTotal());
        for (CartItem item : cart.getItems()) {
            createLigneCommande(idCommande, item);
        }
        return idCommande;
    }

    // ==============================
    //     DTOs INTERNES (Backend)
    // ==============================

    private static class PlatBackendDto {
        public int idPlat;
        public String nom;
        public String description;
        public double prix;
        public CategorieDto categorie;
    }

    private static class CategorieDto {
        public int idCategorie;
        public String nom;
    }

    static class CommandeRequest { private double montantTotal; public void setMontantTotal(double d) {this.montantTotal=d;} }
    static class CommandeResponse { private int idCommande; public int getIdCommande() {return idCommande;} }

    // DTO MODIFIÉ : Ajout du champ options
    static class LigneCommandeRequest {
        private CommandeRef commande;
        private PlatRef plat;
        private int quantite;
        private double prixUnitaire;
        private List<Option> options; // Les options seront envoyées en JSON

        public void setCommande(CommandeRef c) {this.commande=c;}
        public void setPlat(PlatRef p) {this.plat=p;}
        public void setQuantite(int q) {this.quantite=q;}
        public void setPrixUnitaire(double p) {this.prixUnitaire=p;}
        public void setOptions(List<Option> opts) {this.options = opts;}
    }

    static class CommandeRef { private int idCommande; public void setIdCommande(int i) {this.idCommande=i;} }
    static class PlatRef { private int idPlat; public void setIdPlat(int i) {this.idPlat=i;} }
}