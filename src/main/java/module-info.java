module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens org.example.demo to javafx.fxml;
    opens org.example.demo.controllers to javafx.fxml;

    exports org.example.demo;
    exports org.example.demo.controllers;
}
