package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import models.Grafo;
import models.Ruta;
import DataBase.RutaDAO;

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
            mostrarAlerta("Error", "Selecciona una ruta para eliminar.");
            return;
        }

        // Confirmación antes de eliminar
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la ruta \"" + seleccionada.getNombre() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                // Eliminar de grafo en memoria
                grafo.eliminarRuta(seleccionada);

                // Eliminar de base de datos
                RutaDAO.getInstance().eliminarRuta(seleccionada.getId());

                // Actualizar tabla
                listaRutas.remove(seleccionada);

                // Mostrar alerta de éxito
                mostrarAlerta("Éxito", "Ruta eliminada correctamente.");
            }
        });
    }

    @FXML
    private void modificarRutaSeleccionada() {
        Ruta seleccionada = tableRutas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Error", "Selecciona una ruta para modificar.");
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
            mostrarAlerta("Error", "No se pudo abrir el formulario de edición:\n" + e.getMessage());
        }
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        if (grafo != null) {
            // Cargar rutas desde memoria (Grafo ya debería cargar desde DB)
            listaRutas.setAll(grafo.getRutas());
        }
    }

    public ObservableList<Ruta> getListaRutas() {
        return listaRutas;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
