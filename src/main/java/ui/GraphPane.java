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
     * Aquí pintamos el grafo a mano en un Pane. Decidimos hacerlo así porque queríamos
     * controlar cada detallito visual: el círculo del nodo, el nombre debajo, la flecha abierta,
     * el color del camino, etc. No usamos Canvas para mantenerlo simple con nodos JavaFX.
     *
     * - Nodos circulares con el nombre completo debajo. Esto ayuda a leer cada parada sin acercar zoom.
     * - Colores: por defecto azul; si es origen lo pintamos verde, si es destino lo pintamos naranja.
     * - Aristas grises por defecto. Cuando resaltamos un camino, esas líneas cambian al color del criterio.
     * - Badge con inicial del tipo (M/T/A) dentro del círculo. Es pequeño pero útil para identificar tipo rápido.
     *
     * Nota}: varias decisiones fueron ensayo y error. Por ejemplo, la flecha abierta la probamos
     * con triángulo sólido pero se veía pesada; la abierta quedó más elegante. También ajustamos márgenes para
     * que la línea no se meta dentro del círculo y la flecha no sobresalga. Eso nos tomó un buen rato calibrando.
     */
    public class GraphPane extends Pane {
    
        /**
         * NodeClickListener
         * Cuando el usuario le da click a un nodo, reportamos la parada al controlador.
         * Lo dejamos como interfaz para que el controlador decida qué hacer (marcar origen/destino, etc.).
         */
        public interface NodeClickListener { void onClick(Parada parada); }
        private NodeClickListener clickListener;
    
        // Guardamos el “grupo visual” de cada ruta (línea + flechas) para poder recolorearlo
        // cuando resaltamos un camino. Si no guardamos esto, sería difícil encontrar
        // cuál Line corresponde a cuál Ruta después de dibujar todo.
        private final Map<Ruta, Group> rutaVisualGrupos = new HashMap<>();
    
        // Overlays: pequeños carteles con el nombre de la ruta (R1, R2, etc.) que ponemos
        // cuando resaltamos el camino. Los guardamos para poder limpiarlos después.
        private final List<StackPane> overlays = new ArrayList<>();
    
        // Geometría base. Probamos varios tamaños y estos nos dieron una relación visual decente
        // entre nodo, línea y flecha.
        private static final double NODE_RADIUS = 18.0;
        private static final double ARROW_SIZE = 14.0;   // longitud de cada “brazo” de la flecha abierta
        private static final double ARROW_SPREAD = 0.6;  // qué tan “abierta” está la flecha (0..1 aprox)
        private static final double HEAD_INSET = 8.0;    // cuánto retraemos la línea para ubicar la cabeza sin que se salga
        private static final double LINE_MARGIN = 6.0;   // margen adicional para que la línea no toque el borde del círculo
    
        // Paleta de colores de nodos. Nos funcionó bien porque contrasta con el fondo oscuro del mapa.
        private static final String COLOR_NODE_DEFAULT = "#27a9e3"; // azul
        private static final String COLOR_NODE_ORIGEN  = "#1dd3b0"; // verde
        private static final String COLOR_NODE_DESTINO = "#ff9800"; // naranja
    
        /**
         * GraphPane
         * Decidimos tamaños mínimos para que el grafo no colapse en ventanas pequeñas.
         * El prefSize nos da un área cómoda para dibujar sin que todo quede apretado.
         */
        public GraphPane() {
            setMinSize(600, 420);
            setPrefSize(700, 460);
        }
    
        /**
         * setNodoClick
         * Con esto registramos el callback que el controlador nos pasa para reaccionar al click.
         */
        public void setNodoClick(NodeClickListener listener) { this.clickListener = listener; }
    
        /* ================== Render principal ================== */
    
        /**
         * render
         * Objetivo: dibujar todo el grafo según la lista de paradas y rutas del modelo.
         * Pasos:
         * 1. Limpiamos el Pane y caches visuales.
         * 2. Calculamos posiciones (lo hacemos en círculo para distribuir parejo).
         * 3. Dibujamos todas las aristas (líneas + flechas). Si hay paralelas, las separamos un poco.
         * 4. Dibujamos todos los nodos y les aplicamos color según si son origen/destino.
         *
         * Nota: aquí pensamos bastante cómo detectar rutas bidireccionales y cómo separar líneas paralelas.
         * Lo resolvimos agrupando por par inicio→destino y calculando un offset lateral con la perpendicular.
         */
        public void render(Grafo grafo, Parada origen, Parada destino) {
            // 1) Reset visual
            getChildren().clear();
            rutaVisualGrupos.clear();
            clearOverlays();
    
            // 2) Traemos paradas y si no hay, no dibujamos nada
            List<Parada> paradas = grafo.getParadasList();
            if (paradas == null || paradas.isEmpty()) return;
    
            // 3) Posiciones (nos funcionó un layout circular sencillo, sin fuerzas)
            Map<Parada, Point2D> pos = posicionesCirculares(paradas);
    
            // 4) Detectar pares bidireccionales reales (si existe A->B y B->A)
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
    
            // 5) Dibujar todas las aristas agrupando paralelas del mismo par inicio->destino
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
                        // Crear línea recortada + flechas. Esto nos llevó tiempo ajustar márgenes y flecha abierta.
                        Group g = crearArista(r, pos.get(r.getInicio()), pos.get(r.getDestino()), grupo.size(), i, bidir);
                        rutaVisualGrupos.put(r, g);
                        getChildren().add(g);
                    }
                }
            }
    
            // 6) Dibujar nodos. Primero en azul, luego sobrescribimos color si es origen/destino.
            for (Parada p : paradas) {
                Group nodo = crearNodoConNombre(p, pos.get(p));
                setNodeColor(nodo, COLOR_NODE_DEFAULT);  // default azul
    
                if (p == origen) setNodeColor(nodo, COLOR_NODE_ORIGEN);
                if (p == destino) setNodeColor(nodo, COLOR_NODE_DESTINO);
    
                getChildren().add(nodo);
            }
        }
    
        /* ================== Resaltado de camino ================== */
    
        /**
         * iluminarCamino
         * Objetivo: colorear el camino óptimo según el criterio elegido y poner etiquetas de tramo.
         * Lo que hacemos:
         * - Bajamos cualquier resaltado anterior.
         * - Para cada ruta del path, pintamos su línea y sus flechas con el color recibido.
         * - En el medio de cada segmento agregamos un cartelito con el nombre de la ruta (R1, R2...).
         *
         * Nota: decidimos subir el strokeWidth en el resaltado para que se note mejor sobre las grises.
         */
        public void iluminarCamino(List<Ruta> path, String colorHex) {
            clearCaminoIluminado();
            if (path == null || path.isEmpty()) return;
    
            Color c = Color.web(colorHex == null || colorHex.isBlank() ? COLOR_NODE_ORIGEN : colorHex);
    
            for (Ruta r : path) {
                Group g = rutaVisualGrupos.get(r);
                if (g == null) continue;
    
                // La línea es el child 0 del Group. Subimos grosor y cambiamos color.
                Line line = (Line) g.getChildren().get(0);
                line.getStyleClass().add("highlight");
                line.setStrokeWidth(3.4);
                line.setStyle("-fx-stroke: " + toHexColor(c) + ";");
    
                // Pintamos flecha del destino (child 1) y, si hay, flecha del origen (child 2 en bidireccionales).
                if (g.getChildren().size() > 1 && g.getChildren().get(1) instanceof Group arrowDst)
                    colorizeArrowGroup(arrowDst, c, 3.4);
                if (g.getChildren().size() > 2 && g.getChildren().get(2) instanceof Group arrowSrc)
                    colorizeArrowGroup(arrowSrc, c, 3.4);
    
                // Cartelito en el midpoint con el nombre de la ruta. Probamos arriba/abajo y al final
                // lo dejamos cerca del centro porque se lee mejor en la mayoría de ángulos.
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
    
        /**
         * clearCaminoIluminado
         * Objetivo: regresar todo a estado base (líneas grises finas, flechas grises y sin overlays).
         * Lo usamos antes de iluminar un camino nuevo para no mezclar estilos.
         */
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
    
        /**
         * clearOverlays
         * Objetivo: quitar los cartelitos del medio de cada segmento cuando limpiamos el resaltado.
         */
        private void clearOverlays() {
            for (StackPane sp : overlays) getChildren().remove(sp);
            overlays.clear();
        }
    
        /* ================== Helpers de aristas ================== */
    
        /**
         * key
         * Objetivo: generar una clave simple para el par inicio→destino usando hashCode.
         * Nos sirve para agrupar rutas paralelas del mismo par y también para detectar pares bidireccionales.
         */
        private String key(Parada a, Parada b) { return a.hashCode() + "->" + b.hashCode(); }
    
        /**
         * normalizePair
         * Objetivo: convertir cualquier “a->b” o “b->a” en la misma clave “a<>b” ordenada.
         * Así detectamos fácilmente si existe la ruta inversa.
         */
        private String normalizePair(String k) {
            String[] p = k.split("->");
            return (p[0].compareTo(p[1]) <= 0) ? p[0] + "<>" + p[1] : p[1] + "<>" + p[0];
        }
    
        /**
         * tieneRuta
         * Objetivo: chequear si hay una ruta exacta inicio→destino. Lo usamos para marcar bidireccionalidad real.
         */
        private boolean tieneRuta(Grafo grafo, Parada inicio, Parada destino) {
            List<Ruta> sal = grafo.getRutasDeSalida(inicio);
            if (sal == null) return false;
            for (Ruta r : sal) if (r.getDestino() == destino) return true;
            return false;
        }
    
        /**
         * crearArista
         * Objetivo: crear el Group con la línea recortada y una flecha abierta en la punta.
         * Detalles que nos dieron trabajo:
         * - Calcular la perpendicular para separar rutas paralelas. Sin eso se montan una encima de otra.
         * - Recortar la línea con margen para que no se meta en el nodo y para ubicar bien la flecha.
         * - Apuntar flechas correctas en ambos sentidos cuando es bidireccional.
         */
        private Group crearArista(Ruta r, Point2D A, Point2D B, int total, int index, boolean bidirectional) {
            // Dirección y unidad
            double dx = B.getX() - A.getX();
            double dy = B.getY() - A.getY();
            double len = Math.hypot(dx, dy);
            double ux = dx / (len == 0 ? 1 : len);
            double uy = dy / (len == 0 ? 1 : len);
            // Perpendicular (para separar paralelas)
            double px = -uy;
            double py = ux;
    
            // Separación entre paralelas. Calculamos un offset simétrico: -1, 0, +1, etc.
            double separation = 14.0;
            double offsetFactor = (total == 1) ? 0 : (index - (total - 1) / 2.0);
            double ox = px * offsetFactor * separation;
            double oy = py * offsetFactor * separation;
    
            // Margen para no tocar el nodo y espacio para ubicar la flecha sin que “se salga” de la línea
            double margin = NODE_RADIUS + LINE_MARGIN + HEAD_INSET;
    
            // Coordenadas finales de la línea recortada
            double startX = A.getX() + ox + ux * margin;
            double startY = A.getY() + oy + uy * margin;
            double endX   = B.getX() + ox - ux * margin;
            double endY   = B.getY() + oy - uy * margin;
    
            Line line = new Line(startX, startY, endX, endY);
            line.getStyleClass().add("graph-edge");
            line.setStrokeLineCap(StrokeLineCap.ROUND);
    
            // Flecha abierta apuntando al destino
            Group arrowDst = crearFlechaAbierta(endX, endY, ux, uy, px, py, ARROW_SIZE, 2.4);
            colorizeArrowGroup(arrowDst, Color.web("#6e7681"), 2.4);
            arrowDst.toFront();
    
            if (bidirectional) {
                // Si es bidireccional, también ponemos flecha “hacia atrás” en el origen.
                Group arrowSrc = crearFlechaAbierta(startX, startY, -ux, -uy, px, py, ARROW_SIZE, 2.4);
                colorizeArrowGroup(arrowSrc, Color.web("#6e7681"), 2.4);
                arrowSrc.toFront();
                return new Group(line, arrowDst, arrowSrc);
            } else {
                return new Group(line, arrowDst);
            }
        }
    
        /**
         * crearFlechaAbierta
         * Objetivo: construir la flecha tipo “<” como dos segmentos que salen de la punta.
         * Elegimos esta forma para que se vea ligera y no tape la línea principal.
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
    
        /**
         * colorizeArrowGroup
         * Objetivo: colorear y ajustar grosor de las líneas que forman la flecha abierta.
         * Lo usamos tanto para poner gris base como para el color del resaltado.
         */
        private void colorizeArrowGroup(Group arrowGroup, Color color, double width) {
            for (Node n : arrowGroup.getChildren()) {
                if (n instanceof Line l) {
                    l.setStroke(color);
                    l.setStrokeWidth(width);
                }
            }
        }
    
        /* ================== Nodos: círculo + nombre debajo ================== */
    
        /**
         * crearNodoConNombre
         * Objetivo: crear el círculo del nodo, agregarle un badge con la inicial del tipo si aplica,
         * y poner el nombre completo debajo, centrado.
         * Decisión visual: el nombre fuera del círculo se lee mejor y no compite con el badge.
         */
        private Group crearNodoConNombre(Parada p, Point2D pos) {
            // Círculo base del nodo
            StackPane circle = new StackPane();
            circle.setPrefSize(NODE_RADIUS * 2, NODE_RADIUS * 2);
            circle.getStyleClass().add("graph-node");
    
            // Badge del tipo (M/T/A). Si el tipo no encaja, simplemente no ponemos nada.
            String initial = getInitialFromTipo(p.getTipo());
            if (!initial.isEmpty()) {
                Label badge = new Label(initial);
                badge.setStyle("-fx-text-fill:white;-fx-font-size:10px;-fx-font-weight:800;"
                        + "-fx-background-color: rgba(0,0,0,0.35); -fx-background-radius:10;"
                        + "-fx-padding:2 6;");
                circle.getChildren().add(badge);
            }
    
            // Nombre de la parada debajo del círculo
            Label name = new Label(p.getNombre());
            name.setStyle("-fx-text-fill:#eaecef; -fx-font-size:11px; -fx-font-weight:600;");
            name.setWrapText(false);
    
            Group group = new Group(circle, name);
    
            // Posicionamos el círculo centrado en (pos) y el nombre debajo
            circle.setLayoutX(pos.getX() - NODE_RADIUS);
            circle.setLayoutY(pos.getY() - NODE_RADIUS);
    
            double labelY = pos.getY() + NODE_RADIUS + 2;
            name.setLayoutY(labelY);
            // Centrado horizontal del texto de nombre, sin calcularlo a ojo:
            name.layoutBoundsProperty().addListener((o, oldV, newV) -> {
                name.setLayoutX(pos.getX() - newV.getWidth() / 2);
            });
    
            // Click en el grupo: lo delegamos al callback para que el controlador decida la acción.
            group.setOnMouseClicked(e -> { if (clickListener != null) clickListener.onClick(p); });
    
            return group;
        }
    
        /*/**
         * getInitialFromTipo
         * Objetivo: a partir del tipo de parada, devolver una inicial para el badge:
         * M para metro, T para tren y A para autobus.
         */
        private String getInitialFromTipo(String tipoRaw) {
            if (tipoRaw == null) return "";
            // Como el tipo viene de la ComboBox (Tren, Metro, Autobus), lo mapeamos directo.
            switch (tipoRaw) {
                case "Metro":   return "M";
                case "Tren":    return "T";
                case "Autobus": return "A";
                default:        return "";
            }
        }
        /* ================== Utilidades de color/posiciones ================== */
    
        /**
         * setNodeColor
         * Objetivo: colorear el círculo del nodo con borde blanco. Lo usamos para default y para origen/destino.
         * Probamos sombras, pero el borde blanco quedó más nítido en fondos oscuros.
         */
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
    
        /**
         * toHexColor
         * Objetivo: convertir un Color de JavaFX a hex para usarlo en estilos inline.
         */
        private String toHexColor(Color c){
            int r = (int)Math.round(c.getRed()*255);
            int g = (int)Math.round(c.getGreen()*255);
            int b = (int)Math.round(c.getBlue()*255);
            return String.format("#%02x%02x%02x", r, g, b);
        }
    
        /**
         * posicionesCirculares
         * Objetivo: repartir las paradas alrededor de un círculo. Es simple pero efectivo
         * para tener una visual ordenada sin calcular fuerzas o evitar cruces complejos.
         * Detalles:
         * - Usamos el tamaño actual del Pane (o el prefSize si aún no se mide).
         * - El radio se ajusta al mínimo de ancho/alto con un padding para que no pegue al borde.
         */
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