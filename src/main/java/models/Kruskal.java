package models;

import java.util.*;

/**
 * Nombre de la clase: Kruskal
 *
 * Objetivo :
 * - Construir un Árbol de Expansión Mínima (MST) con el algoritmo de Kruskal
 *   a partir de nuestro grafo de rutas que es dirigido. Para poder usar Kruskal,
 *   convertimos las rutas a aristas no dirigidas quedándonos con el menor peso
 *   entre dos paradas.
 *

 * Entradas:
 * - grafo: el grafo con paradas y rutas dirigidas.
 * - filtro: "distancia", "tiempo" o "costo" (define el peso a comparar).
 *
 * Retorno:
 *   - rutasDelArbol: las rutas originales que quedaron dentro del MST.
 *   - pesoTotal: suma de los pesos de esas rutas según el filtro.
 * - Si el grafo no conecta todas las paradas, devolvemos null.
 *
 * Nota:
 * - Esta clase no toca UI.
 */
public class Kruskal {

    /**
     * Clase Resultado
     * Objetivo: entregar el resumen de lo que armó Kruskal.
     */
    public static class Resultado {
        public final List<Ruta> rutasDelArbol; // Las rutas originales que componen el MST
        public final double pesoTotal;         // La suma total de pesos (según filtro)

        public Resultado(List<Ruta> rutasDelArbol, double pesoTotal) {
            this.rutasDelArbol = rutasDelArbol;
            this.pesoTotal = pesoTotal;
        }
    }

    /**
     * - Aunque el grafo es dirigido, para Kruskal usamos pares {u,v} sin dirección.
     * - Guardamos cuál Ruta original representa este par y su peso según el filtro.
     */
    private static class Arista {
        final Parada u, v;   // Extremos del par (no dirigido)
        final Ruta ruta;     // Ruta original que elegimos para {u,v} (la de menor peso)
        final double peso;   // Peso de esta conexión para comparar en Kruskal

        Arista(Parada u, Parada v, Ruta ruta, double peso) {
            this.u = u;
            this.v = v;
            this.ruta = ruta;
            this.peso = peso;
        }
    }

    /**
     * Objetivo: detectar si al unir dos nodos se forma un ciclo. Mantiene conjuntos disjuntos
     * de paradas. Si dos paradas ya están en el mismo conjunto y las intentamos unir, eso formaría ciclo.
     *
     * Conceptos rápidos:
     * - padre[x] = representante inmediato de x (si padre[x] = x, x es raíz).
     * - buscar(x) = devuelve la raíz del conjunto de x (con compresión de caminos).
     * - unir(a,b) = une los conjuntos de a y b (usamos rango para balancear).
     */
    private static class UF {
        private final Map<Parada, Parada> padre = new HashMap<>(); // Mapa de padre por nodo
        private final Map<Parada, Integer> rango = new HashMap<>(); // Altura aproximada para balanceo

        UF(Collection<Parada> nodos) {
            // Inicialmente, cada parada es su propio padre
            for (Parada p : nodos) {
                padre.put(p, p);
                rango.put(p, 0);
            }
        }

        // apuntamos cada nodo directamente a la raíz para acelerar futuras búsquedas.
        Parada buscar(Parada x) {
            Parada p = padre.get(x);
            if (p == null) return null;
            if (!p.equals(x)) {              // Si x no es raíz, subimos recursivamente hasta la raíz
                Parada raiz = buscar(p);
                padre.put(x, raiz);
                return raiz;
            }
            return p;                         // x es raíz
        }

        // Unir dos conjuntos. Devuelve true si se unieron, es decir, no había ciclo; false si ya estaban unidos.
        boolean unir(Parada a, Parada b) {
            Parada ra = buscar(a), rb = buscar(b);
            if (ra == null || rb == null) return false; // validación
            if (ra.equals(rb)) return false;            // Ya están en el mismo conjunto por lo que unirlos haría ciclo

            int rra = rango.getOrDefault(ra, 0);
            int rrb = rango.getOrDefault(rb, 0);

            // Unión por rango: colgamos la raíz más pequeña debajo de la más grande
            if (rra < rrb) {
                padre.put(ra, rb);
            } else if (rra > rrb) {
                padre.put(rb, ra);
            } else {
                // Si empatan, elegimos una como raíz y aumentamos su rango
                padre.put(rb, ra);
                rango.put(ra, rra + 1);
            }
            return true; // Se unieron sin formar ciclo
        }
    }

    /**
     * sacarArbolMinimo (Kruskal)
     *
     * Objetivo:
     * - Genera la lista de aristas no dirigidas tomando el menor peso para cada par {u,v}.
     * - Ordena esas aristas por peso ascendente.
     * - Recorre la lista y va metiendo aristas que NO formen ciclo.
     * - Si logra conectar todas las paradas con N-1 aristas, devuelve el MST. Si no, null.
     */
    public Resultado sacarArbolMinimo(Grafo grafo, String filtro) {
        // Validaciones mínimas
        if (grafo == null) throw new IllegalArgumentException("grafo no puede ser null");
        if (filtro == null || filtro.isBlank()) throw new IllegalArgumentException("filtro inválido");

        // Traemos todas las paradas
        List<Parada> paradas = grafo.getParadasList();
        // Casos borde: 0 o 1 parada => no hay aristas que agregar
        if (paradas.isEmpty()) return new Resultado(Collections.emptyList(), 0.0);
        if (paradas.size() == 1) return new Resultado(Collections.emptyList(), 0.0);

        //Se construyen aristas no dirigidas con el menor peso por par {u,v}

        Map<String, Arista> mapaAristas = new HashMap<>();

        for (Parada p : paradas) {
            List<Ruta> sal = grafo.getRutasDeSalida(p);
            if (sal == null) continue; // Si no salen rutas de p, seguimos

            for (Ruta r : sal) {
                Parada a = r.getInicio();             // Extremo A
                Parada b = r.getDestino();            // Extremo B
                if (a == null || b == null) continue; // validación

                String key = clavePareja(a, b);       // Clave ordenada para {a,b}
                double w = pesoPorFiltro(r, filtro);  // Peso según el filtro

                // Si no hay arista guardada aún para este par, o encontramos una más barata, la reemplazamos
                Arista vieja = mapaAristas.get(key);
                if (vieja == null || w < vieja.peso) {
                    mapaAristas.put(key, new Arista(a, b, r, w));
                }
            }
        }

        // Se pasa el mapa a lista y ordenamos por peso (ascendente)
        List<Arista> aristas = new ArrayList<>(mapaAristas.values());
        aristas.sort(Comparator.comparingDouble(e -> e.peso));

        // Preparamos Union-Find y estructuras para construir el MST
        UF uf = new UF(paradas);        // Cada parada empieza en su propio conjunto
        List<Ruta> arbol = new ArrayList<>(); // Rutas que irán al MST
        double total = 0.0;             // Acumulador del peso total

        // Recorremos de la más barata a la más cara
        for (Arista e : aristas) {
            // Si unir(u,v) devuelve true, significa que no había ciclo; podemos meter la arista al MST
            if (uf.unir(e.u, e.v)) {
                arbol.add(e.ruta);  // Guardamos la Ruta original correspondiente
                total += e.peso;    // Sumamos su peso

                // Si ya alcanzamos N-1 aristas, el MST está completo y podemos cortar temprano
                if (arbol.size() == paradas.size() - 1) break;
            }
            // Si unir() devuelve false, era un ciclo: la saltamos y seguimos
        }

        // Verificar que todo quedó conectado, es decir, todas las paradas pertenecen al mismo conjunto
        Parada rep = uf.buscar(paradas.get(0)); // Representante de la primera parada
        for (Parada p : paradas) {
            if (!Objects.equals(uf.buscar(p), rep)) {
                // Si encontramos alguna en un conjunto distinto, no hay MST que cubra todo
                return null;
            }
        }

        // Se devuelve el arbol y su costo total
        return new Resultado(arbol, total);
    }

    /**
     * clavePareja
     * Objetivo: generar una clave ordenada para el par (a,b), de modo que
     * clavePareja(a,b) == clavePareja(b,a). Así agrupamos correctamente las dos direcciones.
     */
    private String clavePareja(Parada a, Parada b) {
        int ha = a.hashCode();
        int hb = b.hashCode();
        return ha <= hb ? ha + "<>" + hb : hb + "<>" + ha;
    }

    /**
     * pesoPorFiltro
     * Objetivo: devolver el valor de la ruta que corresponde al filtro elegido.
     * - "distancia" -> getDistancia()
     * - "tiempo"    -> getTiempo()
     * - "costo"     -> getCosto()
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