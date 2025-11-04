package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import models.Grafo;
import models.Parada;

public class CRUDController {

    @FXML
    private TextField nombreParadaField;

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
    private void eliminarParada() {
        String selected = listViewParadas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Parada p = grafo.getMapa().keySet().stream()
                    .filter(par -> par.getNombre().equals(selected))
                    .findFirst().orElse(null);
            if (p != null) {
                grafo.eliminarParada(p);
                actualizarLista();
            }
        }
    }

    private void actualizarLista() {
        listViewParadas.getItems().clear();
        grafo.getMapa().keySet().forEach(par -> listViewParadas.getItems().add(par.getNombre()));
    }
}
