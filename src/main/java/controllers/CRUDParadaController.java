
package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Grafo;
import models.Parada;

public class CRUDParadaController {

    @FXML
    private TextField nombreParadaField;
    //CREAR UNO PARA TIPO Y YA SI QUIERES OTRO PARA DESCRIPCION

    @FXML
    private ListView<String> listViewParadas;

    private Grafo grafo = new Grafo();

    @FXML
    private void agregarParada() {
        String nombre = nombreParadaField.getText().trim();
        if (!nombre.isEmpty()) {
            Parada nueva = new Parada(nombre);
            grafo.agregarParada(nueva);
            actualizarLista();
            nombreParadaField.clear();
        }
    }

    @FXML
    private void actualizarLista() {
        listViewParadas.getItems().clear();
        grafo.getMapa().keySet().forEach(par -> listViewParadas.getItems().add(par.getNombre()));
    }
    @FXML
    private void limpiarCampos() {
        nombreParadaField.clear();
    }

    @FXML
    private void salir() {
        Stage stage = (Stage) nombreParadaField.getScene().getWindow();
        stage.close();
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
    }

}
