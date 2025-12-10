package models;

import java.util.*;

/**
 * Nombre de la clase: Prim
 *
 * Objetivo:
 * - Armar un árbol de conexión mínima usando el algoritmo de Prim,
 *   pero aplicado a nuestro grafo de rutas (que es dirigido). Para que Prim tenga sentido,
 *   tratamos las rutas como “no dirigidas” tomando, entre dos paradas, el menor peso disponible.

 *
 * Entradas:
 * - grafo: con todas las paradas y rutas (dirigidas).
 * - filtro: criterio para medir el costo de cada ruta. Puede ser: "distancia", "tiempo" o "costo".
 *
 * Retorno:
 * - Un Resultado con la lista de rutas que quedaron dentro del arbol y la suma total del peso.
 * - Si el grafo no es conexo (no se puede unir todo), devuelve null para dejarlo claro.
 *
 * Nota:
 * - Esta clase no toca la UI. Es de apoyo para el tema de MST y para explicar el algoritmo.
 */
public class Prim {

    /**
     * Clase Resultado
     * Propósito: devolver de manera ordenada lo que produjo el algoritmo.
     * - rutasDelArbol: cuáles rutas originales quedaron dentro del MST (árbol mínimo).
     * - pesoTotal: la suma de pesos según el filtro elegido.
     */
    public static class Resultado {
        public final List<Ruta> rutasDelArbol; // Lista con el esqueleto del árbol
        public final double pesoTotal;         // Suma de pesos según filtro

        public Resultado(List<Ruta> rutasDelArbol, double pesoTotal) {
            this.rutasDelArbol = rutasDelArbol;
            this.pesoTotal = pesoTotal;
        }
    }

    /**
     * Aunque el grafo es dirigido, para Prim necesitamos tratar las conexiones como no dirigidas.
     * Aquí guardamos: los dos extremos (u, v), la Ruta original que representamos y su peso según el filtro.
     */
    private static class Arista {
        final Parada u, v;   // Extremos de la arista no dirigida
        final Ruta ruta;     // Ruta original del proyecto que elegimos para representar {u,v}
        final double peso;   // Peso (distancia/tiempo/costo) que usaremos en el algoritmo

        Arista(Parada u, Parada v, Ruta ruta, double peso) {
            this.u = u;
            this.v = v;
            this.ruta = ruta;
            this.peso = peso;
        }
    }

    /**
     * sacarArbolMinimo
     *
     * Objetivo:
     * - Hacer Prim sobre una vista no dirigida del grafo, usando el menor peso entre sentidos.
     *
     * Parámetros:
     * - grafo: el grafo con paradas y rutas dirigidas.
     * - filtro: "distancia", "tiempo" o "costo" (define cómo medimos el arbol).
     *
     * Retorno:
     * - Resultado con lista de rutas y peso total, o null si el grafo no conecta completo.
     */
    public Resultado sacarArbolMinimo(Grafo grafo, String filtro) {
        // Validaciones básicas
        if (grafo == null || filtro == null || filtro.isBlank()) {
            throw new IllegalArgumentException("grafo y filtro no pueden ser null o vacíos");
        }

        // Traemos todas las paradas del grafo
        List<Parada> paradas = grafo.getParadasList();
        // Si no hay paradas, el árbol está vacío y el total 0
        if (paradas.isEmpty()) return new Resultado(Collections.emptyList(), 0.0);
        // Con una sola parada, también es un árbol vacío, es decir, no hay aristas que agregar
        if (paradas.size() == 1) return new Resultado(Collections.emptyList(), 0.0);

        // Se Convierte el grafo dirigido en una vista no dirigida,
        // quedándonos con el menor peso entre dos paradas (si existen ambos sentidos).
        // Usamos un mapa clave {u<>v} -> Arista con el menor peso encontrado entre u y v.
        Map<String, Arista> mapaAristas = new HashMap<>();

        // Recorremos todas las rutas de salida de cada parada
        for (Parada p : paradas) {
            List<Ruta> salidas = grafo.getRutasDeSalida(p);
            if (salidas == null) continue; // Si no hay rutas saliendo de p, seguimos

            for (Ruta r : salidas) {
                if (!r.isEstado()) continue;      // Si la ruta está cerrada, la ignoramos
                Parada a = r.getInicio();         // Extremo A (inicio)
                Parada b = r.getDestino();        // Extremo B (destino)
                if (a == null || b == null) continue; // Por seguridad

                // Clave ordenada para que {a,b} y {b,a} caigan en la misma entrada
                String key = clavePareja(a, b);

                // Peso según el filtro elegido (distancia/tiempo/costo)
                double w = pesoPorFiltro(r, filtro);

                // Si no hay nada guardado aún para este par o encontramos algo más barato, lo reemplazamos
                Arista vieja = mapaAristas.get(key);
                if (vieja == null || w < vieja.peso) {
                    mapaAristas.put(key, new Arista(a, b, r, w));
                }
            }
        }

        //Se construyen las adyacencias para correr Prim más cómodo.
        // adj[X] = lista de aristas (no dirigidas) que tocan a la parada X.
        Map<Parada, List<Arista>> adj = new HashMap<>();
        for (Arista a : mapaAristas.values()) {
            adj.computeIfAbsent(a.u, k -> new ArrayList<>()).add(a);
            adj.computeIfAbsent(a.v, k -> new ArrayList<>()).add(a);
        }

        // Ejecutar Prim.
        // Estructuras que vamos a usar:
        Set<Parada> visitadas = new HashSet<>(); // Paradas ya metidas al árbol
        List<Ruta> arbol = new ArrayList<>();    // Las rutas que van quedando dentro del MST
        double total = 0.0;                      // Suma de pesos (según el filtro)

        // Empezamos desde la primera parada, (puede ser cualquiera)
        Parada inicio = paradas.get(0);
        visitadas.add(inicio);

        // Cola de prioridad
        PriorityQueue<Arista> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.peso));
        // Cargamos todas las aristas que salen del “inicio”
        pq.addAll(adj.getOrDefault(inicio, Collections.emptyList()));

        // Bucle principal de Prim:
        // Mientras haya aristas por considerar y no hayamos cubierto todas las paradas…
        while (!pq.isEmpty() && visitadas.size() < paradas.size()) {
            // Sacamos la arista con menor peso
            Arista e = pq.poll();

            // Determinamos a qué lado nuevo conecta esta arista
            Parada otra = null;
            // Caso 1: u ya está en el árbol y v aún no -> podemos crecer hacia v
            if (visitadas.contains(e.u) && !visitadas.contains(e.v)) {
                otra = e.v;
            }
            // Caso 2: v ya está en el árbol y u aún no -> podemos crecer hacia u
            else if (visitadas.contains(e.v) && !visitadas.contains(e.u)) {
                otra = e.u;
            }
            // Caso 3: si ambos extremos ya estaban (o ninguno aplica), esta arista no sirve para crecer; pasamos
            else {
                continue;
            }

            // Agregamos la nueva parada al conjunto visitado
            visitadas.add(otra);
            // Guardamos la Ruta original que representa esta arista dentro del árbol
            arbol.add(e.ruta);
            // Sumamos el peso de la arista
            total += e.peso;

            // Cargamos a la cola todas las aristas que salgan de otra y que lleven a paradas no visitadas
            for (Arista sig : adj.getOrDefault(otra, Collections.emptyList())) {
                Parada candidato = sig.u.equals(otra) ? sig.v : sig.u;
                if (!visitadas.contains(candidato)) {
                    pq.add(sig);
                }
            }
        }

        // Se Verifica la conectividad.
        // Si no visitamos todas las paradas, significa que el grafo no es conexo
        // y por lo tanto no existe un MST que cubra a todos.
        if (visitadas.size() != paradas.size()) {
            return null;
        }

        // Si llegamos hasta aquí, armamos el resultado con el esqueleto del árbol y su costo total
        return new Resultado(arbol, total);
    }

    /**
     * clavePareja
     * Objetivo: generar una clave ordenada para el par de paradas (a, b),
     * de forma que clavePareja(a,b) == clavePareja(b,a).
     */
    private String clavePareja(Parada a, Parada b) {
        int ha = a.hashCode();
        int hb = b.hashCode();
        // Si ha <= hb: “ha<>hb”, si no, al revés. Así {a,b} y {b,a} caen en la misma llave.
        return ha <= hb ? ha + "<>" + hb : hb + "<>" + ha;
    }

    /**
     * pesoPorFiltro
     * Objetivo: según el filtro elegido, devolver el valor correcto de la Ruta.
     * - "distancia" -> getDistancia()
     * - "tiempo"    -> getTiempo()
     * - "costo"     -> getCosto()
     * Si el filtro no es válido, lanzamos IllegalArgumentException para avisar.
     */
    private double pesoPorFiltro(Ruta r, String filtro) {
        return switch (filtro.toLowerCase(Locale.ROOT)) {
            case "distancia" -> r.getDistancia();
            case "tiempo"    -> r.getTiempo();
            case "costo"     -> r.getCosto();
            default -> throw new IllegalArgumentException("Filtro inválido: " + filtro);
        };
    }
}