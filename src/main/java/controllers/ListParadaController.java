package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Grafo;
import models.Parada;

public class ListParadaController {

    @FXML
    private TableView<Parada> tableParadas;

    @FXML
    private TableColumn<Parada, String> nombreColumn;

    private Grafo grafo;
    private ObservableList<Parada> listaParadas = FXCollections.observableArrayList();

    @FXML

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        if (grafo != null) {
            listaParadas.setAll(grafo.getParadas());
        }
    }

    public ObservableList<Parada> getListaParadas() {
        return listaParadas;
    }
}
