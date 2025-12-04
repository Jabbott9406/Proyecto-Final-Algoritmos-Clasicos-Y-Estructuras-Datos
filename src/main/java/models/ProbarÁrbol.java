package models;

import java.util.List;

/**
 * ProbarÁrbol
 *
 * Un main  para ver que Prim y Kruskal trabajan con nuestro grafo.
 * Creamos unas paradas y rutas dirigidas, y mostramos el arbol mínimo por distancia.
 * Esto no toca la UI, solo imprime en consola.
 */
public class ProbarÁrbol {

    public static void main(String[] args) {
        Grafo grafo = Grafo.getInstance();

        // Paradas de ejemplo
        Parada a = new Parada("A", "Autobus");
        Parada b = new Parada("B", "Autobus");
        Parada c = new Parada("C", "Autobus");
        Parada d = new Parada("D", "Autobus");

        grafo.agregarParada(a);
        grafo.agregarParada(b);
        grafo.agregarParada(c);
        grafo.agregarParada(d);

        // Rutas dirigidas (algunas en ambos sentidos con pesos distintos)
        grafo.agregarRuta("R_AB", a, b, 4.0, 10, 2.0);
        grafo.agregarRuta("R_BA", b, a, 3.0, 8, 1.5);
        grafo.agregarRuta("R_AC", a, c, 5.0, 12, 3.0);
        grafo.agregarRuta("R_BC", b, c, 2.0, 4, 0.8);
        grafo.agregarRuta("R_CD", c, d, 3.0, 7, 1.2);
        grafo.agregarRuta("R_BD", b, d, 10.0, 20, 5.0);

        System.out.println("Probando arbol mínimo (interpretamos las rutas dirigidas como aristas no dirigidas con el menor peso entre sentidos).");

        // Prim por distancia
        Prim prim = new Prim();
        Prim.Resultado primRes = prim.sacarArbolMinimo(grafo, "distancia");
        if (primRes == null) {
            System.out.println("Prim: el grafo no conecta todo, no hay arbol completo.");
        } else {
            System.out.println("Prim (distancia) peso total: " + primRes.pesoTotal);
            imprimir(primRes.rutasDelArbol);
        }

        // Kruskal por distancia
        Kruskal kruskal = new Kruskal();
        Kruskal.Resultado krusRes = kruskal.sacarArbolMinimo(grafo, "distancia");
        if (krusRes == null) {
            System.out.println("Kruskal: el grafo no conecta todo, no hay arbol completo.");
        } else {
            System.out.println("Kruskal (distancia) peso total: " + krusRes.pesoTotal);
            imprimir(krusRes.rutasDelArbol);
        }

    }

    private static void imprimir(List<Ruta> rutas) {
        for (Ruta r : rutas) {
            System.out.printf(" - %s : %s -> %s (dist=%.2f)\n",
                    r.getNombre(),
                    r.getInicio() != null ? r.getInicio().getNombre() : "null",
                    r.getDestino() != null ? r.getDestino().getNombre() : "null",
                    r.getDistancia());
        }
    }
}