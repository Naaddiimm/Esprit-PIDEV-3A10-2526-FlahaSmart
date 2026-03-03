module com.example.flahasmarty {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;

    // Java SQL module (built-in)
    requires java.sql;
    requires java.net.http;
    requires mysql.connector.j;

    // Open ALL packages to JavaFX for reflection
    opens com.example.flahasmarty to javafx.fxml;
    opens com.example.flahasmarty.controllers to javafx.fxml;
    opens com.example.flahasmarty.entities to javafx.base, javafx.fxml;
    opens com.example.flahasmarty.services to javafx.fxml;      // Now includes ConsultantBot
    opens com.example.flahasmarty.utilies to javafx.fxml;     // New utilities package

    // Export packages that need to be accessed by other modules
    exports com.example.flahasmarty;
    exports com.example.flahasmarty.controllers;
    exports com.example.flahasmarty.entities;
    exports com.example.flahasmarty.services;                   // Now includes ConsultantBot
    exports com.example.flahasmarty.utilies;                  // New utilities package
}