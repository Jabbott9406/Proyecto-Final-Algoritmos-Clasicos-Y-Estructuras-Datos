package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import models.Grafo;
import models.Parada;
import models.Ruta;

public class CRUDRutaController {
    @FXML
    private TextField nombreField;

    @FXML
    private TextField inicioField;

    @FXML
    private TextField destinoField;

    @FXML
    private TextField distanciaField;

    @FXML
    private TextField tiempoField;

    @FXML
    private TextField costoField;

    private Grafo grafo = new Grafo();

    @FXML
    private void limpiarCampos() {
        nombreField.clear();
        inicioField.clear();
        destinoField.clear();
        distanciaField.clear();
        tiempoField.clear();
        costoField.clear();
    }

    @FXML
    private void salir() {
        System.exit(0);
    }

}
