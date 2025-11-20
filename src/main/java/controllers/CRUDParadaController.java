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

    private Grafo grafo = Grafo.getInstance();

    // Para edición
    private Parada paradaEnEdicion = null;
    private ListParadaController listParadaController;

    @FXML
    public void initialize() {
        // Inicializa el ComboBox
        tipoParadaBox.setItems(FXCollections.observableArrayList("Tren", "Metro", "Autobus"));
    }

    /**
     * Permite agregar una nueva parada o actualizar una existente.
     */
    @FXML
    private void agregarParada(ActionEvent event) {
        String nombre = nombreParadaField.getText().trim();
        String tipo = tipoParadaBox.getValue();

        if (nombre.isEmpty() || tipo == null) {
            mostrarAlerta("Error", "Debe ingresar nombre y tipo de parada.");
            return;
        }

        if (paradaEnEdicion != null) {
            // Actualizar parada existente
            paradaEnEdicion.setNombre(nombre);
            paradaEnEdicion.setTipo(tipo);

            try {
                DataBase.ParadaDAO.getInstance().actualizarParada(paradaEnEdicion);
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo actualizar la parada en la DB: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else {
            // Crear nueva parada
            Parada nueva = new Parada(nombre, tipo);
            grafo.agregarParada(nueva);

            try {
                DataBase.ParadaDAO.getInstance().guardarParada(nueva);
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo guardar la parada en la DB: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        if (listParadaController != null) {
            listParadaController.refreshTabla();
        }

        limpiarCampos();

        // Cerrar ventana al terminar
        Stage stage = (Stage) nombreParadaField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void limpiarCampos() {
        nombreParadaField.clear();
        tipoParadaBox.getSelectionModel().clearSelection();
        paradaEnEdicion = null; // Resetear edición
    }

    @FXML
    private void salir() {
        Stage stage = (Stage) nombreParadaField.getScene().getWindow();
        stage.close();
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
    }

    public void setListParadaController(ListParadaController controller) {
        this.listParadaController = controller;
    }

    public void cargarParadaParaEdicion(Parada parada) {
        this.paradaEnEdicion = parada;
        nombreParadaField.setText(parada.getNombre());
        tipoParadaBox.setValue(parada.getTipo());
    }

    public static void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
