package models;

import java.util.*;

public class BellmanFord {

    public static final double INFINITO = Double.POSITIVE_INFINITY;

    public RutaMasCorta calcular(Grafo grafo, Parada inicio, Parada destino, String filtro) {
        if (grafo == null || inicio == null || destino == null || (filtro == null || filtro.isBlank())) {
            throw new IllegalArgumentException("grafo/inicio/destino/filtro no pueden ser null");
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
}
