module com.example.proyectofinal244252 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    // Ikonli (necesario para FontIcon):
    requires org.kordamp.ikonli.core;        // <-- AGREGAR
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;

    // Paquetes abiertos para carga FXML/reflexión de JavaFX
    opens application to javafx.fxml;
    opens controllers to javafx.fxml;
    opens models to javafx.base, javafx.fxml;

    // Opcional: si alguna vez cargas clases del paquete ui desde FXML
    opens ui to javafx.fxml;

    exports application;
    exports controllers; // opcional si otros módulos usan tus controllers
    // exports ui; // solo si otro módulo necesita usar ui públicamente
}