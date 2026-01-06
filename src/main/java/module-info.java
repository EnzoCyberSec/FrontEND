module org.example.frontend {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.frontend to javafx.fxml;
    exports org.example.frontend;
}