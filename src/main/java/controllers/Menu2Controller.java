package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Menu2Controller {

    private void abrirCRUDParada() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/registparada-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("CRUD Grafo");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RegistrarParadaClick() {
        abrirCRUDParada();
    }
    public  Stage pantalla;

}
