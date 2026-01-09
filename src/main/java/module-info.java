module fr.isen.wokandroll {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;
    requires java.net.http;

    // Autorise JavaFX à lire les fichiers FXML
    opens fr.isen.wokandroll to javafx.fxml;

    // Autorise JavaFX et Gson à accéder aux contrôleurs
    opens fr.isen.wokandroll.controllers to javafx.fxml, com.google.gson;

    // Autorise Gson à accéder aux services (API)
    opens fr.isen.wokandroll.services to com.google.gson;

    // === CORRECTION ICI ===
    // Autorise Gson à accéder aux modèles (Option, Product, CartItem)
    // pour remplir les données reçues du Backend
    opens fr.isen.wokandroll.models to com.google.gson, javafx.base;

    exports fr.isen.wokandroll;
    exports fr.isen.wokandroll.controllers;
    // Si besoin d'exporter les modèles pour d'autres modules
    exports fr.isen.wokandroll.models;
}