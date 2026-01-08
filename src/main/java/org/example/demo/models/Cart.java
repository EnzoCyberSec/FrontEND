package org.example.demo.models;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private static Cart instance;
    private List<CartItem> items;

    public static Cart getInstance() {
        if (instance == null) {
            instance = new Cart();
        }
        return instance;
    }

    private Cart() {
        this.items = new ArrayList<>();
    }

    // AJOUT : Nouvelle méthode supportant les options
    public void addItem(Product product, int quantity, List<Option> options) {
        items.add(new CartItem(product, quantity, options));
    }

    // Méthode de compatibilité (si utilisée ailleurs sans options)
    public void addItem(Product product, int quantity) {
        addItem(product, quantity, new ArrayList<>());
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotal() {
        return items.stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public void clear() {
        items.clear();
    }
}