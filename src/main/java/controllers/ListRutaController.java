
package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import models.Grafo;
import models.Ruta;

public class ListRutaController {

    @FXML
    private TableView<Ruta> tableRutas;

    @FXML
    private TableColumn<Ruta, String> nombreColumn;
    @FXML
    private TableColumn<Ruta, String> inicioColumn;
    @FXML
    private TableColumn<Ruta, String> destinoColumn;
    @FXML
    private TableColumn<Ruta, Double> distanciaColumn;
    @FXML
    private TableColumn<Ruta, Double> tiempoColumn;
    @FXML
    private TableColumn<Ruta, Double> costoColumn;

    private Grafo grafo;
    private ObservableList<Ruta> listaRutas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        nombreColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombre())
        );

        distanciaColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getDistancia()).asObject()
        );

        tiempoColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getTiempo()).asObject()
        );

        costoColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getCosto()).asObject()
        );

        inicioColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getInicio() != null ? cellData.getValue().getInicio().getNombre() : ""
                )
        );

        destinoColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDestino() != null ? cellData.getValue().getDestino().getNombre() : ""
                )
        );

        tableRutas.setItems(listaRutas);
    }

    @FXML
    private void eliminarRutaSeleccionada() {
        Ruta seleccionada = tableRutas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            CRUDRutaController.mostrarAlerta("Error", "Selecciona una ruta para eliminar.");
            return;
        }
        grafo.eliminarRuta(seleccionada);
        listaRutas.remove(seleccionada);
        CRUDRutaController.mostrarAlerta("Éxito", "Ruta eliminada correctamente.");
    }

    @FXML
    private void modificarRutaSeleccionada() {
        Ruta seleccionada = tableRutas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            CRUDRutaController.mostrarAlerta("Error", "Selecciona una ruta para modificar.");
            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/registRuta-view.fxml"));
            Parent root = loader.load();

            CRUDRutaController crudController = loader.getController();
            crudController.setGrafo(grafo);
            crudController.setListRutaController(this);
            crudController.cargarRutaParaEdicion(seleccionada);

            Stage stage = new Stage();
            stage.setTitle("Modificar Ruta");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            CRUDRutaController.mostrarAlerta("Error", "No se pudo abrir el formulario de edición:\n" + e.getMessage());
        }
    }


    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        if (grafo != null) {
            listaRutas.setAll(grafo.getRutas());
        }
    }

    public ObservableList<Ruta> getListaRutas() {
        return listaRutas;
    }
}
