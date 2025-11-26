package application;

import controllers.Menu2Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Grafo;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Cargar datos desde la base de datos ANTES de cargar la interfaz
        Grafo.getInstance().cargarDesdeDB();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/Menu2.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Aplicar CSS
        scene.getStylesheets().add(getClass().getResource("/css/Style.css").toExternalForm());

        // Conectar pantalla al controlador
        Menu2Controller mainController = fxmlLoader.getController();
        mainController.pantalla = stage;

        stage.setTitle("Menú Principal");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
