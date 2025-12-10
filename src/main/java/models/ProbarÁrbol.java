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
        Parada e = new Parada("E", "Autobus");
        Parada f = new Parada("F", "Autobus");
        Parada g = new Parada("G", "Autobus");
        Parada h = new Parada("H", "Autobus");
        Parada i = new Parada("I", "Autobus");
        Parada j = new Parada("J", "Autobus");
        Parada k = new Parada("K", "Autobus");

        grafo.agregarParada(a);
        grafo.agregarParada(b);
        grafo.agregarParada(c);
        grafo.agregarParada(d);
        grafo.agregarParada(e);
        grafo.agregarParada(f);
        grafo.agregarParada(g);
        grafo.agregarParada(h);
        grafo.agregarParada(i);
        grafo.agregarParada(j);
        grafo.agregarParada(k);

        // Rutas dirigidas (algunas en ambos sentidos con pesos distintos)
        grafo.agregarRuta("R_AB", a, b, 8, 10, 2.0);
        grafo.agregarRuta("R_AG", a, g, 9, 10, 2.0);
        grafo.agregarRuta("R_AK", a, k, 3, 10, 2.0);
        grafo.agregarRuta("R_AH", a, h, 10, 10, 2.0);
        grafo.agregarRuta("R_AI", a, i, 6, 10, 2.0);
        grafo.agregarRuta("R_AJ", a, j, 12, 10, 2.0);
        grafo.agregarRuta("R_BC", b, c, 10, 4, 0.8);
        grafo.agregarRuta("R_BK", b, k, 7, 4, 0.8);
        grafo.agregarRuta("R_BE", b, e, 2, 4, 0.8);

        grafo.agregarRuta("R_CD", c, d, 9, 7, 1.2);
        grafo.agregarRuta("R_CK", c, k, 5, 7, 1.2);

        grafo.agregarRuta("R_DE", d, e, 13, 7, 1.2);
        grafo.agregarRuta("R_DF", d, f, 12, 7, 1.2);

        grafo.agregarRuta("R_EF", e, f, 10, 7, 1.2);
        grafo.agregarRuta("R_EG", e, g, 6, 7, 1.2);

        grafo.agregarRuta("R_FG", f, g, 8, 7, 1.2);

        grafo.agregarRuta("R_GH", g, h, 7, 7, 1.2);

        grafo.agregarRuta("R_IA", i, a, 10, 7, 1.2);

        grafo.agregarRuta("R_JK", j, k, 8, 7, 1.2);



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