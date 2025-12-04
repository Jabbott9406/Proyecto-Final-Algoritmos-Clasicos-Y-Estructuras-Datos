package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Grafo;
import models.Parada;

public class CRUDParadaController {

    @FXML
    private TextField nombreParadaField;

    @FXML
    private ComboBox<String> tipoParadaBox;

    private Grafo grafo = Grafo.getInstance();

    // Parada en edición:
    // Esto lo dejamos porque queremos poder abrir el mismo formulario para editar una parada existente
    // sin tener que crear otra pantalla aparte. Si hay algo aquí, el flujo cambia a "editar" en vez de "registrar".
    private Parada paradaEnEdicion = null;
    private ListParadaController listParadaController;

    /**
     * initialize
     * Objetivo: preparar el formulario de paradas con el combo de tipos y el prompt.
     * Retorno: ninguno.
     */
    @FXML
    public void initialize() {
        // Llenamos el combo con los tipos que usamos.
        tipoParadaBox.setItems(FXCollections.observableArrayList("Tren", "Metro", "Autobus"));
        // Prompt para recordar que hay que elegir un tipo.
        tipoParadaBox.setPromptText("Seleccione tipo");
    }

    /**
     * agregarParada
     * Objetivo: registrar una parada nueva o actualizar una existente.
     * Retorno: ninguno.
     */
    @FXML
    private void agregarParada(ActionEvent event) {
        // Agarramos lo que el usuario escribió y lo limpiamos de espacios al principio y al final.
        String nombre = nombreParadaField.getText().trim();
        String tipo = tipoParadaBox.getValue();

        // Validación básica: no dejamos registrar si falta el nombre o el tipo.
        if (nombre.isEmpty() || tipo == null) {
            mostrarAlerta("Error", "Debe ingresar nombre y tipo de parada.");
            return;
        }

        try {
            if (paradaEnEdicion != null) {
                // Modo edición: actualizamos la parada que ya existía.
                paradaEnEdicion.setNombre(nombre);
                paradaEnEdicion.setTipo(tipo);
                DataBase.ParadaDAO.getInstance().actualizarParada(paradaEnEdicion);

                // Mensaje de éxito simple para confirmar.
                MostrarMensaje("Parada actualizada", "La parada se actualizó correctamente.");
            } else {
                // Modo registro: creamos la nueva parada y la guardamos.
                Parada nueva = new Parada(nombre, tipo);
                grafo.agregarParada(nueva);
                DataBase.ParadaDAO.getInstance().guardarParada(nueva);

                // Mensaje de éxito cuando se registra.
                MostrarMensaje("Parada registrada", "La parada se registró correctamente.");
            }
        } catch (Exception e) {
            // Si algo falla, avisamos y dejamos el log para entender el error luego.
            mostrarAlerta("Error", "No se pudo guardar/actualizar la parada en la DB: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Aquí se recarga la tabla del listado si estamos trabajando junto al ListParadaController.
        if (listParadaController != null) {
            listParadaController.refreshTabla();
        }

        // Limpieza del formulario para seguir trabajando sin tener residuos en los campos.
        limpiarCampos();
    }

    /**
     * limpiarCampos
     * Objetivo: resetear el formulario a su estado inicial.
     * Retorno: ninguno.
     */
    @FXML
    private void limpiarCampos() {
        // Limpiamos el nombre.
        nombreParadaField.clear();

        // Quitamos la selección del combo y dejamos el prompt otra vez.
        tipoParadaBox.getSelectionModel().clearSelection();
        tipoParadaBox.setValue(null);
        tipoParadaBox.setPromptText("Seleccione tipo");

        // Salimos del modo edición para evitar confusiones en el siguiente registro.
        paradaEnEdicion = null;
    }

    /**
     * salir
     * Objetivo: cerrar la ventana actual del formulario.
     * Retorno: ninguno.
     */
    @FXML
    private void salir() {
        Stage stage = (Stage) nombreParadaField.getScene().getWindow();
        stage.close();
    }

    /**
     * setGrafo
     * Objetivo: inyectar el grafo para que el controlador tenga acceso al modelo.
     * Retorno: ninguno.
     */
    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
    }

    /**
     * setListParadaController
     * Objetivo: guardar referencia del listado para poder refrescarlo.
     * Retorno: ninguno.
     */
    public void setListParadaController(ListParadaController controller) {
        this.listParadaController = controller;
    }

    /**
     * cargarParadaParaEdicion
     * Objetivo: precargar el formulario con los datos de la parada que vamos a editar.
     * Retorno: ninguno.
     */
    public void cargarParadaParaEdicion(Parada parada) {
        // Al llenar estos campos, el flujo pasa a “edición”.
        this.paradaEnEdicion = parada;
        nombreParadaField.setText(parada.getNombre());
        tipoParadaBox.setValue(parada.getTipo());
    }

    /**
     * mostrarAlerta
     * Objetivo: utilitario rápido para mostrar un alert informativo.
     * Retorno: ninguno.
     */
    public static void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * MostrarMensaje
     * Objetivo: mensajito de confirmación estilo CRUDRutaController.
     * Retorno: ninguno.
     */
    public static void MostrarMensaje(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}