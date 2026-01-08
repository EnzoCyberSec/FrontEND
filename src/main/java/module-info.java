module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;
    requires java.net.http;

    // Autorise JavaFX à lire les fichiers FXML
    opens org.example.demo to javafx.fxml;

    // Autorise JavaFX et Gson à accéder aux contrôleurs
    opens org.example.demo.controllers to javafx.fxml, com.google.gson;

    // Autorise Gson à accéder aux services (API)
    opens org.example.demo.services to com.google.gson;

    // === CORRECTION ICI ===
    // Autorise Gson à accéder aux modèles (Option, Product, CartItem)
    // pour remplir les données reçues du Backend
    opens org.example.demo.models to com.google.gson, javafx.base;

    exports org.example.demo;
    exports org.example.demo.controllers;
    // Si besoin d'exporter les modèles pour d'autres modules
    exports org.example.demo.models;
}