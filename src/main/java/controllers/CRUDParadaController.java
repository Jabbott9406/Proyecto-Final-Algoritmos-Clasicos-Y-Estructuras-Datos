package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Grafo;
import models.Parada;

public class CRUDParadaController {
    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnRegistro;

    @FXML
    private TextField nombreParadaField;

    @FXML
    private ComboBox<String> tipoParadaBox;

    private Grafo grafo = Grafo.getInstance();
    private ListView<Parada> listViewParadas;

    @FXML
    public ComboBox<String> getTipoParadaBox() {

        tipoParadaBox.setItems(FXCollections.observableArrayList("Tren", "Metro", "Autobus"));
        return tipoParadaBox;
    }

    @FXML
    private void agregarParada(ActionEvent event) {
        String nombre = nombreParadaField.getText().trim();
        Parada aux = new Parada(nombre, tipoParadaBox.getValue());
        if (!nombre.isEmpty()) {
            grafo.agregarParada(aux);
           // actualizarLista();
            nombreParadaField.clear();
            tipoParadaBox.getItems().clear();
        }
    }



    @FXML
    private void salir() {
        Stage stage = (Stage) nombreParadaField.getScene().getWindow();
        stage.close();
    }

//    @FXML
//    private void initialize() {
//        listViewParadas.setItems(grafo.getParadas());
//
//        // Muestra solo el nombre en cada celda (si Parada tiene getNombre())
//        listViewParadas.setCellFactory(lv -> new ListCell<>() {
//            @Override
//            protected void updateItem(Parada p, boolean empty) {
//                super.updateItem(p, empty);
//                setText(empty || p == null ? null : p.getNombre());
//            }
//        });
//    }


//    @FXML
//    private TextField nombreParadaField;
//    //CREAR UNO PARA TIPO Y YA SI QUIERES OTRO PARA DESCRIPCION
//
//    @FXML
//    private ListView<String> listViewParadas;
//
//    private Grafo grafo = new Grafo();
//
//    @FXML
//    private void agregarParada() {
//        String nombre = nombreParadaField.getText().trim();
//        if (!nombre.isEmpty()) {
//            Parada nueva = new Parada(nombre);
//            grafo.agregarParada(nueva);
//            actualizarLista();
//            nombreParadaField.clear();
//        }
//    }
//
//    @FXML
//    private void actualizarLista() {
//        listViewParadas.getItems().clear();
//        grafo.getMapa().keySet().forEach(par -> listViewParadas.getItems().add(par.getNombre()));
//    }
//    @FXML
//    private void limpiarCampos() {
//        nombreParadaField.clear();
//    }
//
//    @FXML
//    private void salir() {
//        Stage stage = (Stage) nombreParadaField.getScene().getWindow();
//        stage.close();
//    }

}

