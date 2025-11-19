package controllers;

import DataBase.RutaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import models.Grafo;
import models.Parada;
import models.Ruta;

public class CRUDRutaController {

    @FXML
    private TextField nombreField;

    @FXML
    private TextField distanciaField;

    @FXML
    private TextField tiempoField;

    @FXML
    private TextField costoField;

    @FXML
    private ComboBox<Parada> inicioCombo;

    @FXML
    private ComboBox<Parada> destinoCombo;

    private Grafo grafo;
    private Ruta rutaEnEdicion = null;
    private ObservableList<Parada> listaParadas = FXCollections.observableArrayList();
    private ListRutaController listRutaController;

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        if (grafo != null) {
            this.listaParadas.setAll(grafo.getParadas());
            this.inicioCombo.setItems(this.listaParadas);
            this.destinoCombo.setItems(this.listaParadas);
        }
    }

    public void setListRutaController(ListRutaController controller) {
        this.listRutaController = controller;
    }

    @FXML
    private void registrarRuta() {
        try {
            String nombre = nombreField.getText().trim();
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
                // Actualizar en memoria
                grafo.modificarRuta(rutaEnEdicion, nombre, inicio, destino, distancia, tiempo, costo);

                // Actualizar en DB
                RutaDAO.getInstance().actualizarRuta(rutaEnEdicion);

                mostrarAlerta("Éxito", "Ruta modificada correctamente.");
                if (listRutaController != null) {
                    int index = listRutaController.getListaRutas().indexOf(rutaEnEdicion);
                    if (index >= 0) {
                        listRutaController.getListaRutas().set(index, rutaEnEdicion);
                    }
                }
            } else {
                // Crear nueva ruta en memoria
                Ruta nuevaRuta = grafo.agregarRuta(nombre, inicio, destino, distancia, tiempo, costo);

                // Guardar en DB
                RutaDAO.getInstance().guardarRuta(nuevaRuta);

                mostrarAlerta("Éxito", "Ruta registrada correctamente:\n" + nuevaRuta);

                if (listRutaController != null) {
                    listRutaController.getListaRutas().add(nuevaRuta);
                }
            }

            limpiarCampos();
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Distancia, tiempo y costo deben ser numéricos.");
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un problema:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarRuta() {
        if (rutaEnEdicion == null) {
            mostrarAlerta("Error", "Selecciona una ruta para eliminar.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "¿Eliminar la ruta \"" + rutaEnEdicion.getNombre() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                // Eliminar de memoria
                grafo.eliminarRuta(rutaEnEdicion);

                // Eliminar de DB
                RutaDAO.getInstance().eliminarRuta(rutaEnEdicion.getId());

                // Eliminar de UI
                if (listRutaController != null) {
                    listRutaController.getListaRutas().remove(rutaEnEdicion);
                }

                limpiarCampos();
                mostrarAlerta("Éxito", "Ruta eliminada correctamente.");
            }
        });
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
        Alert alert = new Alert(AlertType.INFORMATION);
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
