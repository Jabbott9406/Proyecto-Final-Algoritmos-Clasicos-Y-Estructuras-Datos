package ui;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import models.Grafo;
import models.Parada;
import models.Ruta;

import java.util.*;

/**
 * Dibuja grafo dirigido:
 *  - Posiciones circulares
 *  - Resaltado de un camino con color variable
 */
public class GraphPane extends Pane {

    public interface NodeClickListener { void onClick(Parada parada); }
    private NodeClickListener clickListener;

    private final Map<Ruta, Group> rutaVisualGrupos = new HashMap<>();
    private final List<StackPane> overlays = new ArrayList<>();

    public GraphPane() {
        setMinSize(600, 420);
        setPrefSize(700, 460);
    }

    public void setNodoClick(NodeClickListener listener) {
        this.clickListener = listener;
    }

    public void render(Grafo grafo, Parada origen, Parada destino) {
        getChildren().clear(); // Limpiar el panel
        rutaVisualGrupos.clear();
        clearOverlays();

        List<Parada> paradas = grafo.getParadasList();
        if (paradas.isEmpty()) return;

        Map<Parada, Point2D> pos = posicionesCirculares(paradas);

        // Agrupar rutas paralelas por inicio-destino
        Map<String, List<Ruta>> paraleloMap = new HashMap<>();
        for (Parada p : paradas) {
            for (Ruta r : grafo.getMapa().getOrDefault(p, Collections.emptyList())) {
                String key = r.getInicio().hashCode() + "->" + r.getDestino().hashCode();
                paraleloMap.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }
        }

        // Dibujar aristas para paralelas
        for (List<Ruta> grupo : paraleloMap.values()) {
            int count = grupo.size();
            for (int i = 0; i < count; i++) {
                Ruta r = grupo.get(i);
                Point2D A = pos.get(r.getInicio());
                Point2D B = pos.get(r.getDestino());
                Group g = crearArista(r, A, B, count, i);
                rutaVisualGrupos.put(r, g);
                getChildren().add(g);
            }
        }

        // Nodos
        for (Parada p : paradas) {
            StackPane node = crearNodo(p, pos.get(p));
            if (p == origen) node.getStyleClass().add("selected-origen");
            if (p == destino) node.getStyleClass().add("selected-destino");
            getChildren().add(node);
        }
    }

    public void iluminarCamino(List<Ruta> path, String colorHex) {
        clearCaminoIluminado();
        if (path == null || path.isEmpty()) return;

        for (Ruta r : path) {
            Group g = rutaVisualGrupos.get(r);
            if (g == null) continue;
            Line line = (Line) g.getChildren().get(0);
            line.getStyleClass().add("highlight");
            line.setStrokeWidth(3.2);
            line.setStyle("-fx-stroke: " + colorHex + ";");

            if (g.getChildren().size() > 1 && g.getChildren().get(1) instanceof Polygon arrow) {
                arrow.setStyle("-fx-fill: " + colorHex + ";");
            }

            // nombre de la ruta
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
            if (g.getChildren().get(0) instanceof Line line) {
                line.getStyleClass().remove("highlight");
                line.setStyle(""); // limpia color inline
                line.setStrokeWidth(2);
            }
            if (g.getChildren().size() > 1 && g.getChildren().get(1) instanceof Polygon arrow) {
                arrow.setStyle(""); // limpia color inline
            }
        }
        clearOverlays();
    }

    private void clearOverlays() {
        for (StackPane sp : overlays) getChildren().remove(sp);
        overlays.clear();
    }

    /* ---------------- Helpers ---------------- */

    private Group crearArista(Ruta r, Point2D A, Point2D B, int total, int index) {
        double dx = B.getX() - A.getX();
        double dy = B.getY() - A.getY();
        double len = Math.hypot(dx, dy);
        double ux = dx / (len == 0 ? 1 : len);
        double uy = dy / (len == 0 ? 1 : len);
        double px = -uy;
        double py = ux;

        double separation = 12.0; // distancia entre paralelas
        double offsetFactor = (total == 1) ? 0 : (index - (total - 1) / 2.0);
        double ox = px * offsetFactor * separation;
        double oy = py * offsetFactor * separation;

        Line line = new Line(A.getX() + ox, A.getY() + oy, B.getX() + ox, B.getY() + oy);
        line.getStyleClass().add("graph-edge");

        // Flecha
        double arrowSize = 10;
        double ex = line.getEndX();
        double ey = line.getEndY();
        double backX = ex - ux * arrowSize;
        double backY = ey - uy * arrowSize;

        // Dos puntos laterales para la base de la flecha
        double leftX = backX + px * (arrowSize * 0.6);
        double leftY = backY + py * (arrowSize * 0.6);
        double rightX = backX - px * (arrowSize * 0.6);
        double rightY = backY - py * (arrowSize * 0.6);

        Polygon arrow = new Polygon(
                ex, ey,
                leftX, leftY,
                rightX, rightY
        );
        arrow.setStyle("-fx-fill:#6e7681;");

        return new Group(line, arrow);
    }

    private StackPane crearNodo(Parada p, Point2D pos) {
        StackPane sp = new StackPane();
        sp.setLayoutX(pos.getX() - 18);
        sp.setLayoutY(pos.getY() - 18);
        sp.setPrefSize(36,36);
        sp.getStyleClass().add("graph-node");

        Label lbl = new Label(p.getNombre());
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        sp.getChildren().add(lbl);

        sp.setOnMouseClicked(e -> { if (clickListener != null) clickListener.onClick(p); });
        return sp;
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