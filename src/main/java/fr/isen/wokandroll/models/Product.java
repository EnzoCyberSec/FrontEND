package fr.isen.wokandroll.models;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String category;
    // NOUVEAU CHAMP
    private boolean available;

    public Product(int id, String name, String description, double price, String imageUrl, String category, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.available = available;
    }

    public Product(int id, String name, String description, double price, String imageUrl, String category) {
        this(id, name, description, price, imageUrl, category, true);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }

    public boolean isAvailable() { return available; }
}