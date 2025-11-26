package models;

import java.util.*;

public class FloydWarshall {

    public static Map<Parada, Map<Parada, List<Ruta>>> calcular(Grafo grafo, String filtro) {

        List<Parada> paradas = grafo.getParadasList();
        int n = paradas.size();

        double[][] dist = new double[n][n];
        List<Ruta>[][] next = new List[n][n];

        // Inicializar matriz y resetear rutas
        for (int i = 0; i < n; i++) {
            Parada pi = paradas.get(i);
            for (Ruta r : grafo.getRutasDeSalida(pi)) {
                r.resetValores(); // Evita acumulación de penalizaciones
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                    next[i][j] = new ArrayList<>();
                } else {
                    dist[i][j] = Double.POSITIVE_INFINITY;
                    next[i][j] = null;
                }
            }
        }

        // Cargar rutas del grafo
        for (int i = 0; i < n; i++) {
            Parada p = paradas.get(i);

            for (Ruta r : grafo.getRutasDeSalida(p)) {

                if (!r.isEstado()) continue; // Ignorar rutas cerradas

                int j = paradas.indexOf(r.getDestino());

                double peso = switch (filtro.toLowerCase()) {
                    case "distancia" -> r.getDistancia();
                    case "tiempo" -> r.getTiempo();
                    case "costo" -> r.getCosto();
                    default -> throw new IllegalArgumentException("Filtro inválido");
                };

                if (peso < dist[i][j]) {
                    dist[i][j] = peso;
                    next[i][j] = new ArrayList<>(List.of(r));
                }
            }
        }

        // Algoritmo Floyd-Warshall
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];

                        List<Ruta> nuevoCamino = new ArrayList<>();
                        if (next[i][k] != null) nuevoCamino.addAll(next[i][k]);
                        if (next[k][j] != null) nuevoCamino.addAll(next[k][j]);

                        next[i][j] = nuevoCamino;
                    }
                }
            }
        }

        // Convertir a mapa
        Map<Parada, Map<Parada, List<Ruta>>> resultado = new HashMap<>();
        for (int i = 0; i < n; i++) {
            Parada pi = paradas.get(i);
            resultado.put(pi, new HashMap<>());
            for (int j = 0; j < n; j++) {
                Parada pj = paradas.get(j);
                if (next[i][j] != null && !next[i][j].isEmpty()) {
                    resultado.get(pi).put(pj, next[i][j]);
                }
            }
        }

        return resultado;
    }
}
