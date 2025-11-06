package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import models.Grafo;
import models.Parada;
import models.Ruta;

public class CRUDRutaController {

    @FXML
    private TextField nombreField, distanciaField, tiempoField, costoField;

    @FXML
    private ComboBox<Parada> inicioCombo, destinoCombo;

    private Grafo grafo;

    private ObservableList<Parada> listaParadas = FXCollections.observableArrayList();

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;

        if (grafo != null) {
            listaParadas.setAll(grafo.getParadas());
            inicioCombo.setItems(listaParadas);
            destinoCombo.setItems(listaParadas);
        }
    }
    private ListRutaController listRutaController;

    public void setListRutaController(ListRutaController controller) {
        this.listRutaController = controller;
    }

    @FXML
    private void registrarRuta() {
        try {
            String nombre = nombreField.getText();
            Parada inicio = inicioCombo.getValue();
            Parada destino = destinoCombo.getValue();
            double distancia = Double.parseDouble(distanciaField.getText());
            double tiempo = Double.parseDouble(tiempoField.getText());
            double costo = Double.parseDouble(costoField.getText());

            if (nombre.isEmpty() || inicio == null || destino == null) {
                mostrarAlerta("Error", "Por favor, completa todos los campos obligatorios.");
                return;
            }

            Ruta nuevaRuta = grafo.agregarRuta(nombre, inicio, destino, distancia, tiempo, costo);
            mostrarAlerta("Éxito", "Ruta registrada correctamente:\n" + nuevaRuta);

            if (listRutaController != null) {
                listRutaController.getListaRutas().add(nuevaRuta);
            }
            limpiarCampos();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Distancia, tiempo y costo deben ser numéricos.");
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un problema:\n" + e.getMessage());
        }
    }

    @FXML
    private void limpiarCampos() {
        nombreField.clear();
        inicioCombo.getSelectionModel().clearSelection();
        destinoCombo.getSelectionModel().clearSelection();
        distanciaField.clear();
        tiempoField.clear();
        costoField.clear();
    }

    @FXML
    private void cancelar() {
        Stage stage = (Stage) nombreField.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

