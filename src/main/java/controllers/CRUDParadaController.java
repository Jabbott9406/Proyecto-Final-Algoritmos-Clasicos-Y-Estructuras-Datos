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
    private TextField nombreParadaField;

    @FXML
    private ComboBox<String> tipoParadaBox;

    @FXML
    private ListView<String> listViewParadas;

    private Grafo grafo = Grafo.getInstance();

    @FXML
    public void initialize() {
        // Inicializa el ComboBox
        tipoParadaBox.setItems(FXCollections.observableArrayList("Tren", "Metro", "Autobus"));
        actualizarLista();
    }

    @FXML
    private void agregarParada(ActionEvent event) {
        String nombre = nombreParadaField.getText().trim();
        String tipo = tipoParadaBox.getValue();

        if (!nombre.isEmpty() && tipo != null) {
            Parada nueva = new Parada(nombre, tipo);

            // Agregar al grafo en memoria
            grafo.agregarParada(nueva);

            // Guardar en la base de datos
            try {
                DataBase.ParadaDAO.getInstance().guardarParada(nueva);
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo guardar la parada en la DB: " + e.getMessage());
                e.printStackTrace();
            }

            actualizarLista();
            limpiarCampos();
            mostrarAlerta("Éxito", "Parada agregada correctamente.");
        } else {
            mostrarAlerta("Error", "Debe ingresar nombre y tipo de parada.");
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
        tipoParadaBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void salir() {
        Stage stage = (Stage) nombreParadaField.getScene().getWindow();
        stage.close();
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        actualizarLista();
    }

    public static void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
