package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Grafo;
import models.Parada;

import java.util.Optional;

public class ListParadaController {

    @FXML
    private TableView<Parada> tableParadas;
    @FXML
    private TableColumn<Parada, String> nombreColumn;
    @FXML
    private TableColumn<Parada, String> tipoColumn;

    private Grafo grafo = Grafo.getInstance();
    private ObservableList<Parada> listaParadas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nombreColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));
        tipoColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTipo()));

        if (grafo != null) {
            listaParadas.setAll(grafo.getParadas());
        }

        tableParadas.setItems(listaParadas);
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        refreshTabla();
    }

    public ObservableList<Parada> getListaParadas() {
        return listaParadas;
    }

    @FXML
    void eliminarParada() {
        Parada seleccionado = tableParadas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona una parada.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la parada \"" + seleccionado.getNombre() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            grafo.eliminarParada(seleccionado);
            listaParadas.remove(seleccionado);

            try {
                DataBase.ParadaDAO.getInstance().eliminarParada(seleccionado.getId());
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error al eliminar de la DB: " + e.getMessage()).showAndWait();
                e.printStackTrace();
            }
        }
    }

    @FXML
    void modificarParada() {
        Parada seleccionado = tableParadas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona una parada.").showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/RegistrarParada.fxml"));
            Parent root = loader.load();

            CRUDParadaController crudController = loader.getController();
            crudController.setGrafo(grafo);
            crudController.setListParadaController(this);
            crudController.cargarParadaParaEdicion(seleccionado);

            Stage stage = new Stage();
            stage.setTitle("Modificar Parada");
            stage.setScene(new Scene(root));
            stage.showAndWait();


            refreshTabla();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir el formulario de edición:\n" + e.getMessage()).showAndWait();
        }
    }

    public void refreshTabla() {
        if (grafo != null) {
            listaParadas.setAll(grafo.getParadas());
            tableParadas.refresh();
        }
    }
}
