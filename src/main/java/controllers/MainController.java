package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Grafo;

public class MainController {

    // Usamos el singleton de Grafo
    private Grafo grafo = Grafo.getInstance();

    public Stage pantalla;

    @FXML
    private void abrirCRUDParada() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/RegistrarParada.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("CRUD Parada");
            stage.setScene(scene);

            CRUDParadaController controller = fxmlLoader.getController();
            controller.setGrafo(grafo);  // pasa la instancia singleton

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirCRUDRuta() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/registruta-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("CRUD Ruta");
            stage.setScene(scene);

            CRUDRutaController controller = fxmlLoader.getController();
            controller.setGrafo(grafo);  // pasa la instancia singleton

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirListadoParadas() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/listParada-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Listado de Paradas");
            stage.setScene(scene);

            ListParadaController controller = fxmlLoader.getController();
            controller.setGrafo(grafo);  // pasa la instancia singleton

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirListadoRutas() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/listRuta-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Listado de Rutas");
            stage.setScene(scene);

            ListRutaController controller = fxmlLoader.getController();
            controller.setGrafo(grafo);  // pasa la instancia singleton

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RegistrarParadaClick() {
        abrirCRUDParada();
    }

    public void RegistrarRutaClick() {
        abrirCRUDRuta();
    }

    public void ListadoParadasClick() {
        abrirListadoParadas();
    }

    public void ListadoRutasClick() {
        abrirListadoRutas();
    }

    @FXML
    private void salir() {
        if (pantalla != null) {
            pantalla.close();
        }
    }

    public void CerrarAplicacionClick() {
        salir();
    }
}
