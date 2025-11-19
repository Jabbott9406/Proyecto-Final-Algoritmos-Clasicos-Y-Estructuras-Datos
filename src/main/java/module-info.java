module com.example.proyectofinal244252 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires javafx.graphics;
    requires java.sql;

    opens application to javafx.fxml;
    opens controllers to javafx.fxml;
    opens models to javafx.base, javafx.fxml;



    exports application;
    exports controllers; // opcional
}
