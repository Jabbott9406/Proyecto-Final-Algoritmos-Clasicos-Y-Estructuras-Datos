package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Grafo;
import models.Parada;

import java.util.Optional;

public class ListParadaController {


    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnModificar;

    @FXML
    private TableColumn<Parada, String> colNombre;

    @FXML
    private TableColumn<Parada, String> colTipo;

    @FXML
    private TableView<Parada> tblParadas;

    private final Grafo grafo = Grafo.getInstance();



    @FXML
    void eliminarParada(ActionEvent event) {
        Parada seleccionado = tblParadas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona una parada.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar la parada \"" + seleccionado.getNombre() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            // elimina del modelo
            grafo.eliminarParada(seleccionado);
            // asegúrate de quitarla de la lista observable (por si tu Grafo no lo hace)
            grafo.getParadas().remove(seleccionado);
            // refresca la tabla
            tblParadas.getSelectionModel().clearSelection();
            tblParadas.refresh();
        }
    }

    @FXML
    void modificarParada(ActionEvent event) {
        Parada seleccionado = tblParadas.getSelectionModel().getSelectedItem();
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

        grafo.modificarParada(seleccionado, n);

        tblParadas.refresh();
    }

    @FXML
    void initialize() {
        System.out.println("ListParadaController.initialize() - paradas.size = " + grafo.getParadas().size());

        // enlaza la lista observable del grafo al TableView
        tblParadas.setItems(grafo.getParadas());

        // configuramos las columnas usando los getters getNombre() y getTipo() de Parada
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));


    }
}



