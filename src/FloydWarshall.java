import java.util.*;

public class FloydWarshall {

    public static Map<Parada, Map<Parada, List<Parada>>> calcular(Grafo grafo, String filtro) {
        List<Parada> paradas = new ArrayList<>(grafo.getMapa().keySet());
        int n = paradas.size();
        double[][] dist = new double[n][n];
        Parada[][] next = new Parada[n][n];
        Map<Parada, Integer> indice = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indice.put(paradas.get(i), i);
        }

        // Inicialización
        for (int i = 0; i < n; i++) {
            Arrays.fill(dist[i], Double.POSITIVE_INFINITY);
            dist[i][i] = 0;
            for (int j = 0; j < n; j++) next[i][j] = null;
        }

        // Rellenar distancias iniciales y next según el filtro
        for (Parada origen : paradas) {
            int i = indice.get(origen);
            List<Ruta> rutas = grafo.getRutasDeSalida(origen);
            if (rutas != null) {
                for (Ruta r : rutas) {
                    int j = indice.get(r.getDestino());
                    double peso;
                    switch (filtro.toLowerCase()) {
                        case "distancia":
                            peso = r.getDistancia();
                            break;
                        case "tiempo":
                            peso = r.getTiempo();
                            break;
                        case "costo":
                            peso = r.getCosto();
                            break;
                        default:
                            throw new IllegalArgumentException("Filtro inválido: " + filtro);
                    }
                    dist[i][j] = peso;
                    next[i][j] = r.getDestino();
                }
            }
        }

        // Floyd–Warshall
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

        // Convertir a mapas legibles
        Map<Parada, Map<Parada, List<Parada>>> resultado = new HashMap<>();
        for (int i = 0; i < n; i++) {
            Map<Parada, List<Parada>> rutasDesde = new HashMap<>();
            for (int j = 0; j < n; j++) {
                List<Parada> ruta = new ArrayList<>();
                if (next[i][j] != null) {
                    int actual = i;
                    while (actual != j) {
                        ruta.add(paradas.get(actual));
                        actual = indice.get(next[actual][j]);
                    }
                    ruta.add(paradas.get(j));
                }
                rutasDesde.put(paradas.get(j), ruta);
            }
            resultado.put(paradas.get(i), rutasDesde);
        }

        return resultado;
    }


    //Auxiliar para mostrar las rutas calculadas
    public static void mostrarRutas(Grafo grafo, String filtro) {
        Map<Parada, Map<Parada, List<Parada>>> rutas = calcular(grafo, filtro);
        for (Parada origen : rutas.keySet()) {
            System.out.println("\nDesde " + origen.getNombre() + ":");
            for (Parada destino : rutas.get(origen).keySet()) {
                List<Parada> ruta = rutas.get(origen).get(destino);
                if (ruta.isEmpty()) {
                    System.out.println("  hasta " + destino.getNombre() + " = No hay ruta");
                } else {
                    System.out.print("  hasta " + destino.getNombre() + " = ");
                    for (int i = 0; i < ruta.size(); i++) {
                        System.out.print(ruta.get(i).getNombre());
                        if (i < ruta.size() - 1) System.out.print(" -> ");
                    }
                    System.out.println();
                }
            }
        }
    }
}
