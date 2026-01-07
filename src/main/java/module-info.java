module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;

    opens org.example.demo to javafx.fxml;
    opens org.example.demo.controllers to javafx.fxml, com.google.gson;

    exports org.example.demo;
    exports org.example.demo.controllers;
}
