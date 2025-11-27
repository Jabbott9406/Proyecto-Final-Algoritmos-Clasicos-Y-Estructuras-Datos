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

    @FXML
    private void initialize() {
        sidebar.setPrefWidth(COLAPSADO);
        ocultarLabelsSidebar();
        actualizarSubmenus();

        AnchorPane.setLeftAnchor(mainRoot, COLAPSADO);
        sidebar.widthProperty().addListener((obs, oldW, newW) ->
                AnchorPane.setLeftAnchor(mainRoot, newW.doubleValue())
        );

        configurarCombos();
        configurarFiltros();

        graphPane = new GraphPane();
        graphPane.setNodoClick(this::manejarClickNodo);
        graphContainer.getChildren().setAll(graphPane);

        iniciarTypewriter();

        renderGraph();
        actualizarResultado(null, "Selecciona origen y destino.", null);
    }

    private void ocultarLabelsSidebar() {
        for (Label l : Arrays.asList(lblMenu,lblHome,lblBus,lblRoute,lblSettings)) {
            l.setVisible(false);
            l.setManaged(false);
            l.setOpacity(0);
        }
    }
    @FXML
    private void menuDesplegable() {
        expandido = !expandido;
        double targetWidth = expandido ? EXPANDIDO : COLAPSADO;

        Timeline widthTl = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(sidebar.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH))
        );
        ParallelTransition textPt = new ParallelTransition();
        for (Label l : Arrays.asList(lblMenu,lblHome,lblBus,lblRoute,lblSettings)) {
            if (expandido) {
                l.setManaged(true);
                l.setVisible(true);
            }
            FadeTransition ft = new FadeTransition(Duration.millis(160), l);
            ft.setFromValue(expandido ? 0 : 1);
            ft.setToValue(expandido ? 1 : 0);
            if (!expandido) ft.setOnFinished(ev -> { l.setVisible(false); l.setManaged(false); });
            textPt.getChildren().add(ft);
        }
        if (!expandido) { paradasOpen = false; rutasOpen = false; actualizarSubmenus(); }
        new ParallelTransition(widthTl, textPt).play();
    }
    @FXML private void toggleParadasMenu() {
        if (!expandido) {
            menuDesplegable();
            paradasOpen = true;
        } else paradasOpen = !paradasOpen;
        actualizarSubmenus();
    }
    @FXML private void toggleRutasMenu() {
        if (!expandido) {
            menuDesplegable();
            rutasOpen = true;
        } else rutasOpen = !rutasOpen;
        actualizarSubmenus();
    }
    private void actualizarSubmenus() {
        setVis(submenuParadas, expandido && paradasOpen);
        setVis(submenuRutas,   expandido && rutasOpen);
    }
    private void setVis(VBox box, boolean v) { if (box!=null){ box.setVisible(v); box.setManaged(v);} }

    private void configurarCombos() {
        recargarParadasCombo();

        cbOrigen.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Parada item, boolean empty) {
                super.updateItem(item, empty); setText(empty || item == null ? "" : item.getNombre());
            }
        });
        cbOrigen.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Parada item, boolean empty) {
                super.updateItem(item, empty); setText(empty || item == null ? "" : item.getNombre());
            }
        });

        cbDestino.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Parada item, boolean empty) {
                super.updateItem(item, empty); setText(empty || item == null ? "" : item.getNombre());
            }
        });
        cbDestino.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Parada item, boolean empty) {
                super.updateItem(item, empty); setText(empty || item == null ? "" : item.getNombre());
            }
        });

        StringConverter<Parada> converter = new StringConverter<>() {
            @Override public String toString(Parada p) { return p == null ? "" : p.getNombre(); }
            @Override public Parada fromString(String s) { return null; }
        };
        cbOrigen.setConverter(converter);
        cbDestino.setConverter(converter);

        cbOrigen.valueProperty().addListener((obs,o,v)-> {
            origenSeleccionado = v;
            recalcularRutaOptima();
        });
        cbDestino.valueProperty().addListener((obs,o,v)-> {
            destinoSeleccionado = v;
            recalcularRutaOptima();
        });
    }
    private void recargarParadasCombo() {
        List<Parada> lista = grafo.getParadasList();
        cbOrigen.getItems().setAll(lista);
        cbDestino.getItems().setAll(lista);
        if (origenSeleccionado != null) cbOrigen.getSelectionModel().select(origenSeleccionado);
        if (destinoSeleccionado != null) cbDestino.getSelectionModel().select(destinoSeleccionado);
    }

    private void configurarFiltros() {
        // Aseguramos color morado SOLO para el toggle de Transbordos
        if (!tbTransbordos.getStyleClass().contains("chip-purple")) {
            tbTransbordos.getStyleClass().add("chip-purple");
        }

        tbDistancia.setSelected(true);
        tbDistancia.setOnAction(e -> { deselectOthers(tbDistancia); recalcularRutaOptima(); });
        tbTiempo.setOnAction(e    -> { deselectOthers(tbTiempo);    recalcularRutaOptima(); });
        tbCosto.setOnAction(e     -> { deselectOthers(tbCosto);     recalcularRutaOptima(); });
        tbTransbordos.setOnAction(e -> { deselectOthers(tbTransbordos); recalcularRutaOptima(); });
    }
    private void deselectOthers(ToggleButton selected) {
        for (ToggleButton tb : Arrays.asList(tbDistancia,tbTiempo,tbCosto,tbTransbordos)) {
            tb.setSelected(tb == selected);
        }
    }

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

    private String getFiltroString() {
        if (tbTransbordos.isSelected()) return "transbordos";
        if (tbTiempo.isSelected())  return "tiempo";
        if (tbCosto.isSelected())   return "costo";
        return "distancia";
    }
    private String getCriterioNombre() {
        if (tbTransbordos.isSelected()) return "Transbordos";
        if (tbTiempo.isSelected()) return "Tiempo";
        if (tbCosto.isSelected())  return "Costo";
        return "Distancia";
    }
    private String criterioColor() {
        if (tbTransbordos.isSelected()) return "#9b59b6"; // morado
        if (tbTiempo.isSelected()) return "#27a9e3";
        if (tbCosto.isSelected())  return "#ff9800";
        return "#1dd3b0";
    }

    private void renderGraph() {
        graphPane.render(grafo, origenSeleccionado, destinoSeleccionado);
    }

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

    private String formatNum(double v) {
        if (Double.isInfinite(v) || Double.isNaN(v)) return "-";
        return String.format(Locale.US, "%.2f", v);
    }

    private void iniciarTypewriter() {
        lblTyping.setText("");
        mostrarFrase(frases[fraseIndex]);
    }
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

    @FXML private void abrirRegistrarParada() { abrirVentana("/application/RegistrarParada.fxml","Registrar Parada"); }
    @FXML private void abrirListadoParadas()   { abrirVentana("/application/listParada-view.fxml","Listado de Paradas"); }
    @FXML private void abrirRegistrarRuta()    { abrirVentana("/application/registRuta-view.fxml","Registrar Ruta"); }
    @FXML private void abrirListadoRutas()     { abrirVentana("/application/listRuta-view.fxml","Listado de Rutas"); }

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