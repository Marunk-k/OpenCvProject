module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;
    requires java.desktop;


    opens org.example.demo to javafx.fxml;
    exports org.example.demo;
    exports org.example.demo.Controllers;
    opens org.example.demo.Controllers to javafx.fxml;
}