package controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import models.Grafo;
import models.Parada;
import models.Ruta;
import models.RutaMasCorta;
import ui.GraphPane;

import java.util.*;
import java.util.stream.Collectors;

public class Menu2Controller {

    @FXML private VBox sidebar;
    @FXML private Label lblMenu, lblHome, lblBus, lblRoute, lblSettings;
    @FXML private VBox submenuParadas, submenuRutas;

    @FXML private BorderPane mainRoot;
    @FXML private Label lblTitulo;
    @FXML private Label lblTyping;

    @FXML private ComboBox<Parada> cbOrigen;
    @FXML private ComboBox<Parada> cbDestino;

    @FXML private ToggleButton tbDistancia, tbTiempo, tbCosto, tbTransbordos;

    @FXML private Label lblRutaTitulo, lblMotivo, lblDuracion, lblCosto, lblDistancia, lblTramos, lblListadoRutas;
    @FXML private Label lblEvento, lblTransbordos;

    @FXML private StackPane graphContainer;

    private final Grafo grafo = Grafo.getInstance();
    public Stage pantalla;

    private boolean expandido = false;
    private boolean paradasOpen = false;
    private boolean rutasOpen   = false;

    private Parada origenSeleccionado;
    private Parada destinoSeleccionado;

    private GraphPane graphPane;

    private static final double COLAPSADO = 80;
    private static final double EXPANDIDO = 220;

    private final String[] frases = {
            "¿Hacia dónde quieres ir hoy?",
            "Elige tu ruta o selecciónala en el mapa.",
            "Selecciona origen y destino para comenzar."
    };
    private int fraseIndex = 0;

    /**
     * initialize
     * Objetivo: preparar la pantalla principal del mapa: sidebar, combos, filtros, grafo y mensaje inicial.
     * Retorno: ninguno.
     */
    @FXML
    private void initialize() {
        // Ponemos el sidebar pequeño de entrada.
        sidebar.setPrefWidth(COLAPSADO);

        // Ocultamos los textos del menú porque en modo colapsado solo estorban.
        ocultarLabelsSidebar();

        // Actualizamos estado de submenús.
        actualizarSubmenus();

        // Esta parte nos fue difícil entenderla al principio: teníamos que lograr que el contenido principal
        // se moviera automáticamente cuando el sidebar cambia de ancho. La forma fue anclar el BorderPane
        // al left del AnchorPane y escuchar cambios de ancho del sidebar.
        AnchorPane.setLeftAnchor(mainRoot, COLAPSADO);
        sidebar.widthProperty().addListener((obs, oldW, newW) ->
                AnchorPane.setLeftAnchor(mainRoot, newW.doubleValue())
        );

        // Configuramos los combos de origen y destino.
        configurarCombos();

        // Configuramos los filtros para que funcione el cambio de criterio.
        configurarFiltros();

        // Colocamos el panel donde dibujaremos el grafo y le asignamos el callback de click.
        graphPane = new GraphPane();
        graphPane.setNodoClick(this::manejarClickNodo);
        graphContainer.getChildren().setAll(graphPane);

        // Iniciamos la animación tipo máquina de escribir.
        iniciarTypewriter();

        // Dibujamos el grafo por primera vez y mostramos mensaje guía.
        renderGraph();
        actualizarResultado(null, "Selecciona origen y destino.", null);
    }

    /**
     * ocultarLabelsSidebar
     * Objetivo: esconder los labels cuando el sidebar está colapsado.
     * Retorno: ninguno.
     */
    private void ocultarLabelsSidebar() {
        for (Label l : Arrays.asList(lblMenu,lblHome,lblBus,lblRoute,lblSettings)) {
            l.setVisible(false);
            l.setManaged(false);
            l.setOpacity(0);
        }
    }

    /**
     * menuDesplegable
     * Objetivo: expandir o contraer el menú lateral con animaciones.
     * Retorno: ninguno.
     */
    @FXML
    private void menuDesplegable() {
        // Invertimos el estado expandido/colapsado.
        expandido = !expandido;
        double targetWidth = expandido ? EXPANDIDO : COLAPSADO;

        // Animación del ancho del sidebar.
        Timeline widthTl = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(sidebar.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH))
        );

        // Animaciones de aparición/desaparición de cada label.
        ParallelTransition textPt = new ParallelTransition();
        for (Label l : Arrays.asList(lblMenu,lblHome,lblBus,lblRoute,lblSettings)) {
            if (expandido) {
                l.setManaged(true);
                l.setVisible(true);
            }
            FadeTransition ft = new FadeTransition(Duration.millis(160), l);
            ft.setFromValue(expandido ? 0 : 1);
            ft.setToValue(expandido ? 1 : 0);
            if (!expandido) {
                // Al terminar el fade cuando colapsamos, realmente lo quitamos del layout.
                ft.setOnFinished(ev -> { l.setVisible(false); l.setManaged(false); });
            }
            textPt.getChildren().add(ft);
        }

        // Si cerramos, también apagamos submenús.
        if (!expandido) {
            paradasOpen = false;
            rutasOpen = false;
            actualizarSubmenus();
        }

        // Lanzamos ambas animaciones juntas.
        new ParallelTransition(widthTl, textPt).play();
    }

    /**
     * toggleParadasMenu
     * Objetivo: alternar submenú de paradas (si está colapsado primero expandimos el sidebar).
     * Retorno: ninguno.
     */
    @FXML private void toggleParadasMenu() {
        if (!expandido) {
            menuDesplegable();
            paradasOpen = true;
        } else {
            paradasOpen = !paradasOpen;
        }
        actualizarSubmenus();
    }

    /**
     * toggleRutasMenu
     * Objetivo: alternar submenú de rutas.
     * Retorno: ninguno.
     */
    @FXML private void toggleRutasMenu() {
        if (!expandido) {
            menuDesplegable();
            rutasOpen = true;
        } else {
            rutasOpen = !rutasOpen;
        }
        actualizarSubmenus();
    }

    /**
     * actualizarSubmenus
     * Objetivo: sincronizar visibilidad de submenús según flags y estado del sidebar.
     * Retorno: ninguno.
     */
    private void actualizarSubmenus() {
        setVis(submenuParadas, expandido && paradasOpen);
        setVis(submenuRutas,   expandido && rutasOpen);
    }

    /**
     * setVis
     * Objetivo: mostrar/ocultar un VBox y hacer que el layout lo tome o no.
     * Retorno: ninguno.
     */
    private void setVis(VBox box, boolean v) {
        if (box!=null){
            box.setVisible(v);
            box.setManaged(v);
        }
    }

    /**
     * configurarCombos
     * Objetivo: llenar y preparar los ComboBox de origen y destino y asignar listeners de cambio.
     * Retorno: ninguno.
     */
    private void configurarCombos() {
        recargarParadasCombo();

        cbOrigen.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Parada item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });
        cbOrigen.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Parada item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });

        cbDestino.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Parada item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });
        cbDestino.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Parada item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });

        // Converter para asegurar que el botón muestre correctamente el nombre.
        StringConverter<Parada> converter = new StringConverter<>() {
            @Override public String toString(Parada p) { return p == null ? "" : p.getNombre(); }
            @Override public Parada fromString(String s) { return null; }
        };
        cbOrigen.setConverter(converter);
        cbDestino.setConverter(converter);

        // Listener: cuando cambia el origen recalculamos.
        cbOrigen.valueProperty().addListener((obs,o,v)-> {
            origenSeleccionado = v;
            recalcularRutaOptima();
        });

        // Listener: cuando cambia el destino recalculamos.
        cbDestino.valueProperty().addListener((obs,o,v)-> {
            destinoSeleccionado = v;
            recalcularRutaOptima();
        });
    }

    /**
     * recargarParadasCombo
     * Objetivo: volver a cargar paradas en los combos y mantener selección previa.
     * Retorno: ninguno.
     */
    private void recargarParadasCombo() {
        List<Parada> lista = grafo.getParadasList();
        cbOrigen.getItems().setAll(lista);
        cbDestino.getItems().setAll(lista);
        if (origenSeleccionado != null) cbOrigen.getSelectionModel().select(origenSeleccionado);
        if (destinoSeleccionado != null) cbDestino.getSelectionModel().select(destinoSeleccionado);
    }

    /**
     * configurarFiltros
     * Objetivo: dejar listos los cuatro filtros y que solo uno esté activo a la vez.
     * Retorno: ninguno.
     */
    private void configurarFiltros() {
        if (!tbTransbordos.getStyleClass().contains("chip-purple")) {
            tbTransbordos.getStyleClass().add("chip-purple");
        }

        tbDistancia.setSelected(true);
        tbDistancia.setOnAction(e -> { deselectOthers(tbDistancia); recalcularRutaOptima(); });
        tbTiempo.setOnAction(e    -> { deselectOthers(tbTiempo);    recalcularRutaOptima(); });
        tbCosto.setOnAction(e     -> { deselectOthers(tbCosto);     recalcularRutaOptima(); });
        tbTransbordos.setOnAction(e -> { deselectOthers(tbTransbordos); recalcularRutaOptima(); });
    }

    /**
     * deselectOthers
     * Objetivo: desactivar todos los filtros excepto el que se eligió.
     * Retorno: ninguno.
     */
    private void deselectOthers(ToggleButton selected) {
        for (ToggleButton tb : Arrays.asList(tbDistancia,tbTiempo,tbCosto,tbTransbordos)) {
            tb.setSelected(tb == selected);
        }
    }

    /**
     * manejarClickNodo
     * Objetivo: controlar la lógica al hacer click en un nodo (origen/destino/reset).
     * Retorno: ninguno.
     */
    private void manejarClickNodo(Parada p) {
        if (origenSeleccionado == null) {
            origenSeleccionado = p;
            cbOrigen.getSelectionModel().select(p);
        } else if (destinoSeleccionado == null && p != origenSeleccionado) {
            destinoSeleccionado = p;
            cbDestino.getSelectionModel().select(p);
        } else if (p == origenSeleccionado) {
            origenSeleccionado = null;
            destinoSeleccionado = null;
            cbOrigen.getSelectionModel().clearSelection();
            cbDestino.getSelectionModel().clearSelection();
        } else {
            destinoSeleccionado = p;
            cbDestino.getSelectionModel().select(p);
        }
        recalcularRutaOptima();
    }

    /**
     * recalcularRutaOptima
     * Objetivo: pedir al grafo la mejor ruta según el filtro y reflejarla en pantalla.
     * Retorno: ninguno.
     */
    private void recalcularRutaOptima() {
        renderGraph();

        if (origenSeleccionado == null || destinoSeleccionado == null) {
            graphPane.clearCaminoIluminado();
            actualizarResultado(null, "Selecciona origen y destino.", null);
            return;
        }

        String filtro = getFiltroString();
        String criterio = getCriterioNombre();

        RutaMasCorta rm = grafo.obtenerMejorRuta(origenSeleccionado, destinoSeleccionado, filtro);

        if (rm == null || rm.getRutas() == null || rm.getRutas().isEmpty()) {
            graphPane.clearCaminoIluminado();
            actualizarResultado(null,
                    "No existe ruta entre " + origenSeleccionado.getNombre() + " → " + destinoSeleccionado.getNombre(),
                    null);
            return;
        }

        List<Ruta> path = rm.getRutas();
        graphPane.iluminarCamino(path, criterioColor());

        String motivo = ("transbordos".equalsIgnoreCase(filtro))
                ? "Mejor camino minimizando transbordos"
                : (path.size() == 1 ? "Mejor ruta directa por " : "Mejor camino por ") + criterio.toLowerCase();

        actualizarResultadoDesdeRutaMasCorta(rm, path, motivo,
                (path.size() == 1 ? path.get(0).getNombre() : null));
    }

    /**
     * getFiltroString
     * Objetivo: obtener el nombre del filtro activo.
     * Retorno: string del filtro.
     */
    private String getFiltroString() {
        if (tbTransbordos.isSelected()) return "transbordos";
        if (tbTiempo.isSelected())  return "tiempo";
        if (tbCosto.isSelected())   return "costo";
        return "distancia";
    }

    /**
     * getCriterioNombre
     * Objetivo: versión con mayúscula para UI.
     * Retorno: string human readable.
     */
    private String getCriterioNombre() {
        if (tbTransbordos.isSelected()) return "Transbordos";
        if (tbTiempo.isSelected()) return "Tiempo";
        if (tbCosto.isSelected())  return "Costo";
        return "Distancia";
    }

    /**
     * criterioColor
     * Objetivo: color según filtro, para resaltar camino.
     * Retorno: hex del color.
     */
    private String criterioColor() {
        if (tbTransbordos.isSelected()) return "#9b59b6";
        if (tbTiempo.isSelected()) return "#27a9e3";
        if (tbCosto.isSelected())  return "#ff9800";
        return "#1dd3b0";
    }

    /**
     * renderGraph
     * Objetivo: dibujar nodos y rutas con selección actual.
     * Retorno: ninguno.
     */
    private void renderGraph() {
        graphPane.render(grafo, origenSeleccionado, destinoSeleccionado);
    }

    /**
     * actualizarResultadoDesdeRutaMasCorta
     * Objetivo: llenar panel de resultados con datos de la ruta encontrada.
     * Retorno: ninguno.
     */
    private void actualizarResultadoDesdeRutaMasCorta(RutaMasCorta rm, List<Ruta> path, String motivo, String nombreDirecta) {
        double t = rm.getTotalTiempo();
        double c = rm.getTotalCosto();
        double d = rm.getTotalDistancia();

        lblRutaTitulo.setText(nombreDirecta != null ? nombreDirecta : "Camino (" + path.size() + " tramo(s))");
        lblMotivo.setText(motivo);
        lblDuracion.setText("Duración: " + formatNum(t) + " min");
        lblCosto.setText("Costo: $" + formatNum(c));
        lblDistancia.setText("Distancia: " + formatNum(d) + " km");

        String evento = (rm.getEvento() == null || rm.getEvento().isBlank()) ? "Sin evento" : rm.getEvento();
        lblEvento.setText("Evento: " + evento);

        lblTransbordos.setText("Transbordos: " + rm.getTransbordos());
        lblTramos.setText("Tramos: " + path.size());

        String listado = path.stream().map(Ruta::getNombre).collect(Collectors.joining(" → "));
        lblListadoRutas.setText("Rutas: " + listado);
    }

    /**
     * actualizarResultado
     * Objetivo: poner panel de resultados en estado vacío.
     * Retorno: ninguno.
     */
    private void actualizarResultado(List<Ruta> path, String motivo, String nombreDirecta) {
        lblRutaTitulo.setText("Sin ruta");
        lblMotivo.setText(motivo);
        lblDuracion.setText("Duración: -");
        lblCosto.setText("Costo: -");
        lblDistancia.setText("Distancia: -");
        lblEvento.setText("Evento: -");
        lblTransbordos.setText("Transbordos: -");
        lblTramos.setText("Tramos: -");
        lblListadoRutas.setText("");
    }

    /**
     * formatNum
     * Objetivo: formatear número con dos decimales o guion si no es válido.
     * Retorno: string.
     */
    private String formatNum(double v) {
        if (Double.isInfinite(v) || Double.isNaN(v)) return "-";
        return String.format(Locale.US, "%.2f", v);
    }

    /**
     * iniciarTypewriter
     * Objetivo: poner primera frase y comenzar animación de escritura.
     * Retorno: ninguno.
     */
    private void iniciarTypewriter() {
        lblTyping.setText("");
        mostrarFrase(frases[fraseIndex]);
    }

    /**
     * mostrarFrase
     * Objetivo: escribir la frase carácter por carácter y luego pasar a la siguiente.
     * Retorno: ninguno.
     */
    private void mostrarFrase(String frase) {
        lblTyping.setText("");
        Timeline tl = new Timeline();
        for (int i = 0; i < frase.length(); i++) {
            final int idx = i;
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(40 * (i + 1)),
                    e -> lblTyping.setText(frase.substring(0, idx + 1))));
        }
        tl.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> {
                fraseIndex = (fraseIndex + 1) % frases.length;
                mostrarFrase(frases[fraseIndex]);
            });
            pause.play();
        });
        tl.play();
    }

    /**
     * abrirRegistrarParada
     * Objetivo: abrir ventana de registro de parada y refrescar luego.
     * Retorno: ninguno.
     */
    @FXML private void abrirRegistrarParada() { abrirVentana("/application/RegistrarParada.fxml","Registrar Parada"); }

    /**
     * abrirListadoParadas
     * Objetivo: abrir ventana con listado de paradas.
     * Retorno: ninguno.
     */
    @FXML private void abrirListadoParadas()   { abrirVentana("/application/listParada-view.fxml","Listado de Paradas"); }

    /**
     * abrirRegistrarRuta
     * Objetivo: abrir ventana de registro de ruta.
     * Retorno: ninguno.
     */
    @FXML private void abrirRegistrarRuta()    { abrirVentana("/application/registRuta-view.fxml","Registrar Ruta"); }

    /**
     * abrirListadoRutas
     * Objetivo: abrir ventana con listado de rutas.
     * Retorno: ninguno.
     */
    @FXML private void abrirListadoRutas()     { abrirVentana("/application/listRuta-view.fxml","Listado de Rutas"); }

    /**
     * abrirVentana
     * Objetivo: cargar FXML en nueva Stage y al cerrar refrescar combos y ruta.
     * Retorno: ninguno.
     */
    private void abrirVentana(String resource, String titulo) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(resource));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(scene);

            Object controller = fxmlLoader.getController();
            try {
                controller.getClass().getMethod("setGrafo", Grafo.class).invoke(controller, grafo);
            } catch (Exception ignored) {}

            stage.show();
            stage.setOnHidden(ev -> {
                recargarParadasCombo();
                recalcularRutaOptima();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}