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
    // Lista observable: con esto la tabla se actualiza sola cuando cambiamos el contenido
    private ObservableList<Parada> listaParadas = FXCollections.observableArrayList();

    /**
     * initialize
     * Objetivo: preparar la tabla, enlazar columnas y cargar los datos iniciales.
     * Retorno: ninguno.
     */
    @FXML
    public void initialize() {
        // Enlazamos las columnas a las propiedades de Parada.
        nombreColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));
        tipoColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTipo()));

        // Si el grafo ya tiene paradas, las traemos y las metemos a la lista observable.
        if (grafo != null) {
            listaParadas.setAll(grafo.getParadas());
        }

        // Conectar la tabla con la lista para que se vea todo.
        tableParadas.setItems(listaParadas);
    }

    /**
     * setGrafo
     * Objetivo: inyectar el grafo y refrescar la tabla con su contenido.
     * Retorno: ninguno.
     */
    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        refreshTabla();
    }

    /**
     * getListaParadas
     * Objetivo: exponer la lista observable (útil si otro controlador quiere leerla).
     * Retorno: ObservableList de Parada.
     */
    public ObservableList<Parada> getListaParadas() {
        return listaParadas;
    }

    /**
     * eliminarParada
     * Objetivo: eliminar la parada seleccionada del grafo y la base de datos.
     * Retorno: ninguno.
     */
    @FXML
    void eliminarParada() {
        // Tomamos la selección actual de la tabla.
        Parada seleccionado = tableParadas.getSelectionModel().getSelectedItem();

        // Si no hay nada seleccionado, avisamos.
        if (seleccionado == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona una parada.").showAndWait();
            return;
        }

        // Confirmación para no borrar por error.
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la parada \"" + seleccionado.getNombre() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        Optional<ButtonType> res = confirm.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.OK) {
            // Quitamos del grafo y de la lista que pinta la tabla.
            grafo.eliminarParada(seleccionado);
            listaParadas.remove(seleccionado);

            // Intentamos eliminar en la DB también.
            try {
                DataBase.ParadaDAO.getInstance().eliminarParada(seleccionado.getId());
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error al eliminar de la DB: " + e.getMessage()).showAndWait();
                e.printStackTrace();
            }
        }
    }

    /**
     * modificarParada
     * Objetivo: abrir el formulario en modo edición con la parada seleccionada y actualizar al volver.
     * Retorno: ninguno.
     */
    @FXML
    void modificarParada() {
        // Necesitamos una selección para editar.
        Parada seleccionado = tableParadas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona una parada.").showAndWait();
            return;
        }

        try {
            // Cargamos el formulario de registro.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/RegistrarParada.fxml"));
            Parent root = loader.load();

            // Preparamos el controlador del formulario con el grafo y referencia a este listado.
            CRUDParadaController crudController = loader.getController();
            crudController.setGrafo(grafo);
            crudController.setListParadaController(this);

            // Cargamos la parada para edición (esto llena los campos del formulario).
            crudController.cargarParadaParaEdicion(seleccionado);

            // Mostramos la ventana de edición.
            Stage stage = new Stage();
            stage.setTitle("Modificar Parada");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Aquí se recarga la tabla para ver los cambios hechos.
            refreshTabla();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir el formulario de edición:\n" + e.getMessage()).showAndWait();
        }
    }

    /**
     * salir
     * Objetivo: cerrar la ventana del listado.
     * Retorno: ninguno.
     */
    @FXML
    private void salir() {
        Stage stage = (Stage) tableParadas.getScene().getWindow();
        stage.close();
    }

    /**
     * refreshTabla
     * Objetivo: actualizar los datos mostrados en la tabla con lo que hay ahora en el grafo.
     * Retorno: ninguno.
     */
    public void refreshTabla() {
        if (grafo != null) {
            listaParadas.setAll(grafo.getParadas());
            tableParadas.refresh();
        }
    }
}