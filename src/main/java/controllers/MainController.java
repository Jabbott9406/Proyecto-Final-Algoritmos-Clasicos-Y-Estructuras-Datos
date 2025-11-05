package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {

    @FXML
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

    private void abrirCRUDRuta() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/registruta-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("CRUD Ruta");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirListadoParadas() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/listadoparadas-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Listado de Paradas");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirListadoRutas() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/listadorutas-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Listado de Rutas");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public  Stage pantalla;

    public void RegistrarParadaClick() {
        abrirCRUDParada();
    }

    public void RegistrarRutaClick(){
        abrirCRUDRuta();
    }
    public  void ListadoParadasClick() {
        abrirListadoParadas();
    }

    public void ListadoRutasClick() {
       abrirListadoRutas();
    }
    @FXML
    private void salir() {
      pantalla.close();
    }

    public void CerrarAplicacionClick() {
        salir();
    }
}
