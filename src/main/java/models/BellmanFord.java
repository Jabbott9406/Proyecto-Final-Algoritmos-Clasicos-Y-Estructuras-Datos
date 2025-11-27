package models;

import java.text.Normalizer;
import java.util.*;

public class BellmanFord {

    public static final double INFINITO = Double.POSITIVE_INFINITY;

    public RutaMasCorta calcular(Grafo grafo, Parada inicio, Parada destino, String filtro) {
        if (grafo == null || inicio == null || destino == null || (filtro == null || filtro.isBlank())) {
            throw new IllegalArgumentException("grafo/inicio/destino/filtro no pueden ser null");
        }

        // NUEVO: si el filtro es "transbordos", usamos la variante de transbordos y retornamos
        if ("transbordos".equalsIgnoreCase(filtro)) {
            return calcularMinTransbordos(grafo, inicio, destino);
        }

        List<Parada> paradas = new ArrayList<>(grafo.getMapa().keySet());
        Map<Parada, Double> distancia = new HashMap<>();
        Map<Parada, Ruta> predecesor = new HashMap<>();

        // Inicialización
        for (Parada p : paradas) {
            distancia.put(p, INFINITO);
        }
        distancia.put(inicio, 0.0);

        // Relajación |V|-1 veces
        for (int i = 0; i < paradas.size() - 1; i++) {
            for (Parada u : paradas) {
                List<Ruta> rutas = grafo.getRutasDeSalida(u);
                if (rutas == null) continue;

                for (Ruta r : rutas) {
                    if (!r.isEstado()) continue;
                    Parada v = r.getDestino();
                    double peso = getPesoPorFiltro(r, filtro);

                    if (distancia.get(u) + peso < distancia.get(v)) {
                        distancia.put(v, distancia.get(u) + peso);
                        predecesor.put(v, r);
                    }
                }
            }
        }

        // Verificar ciclos negativos
        for (Parada u : paradas) {
            List<Ruta> rutas = grafo.getRutasDeSalida(u);
            if (rutas == null) continue;

            for (Ruta r : rutas) {
                if (!r.isEstado()) continue;
                Parada v = r.getDestino();
                double peso = getPesoPorFiltro(r, filtro);

                if (distancia.get(u) + peso < distancia.get(v)) {
                    throw new IllegalStateException("El grafo contiene un ciclo negativo");
                }
            }
        }

        // Reconstrucción del camino
        List<Ruta> rutaCorta = reconstruirCamino(predecesor, inicio, destino);
        if (rutaCorta == null) return null;

        // Cálculos totales
        double totalTiempo = 0, totalCosto = 0, totalDistancia = 0, totalPeso = 0;
        String evento = null;

        for (Ruta r : rutaCorta) {
            totalTiempo += r.getTiempo();
            totalCosto += r.getCosto();
            totalDistancia += r.getDistancia();
            totalPeso += getPesoPorFiltro(r, filtro);

            if (evento == null && r.getEvento() != null && !r.getEvento().isBlank()) {
                evento = r.getEvento();
            }
        }

        // Si es por distancia - - - >No aplica
        if (filtro.equalsIgnoreCase("distancia")) {
            evento = "No aplica";
        }

        return new RutaMasCorta(rutaCorta, totalTiempo, totalCosto, totalDistancia, totalPeso, filtro, evento);
    }

    private double getPesoPorFiltro(Ruta r, String filtro) {
        switch (filtro.toLowerCase()) {
            case "distancia": return r.getDistancia();
            case "tiempo": return r.getTiempo();
            case "costo": return r.getCosto();
            default: throw new IllegalArgumentException("Filtro inválido: " + filtro);
        }
    }

    private List<Ruta> reconstruirCamino(Map<Parada, Ruta> predecesor, Parada inicio, Parada destino) {
        LinkedList<Ruta> camino = new LinkedList<>();
        Parada actual = destino;

        while (actual != null && actual != inicio) {
            Ruta r = predecesor.get(actual);
            if (r == null) return null; // No hay camino
            camino.addFirst(r);
            actual = r.getInicio();
        }

        if (!camino.isEmpty() && camino.getFirst().getInicio() != inicio) return null;
        return camino;
    }

    // ---------------- TRANSBORDOS ----------------
    private RutaMasCorta calcularMinTransbordos(Grafo grafo, Parada inicio, Parada destino) {
        final double BIG = 1_000_000.0; // prioriza transbordos sobre distancia

        // línea = TIPO de la parada INICIO del tramo (si no hay, probamos destino; si tampoco, usamos nombre de la ruta)
        java.util.function.Function<Ruta, String> lineaDe = r -> {
            String tipo = null;
            if (r.getInicio() != null) tipo = r.getInicio().getTipo();
            if ((tipo == null || tipo.isBlank()) && r.getDestino() != null) tipo = r.getDestino().getTipo();
            if (tipo == null || tipo.isBlank()) tipo = r.getNombre();

            String s = Normalizer.normalize(tipo, Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                    .toLowerCase(Locale.ROOT)
                    .trim();
            return s.isBlank() ? "-" : s;
        };
        java.util.function.BiFunction<Parada, String, String> key = (p, lin) -> p.hashCode() + "|" + (lin == null ? "-" : lin);

        Map<Parada, Set<String>> lineasEnParada = new HashMap<>();
        for (Parada u : grafo.getMapa().keySet()) {
            List<Ruta> sal = grafo.getRutasDeSalida(u);
            if (sal == null) continue;
            for (Ruta r : sal) {
                Parada v = r.getDestino();
                lineasEnParada.computeIfAbsent(v, k -> new HashSet<>()).add(lineaDe.apply(r));
            }
        }
        lineasEnParada.computeIfAbsent(inicio, k -> new HashSet<>()).add("-");

        class Edge {
            String fromKey, toKey; double w; Ruta ruta;
            Edge(String f, String t, double w, Ruta r){fromKey=f;toKey=t;this.w=w;this.ruta=r;}
        }

        List<Edge> edges = new ArrayList<>();
        Set<String> estados = new HashSet<>();
        Map<String, Parada> estadoParada = new HashMap<>();

        for (Parada p : grafo.getMapa().keySet()) {
            Set<String> prevLines = new HashSet<>(lineasEnParada.getOrDefault(p, Collections.emptySet()));
            if (p == inicio) prevLines.add("-");
            List<Ruta> sal = grafo.getRutasDeSalida(p);
            if (sal == null) continue;
            for (Ruta r : sal) {
                if (!r.isEstado()) continue;
                String lr = lineaDe.apply(r);
                Parada v = r.getDestino();
                for (String lp : prevLines) {
                    int cambio = ("-".equals(lp) || lp.equals(lr)) ? 0 : 1;
                    double w = cambio * BIG + r.getDistancia(); // desempate por distancia
                    String fk = key.apply(p, lp);
                    String tk = key.apply(v, lr);
                    edges.add(new Edge(fk, tk, w, r));
                    estados.add(fk); estados.add(tk);
                    estadoParada.put(fk, p); estadoParada.put(tk, v);
                }
            }
        }

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> predSt = new HashMap<>();
        Map<String, Ruta> predRt = new HashMap<>();
        for (String s : estados) dist.put(s, INFINITO);
        String startKey = key.apply(inicio, "-");
        dist.put(startKey, 0.0);

        int S = estados.size();
        for (int i = 0; i < S - 1; i++) {
            boolean cambio = false;
            for (Edge e : edges) {
                double dv = dist.get(e.fromKey);
                if (dv == INFINITO) continue;
                double nv = dv + e.w;
                if (nv < dist.get(e.toKey)) {
                    dist.put(e.toKey, nv);
                    predSt.put(e.toKey, e.fromKey);
                    predRt.put(e.toKey, e.ruta);
                    cambio = true;
                }
            }
            if (!cambio) break;
        }

        String bestKey = null;
        double bestVal = INFINITO;
        for (String s : estados) {
            Parada p = estadoParada.get(s);
            if (p != null && p.equals(destino)) {
                double v = dist.getOrDefault(s, INFINITO);
                if (v < bestVal) { bestVal = v; bestKey = s; }
            }
        }
        if (bestKey == null || bestVal == INFINITO) return null;

        LinkedList<Ruta> camino = new LinkedList<>();
        String cur = bestKey;
        while (cur != null && !cur.equals(startKey)) {
            Ruta r = predRt.get(cur);
            if (r == null) break;
            camino.addFirst(r);
            cur = predSt.get(cur);
        }
        if (camino.isEmpty()) return null;

        double totalTiempo = 0, totalCosto = 0, totalDistancia = 0;
        for (Ruta r : camino) {
            totalTiempo += r.getTiempo();
            totalCosto += r.getCosto();
            totalDistancia += r.getDistancia();
        }
        int trans = 0;
        String prevLin = null;
        for (Ruta r : camino) {
            String lin = lineaDe.apply(r);
            if (prevLin != null && !prevLin.equals(lin)) trans++;
            prevLin = lin;
        }
        String evento = "Normal";
        for (Ruta r : camino) {
            if (r.getEvento() != null && !r.getEvento().isBlank() && !"Normal".equalsIgnoreCase(r.getEvento())) {
                evento = r.getEvento(); break;
            }
        }

        RutaMasCorta rm = new RutaMasCorta(camino, totalTiempo, totalCosto, totalDistancia, trans, "transbordos", evento);
        rm.setTransbordos(trans);
        return rm;
    }
}