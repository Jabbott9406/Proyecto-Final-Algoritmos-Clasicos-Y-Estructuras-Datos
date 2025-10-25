import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Grafo {


    Map<Parada, List<Ruta>> mapa;

    public Grafo() {
        mapa = new HashMap<>();
        Random rand = new Random();
    }

    public void agregarParada(Parada parada, List<Ruta> rutas) {
        mapa.put(parada, rutas);
    }


    /**
     * Nombre: simularEventos
     * Parámetros: ninguno
     * Funcionamiento: recorre todas las rutas del mapa y simula eventos aleatorios
     * que pueden afectar su estado o tiempo de recorrido. Los eventos posibles son:
     * <p>
     * 1) Accidente: deshabilita la ruta y marca el evento.
     * 2) Retraso: incrementa el tiempo de la ruta en un 50%.
     * 3) Lluvia: incrementa el tiempo de la ruta en un 20%.
     * 4) Normal: no altera la ruta.
     * <p>
     * Probabilidades de ocurrencia:
     * - Accidente: 10%
     * - Retraso: 15%
     * - Lluvia: 10%
     * - Normal: 65%
     * <p>
     * Retorno: no retorna ningún valor.
     */

    public void simularEventos() {
        Random rand = new Random();

        for (Parada p : mapa.keySet()) {
            for (Ruta r : mapa.get(p)) {

                int prob = rand.nextInt(100) + 1;
                int categoria;

                if (prob <= 10) categoria = 1;
                else if (prob <= 25) categoria = 2;
                else if (prob <= 35) categoria = 3;
                else categoria = 4;

                switch (categoria) {
                    case 1:
                        r.setEstado(false);
                        r.setEvento("Accidente");
                        System.out.println("Ha ocurrido un accidente en " + r.getNombre() +
                                " (" + r.getInicio().getNombre() + " → " + r.getDestino().getNombre() + ")");
                        break;

                    case 2:
                        r.setEstado(true);
                        r.setEvento("Retraso");
                        System.out.println("Hay retraso en " + r.getNombre());
                        r.setTiempo(r.getTiempo() * 1.5);
                        break;

                    case 3:
                        r.setEstado(true);
                        r.setEvento("Lluvia");
                        System.out.println("Lluvia en " + r.getNombre());
                        r.setTiempo(r.getTiempo() * 1.2);
                        break;

                    case 4:
                        r.setEstado(true);
                        r.setEvento("Normal");
                        break;
                }
            }
        }
    }
}