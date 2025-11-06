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

    private Ruta rutaEnEdicion = null;

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

            if (rutaEnEdicion != null) {
                rutaEnEdicion.setNombre(nombre);
                rutaEnEdicion.setInicio(inicio);
                rutaEnEdicion.setDestino(destino);
                rutaEnEdicion.setDistancia(distancia);
                rutaEnEdicion.setTiempo(tiempo);
                rutaEnEdicion.setCosto(costo);

                mostrarAlerta("Éxito", "Ruta modificada correctamente.");

                if (listRutaController != null) {
                    int index = listRutaController.getListaRutas().indexOf(rutaEnEdicion);
                    if (index >= 0) {
                        listRutaController.getListaRutas().set(index, rutaEnEdicion);
                    }
                }

            } else {
                Ruta nuevaRuta = grafo.agregarRuta(nombre, inicio, destino, distancia, tiempo, costo);
                mostrarAlerta("Éxito", "Ruta registrada correctamente:\n" + nuevaRuta);

                if (listRutaController != null) {
                    listRutaController.getListaRutas().add(nuevaRuta);
                }
            }

            limpiarCampos();
            rutaEnEdicion = null;

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

        rutaEnEdicion = null;
    }

    @FXML
    private void cancelar() {
        Stage stage = (Stage) nombreField.getScene().getWindow();
        stage.close();
    }


    public static void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void cargarRutaParaEdicion(Ruta ruta) {
        if (ruta != null) {
            rutaEnEdicion = ruta;
            nombreField.setText(ruta.getNombre());
            inicioCombo.setValue(ruta.getInicio());
            destinoCombo.setValue(ruta.getDestino());
            distanciaField.setText(String.valueOf(ruta.getDistancia()));
            tiempoField.setText(String.valueOf(ruta.getTiempo()));
            costoField.setText(String.valueOf(ruta.getCosto()));

        }
    }

}

