package org.example.demo.models;

import java.util.ArrayList;
import java.util.List;

public class CartItem {
    private Product product;
    private int quantity;
    // AJOUT : Liste des options sélectionnées pour cet article
    private List<Option> options;

    public CartItem(Product product, int quantity, List<Option> options) {
        this.product = product;
        this.quantity = quantity;
        this.options = (options != null) ? options : new ArrayList<>();
    }

    // Constructeur simplifié pour compatibilité
    public CartItem(Product product, int quantity) {
        this(product, quantity, new ArrayList<>());
    }

    public Product getProduct() { return product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // AJOUT : Getter pour les options
    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }

    public double getSubtotal() {
        // Le prix du produit inclut déjà le surcoût des options dans la logique actuelle du controller
        return product.getPrice() * quantity;
    }
}