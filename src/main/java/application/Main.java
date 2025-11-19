package application;

import controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Grafo;
import java.awt.*;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Grafo.getInstance().cargarDesdeDB();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/menu-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MainController mainController = fxmlLoader.getController();
        mainController.pantalla = stage;

        stage.setTitle("Menú Principal");
        stage.setWidth(Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 20);
        stage.setHeight(Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 50);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
