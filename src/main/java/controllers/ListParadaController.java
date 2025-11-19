package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Grafo;
import models.Parada;

import java.util.Optional;

public class  ListParadaController {

    @FXML
    private TableView<Parada> tableParadas;

    @FXML
    private TableColumn<Parada, String> nombreColumn;

    @FXML
    private TableColumn<Parada, String> tipoColumn;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnModificar;

    private Grafo grafo = Grafo.getInstance();
    private ObservableList<Parada> listaParadas = FXCollections.observableArrayList();

    @FXML
    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        if (grafo != null) {
            listaParadas.setAll(grafo.getParadas());
            tableParadas.setItems(listaParadas);
        }
    }

    public ObservableList<Parada> getListaParadas() {
        return listaParadas;
    }

    @FXML
    void eliminarParada(ActionEvent event) {
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
            tableParadas.getSelectionModel().clearSelection();
            tableParadas.refresh();

            // eliminar de la DB
            try {
                DataBase.ParadaDAO.getInstance().eliminarParada(seleccionado.getId());
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error al eliminar de la DB: " + e.getMessage()).showAndWait();
                e.printStackTrace();
            }
        }
    }


    @FXML
    void modificarParada(ActionEvent event) {
        Parada seleccionado = tableParadas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona una parada.").showAndWait();
            return;
        }

        TextInputDialog dNombre = new TextInputDialog(seleccionado.getNombre());
        dNombre.setTitle("Modificar Parada");
        dNombre.setHeaderText("Editar nombre");
        dNombre.setContentText("Nombre:");
        Optional<String> nuevoNombre = dNombre.showAndWait();
        if (nuevoNombre.isEmpty()) return;
        String n = nuevoNombre.get().trim();
        if (n.isEmpty()) return;

        TextInputDialog dTipo = new TextInputDialog(seleccionado.getTipo());
        dTipo.setTitle("Modificar Parada");
        dTipo.setHeaderText("Editar tipo");
        dTipo.setContentText("Tipo:");
        String t = dTipo.showAndWait().orElse(seleccionado.getTipo());

        seleccionado.setNombre(n);
        seleccionado.setTipo(t);
        tableParadas.refresh();
        try {
            DataBase.ParadaDAO.getInstance().actualizarParada(seleccionado);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al actualizar en la DB: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        System.out.println("ListParadaController.initialize() - paradas.size = " + grafo.getParadas().size());
        listaParadas.setAll(grafo.getParadas());
        tableParadas.setItems(listaParadas);

        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tipoColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));
    }
}
