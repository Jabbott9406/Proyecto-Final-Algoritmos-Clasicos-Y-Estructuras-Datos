package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private void abrirCRUD() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/crud-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("CRUD Grafo");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public  Stage pantalla;

    public void RegistrarParadaClick() {
        abrirCRUD();
    }

    @FXML
    private void salir() {
      pantalla.close();
    }

    public void CerrarAplicacionClick() {
        salir();
    }
}
