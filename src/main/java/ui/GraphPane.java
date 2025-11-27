package ui;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import models.Grafo;
import models.Parada;
import models.Ruta;

import java.text.Normalizer;
import java.util.*;

/**
 * GraphPane:
 *  - Nodos circulares con el nombre COMPLETO debajo (fuera del círculo).
 *  - Todos los nodos azules por defecto; origen en verde y destino en naranja.
 *  - Aristas grises por defecto; el camino resaltado se pinta con el color recibido.
 *  - Flecha abierta tipo "<" con puntas redondeadas, sin sobresalir de la línea.
 *  - Badge con inicial del tipo (M/T/A) dentro del círculo (A para Autobus/Bus/Guagua/Ómnibus, etc.).
 */
public class GraphPane extends Pane {

    public interface NodeClickListener { void onClick(Parada parada); }
    private NodeClickListener clickListener;

    // Línea + flechas por Ruta
    private final Map<Ruta, Group> rutaVisualGrupos = new HashMap<>();
    // Overlays para etiquetas de tramo al resaltar
    private final List<StackPane> overlays = new ArrayList<>();

    // Geometría y estilos
    private static final double NODE_RADIUS = 18.0;
    private static final double ARROW_SIZE = 14.0;   // largo de los segmentos de la cabeza
    private static final double ARROW_SPREAD = 0.6;  // apertura lateral relativa
    private static final double HEAD_INSET = 8.0;    // cuánto retraemos línea para ubicar cabeza
    private static final double LINE_MARGIN = 6.0;   // margen extra al borde del nodo

    // Colores de nodos
    private static final String COLOR_NODE_DEFAULT = "#27a9e3"; // azul
    private static final String COLOR_NODE_ORIGEN  = "#1dd3b0"; // verde
    private static final String COLOR_NODE_DESTINO = "#ff9800"; // naranja

    public GraphPane() {
        setMinSize(600, 420);
        setPrefSize(700, 460);
    }

    public void setNodoClick(NodeClickListener listener) { this.clickListener = listener; }

    /* ================== Render principal ================== */
    public void render(Grafo grafo, Parada origen, Parada destino) {
        getChildren().clear();
        rutaVisualGrupos.clear();
        clearOverlays();

        List<Parada> paradas = grafo.getParadasList();
        if (paradas == null || paradas.isEmpty()) return;

        Map<Parada, Point2D> pos = posicionesCirculares(paradas);

        // Detectar pares con ruta inversa real (A->B y B->A)
        Set<String> paresBidir = new HashSet<>();
        for (Parada p : paradas) {
            List<Ruta> sal = grafo.getRutasDeSalida(p);
            if (sal == null) continue;
            for (Ruta r : sal) {
                if (tieneRuta(grafo, r.getDestino(), r.getInicio())) {
                    paresBidir.add(normalizePair(key(r.getInicio(), r.getDestino())));
                }
            }
        }

        // Dibujar aristas (agrupando paralelas por par inicio->destino)
        for (Parada p : paradas) {
            List<Ruta> sal = grafo.getRutasDeSalida(p);
            if (sal == null) continue;

            Map<String, List<Ruta>> grupoPar = new HashMap<>();
            for (Ruta r : sal) {
                String k = key(r.getInicio(), r.getDestino());
                grupoPar.computeIfAbsent(k, kk -> new ArrayList<>()).add(r);
            }

            for (Map.Entry<String, List<Ruta>> e : grupoPar.entrySet()) {
                boolean bidir = paresBidir.contains(normalizePair(e.getKey()));
                List<Ruta> grupo = e.getValue();
                for (int i = 0; i < grupo.size(); i++) {
                    Ruta r = grupo.get(i);
                    Group g = crearArista(r, pos.get(r.getInicio()), pos.get(r.getDestino()), grupo.size(), i, bidir);
                    rutaVisualGrupos.put(r, g);
                    getChildren().add(g);
                }
            }
        }

        // Dibujar nodos (azul por defecto, origen verde, destino naranja)
        for (Parada p : paradas) {
            Group nodo = crearNodoConNombre(p, pos.get(p));
            // Default azul
            setNodeColor(nodo, COLOR_NODE_DEFAULT);
            // Overrides
            if (p == origen) setNodeColor(nodo, COLOR_NODE_ORIGEN);
            if (p == destino) setNodeColor(nodo, COLOR_NODE_DESTINO);
            getChildren().add(nodo);
        }
    }

    /* ================== Resaltado de camino ================== */
    public void iluminarCamino(List<Ruta> path, String colorHex) {
        clearCaminoIluminado();
        if (path == null || path.isEmpty()) return;

        Color c = Color.web(colorHex == null || colorHex.isBlank() ? COLOR_NODE_ORIGEN : colorHex);

        for (Ruta r : path) {
            Group g = rutaVisualGrupos.get(r);
            if (g == null) continue;

            Line line = (Line) g.getChildren().get(0);
            line.getStyleClass().add("highlight");
            line.setStrokeWidth(3.4);
            line.setStyle("-fx-stroke: " + toHexColor(c) + ";");

            if (g.getChildren().size() > 1 && g.getChildren().get(1) instanceof Group arrowDst)
                colorizeArrowGroup(arrowDst, c, 3.4);
            if (g.getChildren().size() > 2 && g.getChildren().get(2) instanceof Group arrowSrc)
                colorizeArrowGroup(arrowSrc, c, 3.4);

            double mx = (line.getStartX() + line.getEndX()) / 2;
            double my = (line.getStartY() + line.getEndY()) / 2;
            Label lbl = new Label(r.getNombre());
            lbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
            StackPane tag = new StackPane(lbl);
            tag.setStyle("-fx-background-color: rgba(0,0,0,0.55); -fx-background-radius: 8; -fx-padding: 4 8;");
            tag.setLayoutX(mx - 40);
            tag.setLayoutY(my - 18);
            overlays.add(tag);
            getChildren().add(tag);
        }
    }

    public void clearCaminoIluminado() {
        for (Group g : rutaVisualGrupos.values()) {
            if (!g.getChildren().isEmpty() && g.getChildren().get(0) instanceof Line line) {
                line.getStyleClass().remove("highlight");
                line.setStyle("");
                line.setStrokeWidth(2.4);
            }
            for (int i = 1; i < g.getChildren().size(); i++) {
                Node n = g.getChildren().get(i);
                if (n instanceof Group arrowGroup)
                    colorizeArrowGroup(arrowGroup, Color.web("#6e7681"), 2.4); // gris base
            }
        }
        clearOverlays();
    }

    private void clearOverlays() {
        for (StackPane sp : overlays) getChildren().remove(sp);
        overlays.clear();
    }

    /* ================== Helpers de aristas ================== */
    private String key(Parada a, Parada b) { return a.hashCode() + "->" + b.hashCode(); }
    private String normalizePair(String k) {
        String[] p = k.split("->");
        return (p[0].compareTo(p[1]) <= 0) ? p[0] + "<>" + p[1] : p[1] + "<>" + p[0];
    }
    private boolean tieneRuta(Grafo grafo, Parada inicio, Parada destino) {
        List<Ruta> sal = grafo.getRutasDeSalida(inicio);
        if (sal == null) return false;
        for (Ruta r : sal) if (r.getDestino() == destino) return true;
        return false;
    }

    /**
     * Crea la línea recortada y flechas abiertas.
     * La punta de la flecha coincide EXACTO con el extremo de la línea para que no sobresalga.
     */
    private Group crearArista(Ruta r, Point2D A, Point2D B, int total, int index, boolean bidirectional) {
        // Dirección y perpendicular
        double dx = B.getX() - A.getX();
        double dy = B.getY() - A.getY();
        double len = Math.hypot(dx, dy);
        double ux = dx / (len == 0 ? 1 : len);
        double uy = dy / (len == 0 ? 1 : len);
        double px = -uy;
        double py = ux;

        // Separación para paralelas
        double separation = 14.0;
        double offsetFactor = (total == 1) ? 0 : (index - (total - 1) / 2.0);
        double ox = px * offsetFactor * separation;
        double oy = py * offsetFactor * separation;

        // Margen para no entrar al nodo + Head inset para la cabeza
        double margin = NODE_RADIUS + LINE_MARGIN + HEAD_INSET;

        // La punta coincide exacto con el extremo de la línea
        double startX = A.getX() + ox + ux * margin;
        double startY = A.getY() + oy + uy * margin;
        double endX   = B.getX() + ox - ux * margin;
        double endY   = B.getY() + oy - uy * margin;

        Line line = new Line(startX, startY, endX, endY);
        line.getStyleClass().add("graph-edge");
        line.setStrokeLineCap(StrokeLineCap.ROUND);

        // Flecha abierta en DESTINO
        Group arrowDst = crearFlechaAbierta(endX, endY, ux, uy, px, py, ARROW_SIZE, 2.4);
        colorizeArrowGroup(arrowDst, Color.web("#6e7681"), 2.4);
        arrowDst.toFront();

        if (bidirectional) {
            // Flecha abierta en ORIGEN (apuntando hacia fuera)
            Group arrowSrc = crearFlechaAbierta(startX, startY, -ux, -uy, px, py, ARROW_SIZE, 2.4);
            colorizeArrowGroup(arrowSrc, Color.web("#6e7681"), 2.4);
            arrowSrc.toFront();
            return new Group(line, arrowDst, arrowSrc);
        } else {
            return new Group(line, arrowDst);
        }
    }

    /**
     * Flecha ABIERTA tipo “<” (dos segmentos) con punta en (tipX,tipY).
     */
    private Group crearFlechaAbierta(double tipX, double tipY,
                                     double ux, double uy, double px, double py,
                                     double size, double strokeWidth) {
        double spread = size * ARROW_SPREAD;
        double back   = size;

        double leftX  = tipX - ux * back + px * spread;
        double leftY  = tipY - uy * back + py * spread;
        double rightX = tipX - ux * back - px * spread;
        double rightY = tipY - uy * back - py * spread;

        Line l1 = new Line(tipX, tipY, leftX, leftY);
        Line l2 = new Line(tipX, tipY, rightX, rightY);
        l1.setStrokeLineCap(StrokeLineCap.ROUND);
        l2.setStrokeLineCap(StrokeLineCap.ROUND);
        l1.setStrokeWidth(strokeWidth);
        l2.setStrokeWidth(strokeWidth);
        return new Group(l1, l2);
    }

    private void colorizeArrowGroup(Group arrowGroup, Color color, double width) {
        for (Node n : arrowGroup.getChildren()) {
            if (n instanceof Line l) {
                l.setStroke(color);
                l.setStrokeWidth(width);
            }
        }
    }

    /* ================== Nodos: círculo + nombre debajo ================== */
    private Group crearNodoConNombre(Parada p, Point2D pos) {
        // Círculo
        StackPane circle = new StackPane();
        circle.setPrefSize(NODE_RADIUS * 2, NODE_RADIUS * 2);
        circle.getStyleClass().add("graph-node");

        // Badge del tipo (M/T/A). Se normaliza (sin acentos) y case-insensitive.
        String initial = getInitialFromTipo(p.getTipo());
        if (!initial.isEmpty()) {
            Label badge = new Label(initial);
            badge.setStyle("-fx-text-fill:white;-fx-font-size:10px;-fx-font-weight:800;"
                    + "-fx-background-color: rgba(0,0,0,0.35); -fx-background-radius:10;"
                    + "-fx-padding:2 6;");
            circle.getChildren().add(badge);
        }

        // Nombre completo debajo del círculo
        Label name = new Label(p.getNombre());
        name.setStyle("-fx-text-fill:#eaecef; -fx-font-size:11px; -fx-font-weight:600;");
        name.setWrapText(false);

        Group group = new Group(circle, name);

        // Posicionar círculo centrado y nombre debajo
        circle.setLayoutX(pos.getX() - NODE_RADIUS);
        circle.setLayoutY(pos.getY() - NODE_RADIUS);

        double labelY = pos.getY() + NODE_RADIUS + 2;
        name.setLayoutY(labelY);
        name.layoutBoundsProperty().addListener((o, oldV, newV) -> {
            name.setLayoutX(pos.getX() - newV.getWidth() / 2);
        });

        // Click
        group.setOnMouseClicked(e -> { if (clickListener != null) clickListener.onClick(p); });

        return group;
    }

    private String getInitialFromTipo(String tipoRaw) {
        if (tipoRaw == null) return "";
        String s = Normalizer.normalize(tipoRaw, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();

        // Sinónimos comunes para Autobus (mostrar "A")
        Set<String> autobus = Set.of(
                "autobus", "autobus urbano", "autobus interurbano",
                "autobus escolar", "autobus publico", "autobus público",
                "autobuses", "bus", "guagua", "omnibus", "omnibuses", "colectivo"
        );

        if (s.equals("metro")) return "M";
        if (s.equals("tren"))  return "T";
        if (autobus.contains(s)) return "A";

        // También si empieza por palabra clave
        if (s.startsWith("autobus") || s.startsWith("auto bus") || s.startsWith("bus")
                || s.startsWith("guagua") || s.startsWith("omnibus"))
            return "A";

        return "";
    }

    /* ================== Utilidades de color/posiciones ================== */
    private void setNodeColor(Group nodeGroup, String hexColor){
        for (Node n : nodeGroup.getChildren()){
            if (n instanceof StackPane circle){
                circle.setStyle("-fx-background-color: " + hexColor + ";" +
                        "-fx-background-radius: " + (NODE_RADIUS) + ";" +
                        "-fx-border-radius: " + (NODE_RADIUS) + ";" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2;");
                break;
            }
        }
    }

    private String toHexColor(Color c){
        int r = (int)Math.round(c.getRed()*255);
        int g = (int)Math.round(c.getGreen()*255);
        int b = (int)Math.round(c.getBlue()*255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private Map<Parada, Point2D> posicionesCirculares(List<Parada> paradas) {
        Map<Parada, Point2D> map = new HashMap<>();
        double w = getWidth()  > 0 ? getWidth()  : getPrefWidth();
        double h = getHeight() > 0 ? getHeight() : getPrefHeight();
        if (w <= 0) w = 700; if (h <= 0) h = 460;

        double cx = w / 2.0;
        double cy = h / 2.0;
        double radio = Math.min(w,h)/2.0 - 80;

        int n = paradas.size();
        for (int i = 0; i < n; i++) {
            double ang = 2 * Math.PI * i / Math.max(1,n);
            map.put(paradas.get(i),
                    new Point2D(cx + radio * Math.cos(ang),
                            cy + radio * Math.sin(ang)));
        }
        return map;
    }
}