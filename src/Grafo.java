import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Grafo {

    Map<Parada, List<Ruta>> mapa;

    public Grafo() {
        mapa = new HashMap<>();
    }

    public Map<Parada, List<Ruta>> getMapa() {
        return mapa;
    }
    /**
     * Nombre: agregarParada
     * Paramétros: Parada.
     * Funcionamiento: Creación de nueva parada y agregación al HashMap.
     * Retorno: no retorna
     *
     */
    public void agregarParada(Parada parada) {
        if (parada == null) throw new IllegalArgumentException("Parada no puede ser null");
        mapa.putIfAbsent(parada, new ArrayList<>()); // Se crea la parada y la inicialización de un listado de rutas
        // dependientes a ella.
    }
    //Validar duplicados

    /**
     * Nombre: agregarRuta
     * Paramétros: String, Parada, Parada, double, double, double.
     * Funcionamiento: Creación de nueva ruta y agregación al HashMap.
     * Retorno: no retorna
     *
     */

    //revisar evento y transbordo
    public Ruta agregarRuta(String nombre, Parada inicio, Parada destino, double distancia, double tiempo, double costo) {
        if (inicio == null || destino == null) throw new IllegalArgumentException("inicio/destino no puede ser null");
        mapa.putIfAbsent(inicio, new ArrayList<>());
        mapa.putIfAbsent(destino, new ArrayList<>());
        Ruta nuevaRuta = new Ruta(nombre, inicio, destino, distancia, tiempo, costo);
        mapa.get(inicio).add(nuevaRuta);
        destino.agregarRutaDeEntrada(nuevaRuta);
        return nuevaRuta;
    }

    /**
     * Nombre: eliminarParada
     * Paramétros: Parada.
     * Funcionamiento: se elimina del grafo la parada seleccionada y las rutas que salen de esta
     * Retorno: no retorna
     *
     */
    public void eliminarParada(Parada parada) {
        if (parada == null) throw new IllegalArgumentException("Parada no puede ser null");
        if (!parada.getRutasDeEntrada().isEmpty()) {
            for (int i = parada.getRutasDeEntrada().size() - 1; i >= 0; i--) { // Se recorre el listado de rutas que entran a la parada
                eliminarRuta(parada.getRutasDeEntrada().get(i)); // Se eliminan las rutas mediante el llamado de la función.
            }
        }
        //Se eliminan todas las rutas que salen de la parada
        if (mapa.get(parada) != null) {
            for (int j = mapa.get(parada).size() - 1; j >= 0; j--) {
                eliminarRuta(mapa.get(parada).get(j));
            }
        }
        mapa.remove(parada); // Se elimina la parada del HashMap.

    }

    /**
     * Nombre: eliminarRuta
     * Paramétros: Ruta.
     * Funcionamiento: se elimina del grafo la ruta seleccionada
     * Retorno: no retorna
     *
     */
    public void eliminarRuta(Ruta ruta) {
        if (ruta == null) throw new IllegalArgumentException("Ruta no puede ser null");
        //Preguntar acerca de quedarse una parada sola
        mapa.get(ruta.getInicio()).remove(ruta); // Se remueve la ruta de la parada inicial.
        ruta.getDestino().eliminarRutaDeEntrada(ruta); // Se remueve la ruta de la parada final.
    }

    /**
     * Nombre: modificarRuta
     * Paramétros: Ruta, String, Parada, Parada, Double, Double, Double.
     * Funcionamiento: se modifican los parametros diferentes a null de la ruta proporcionada,
     * si no se desea modificar ciertos datos, entonces se coloca null para obviarlos.
     * Retorno: no retorna
     */
    public void modificarRuta(Ruta ruta, String nuevoNombre, Parada nuevoInicio, Parada nuevoDestino, Double nuevaDistancia, Double nuevoTiempo, Double nuevoCosto) {
        if (ruta == null) throw new IllegalArgumentException("Ruta no puede ser null");
        if (nuevoNombre != null) {
            ruta.setNombre(nuevoNombre);
        }
        if (nuevoInicio != null && nuevoInicio != ruta.getInicio()) {
            mapa.putIfAbsent(nuevoInicio, new ArrayList<>());
            mapa.get(ruta.getInicio()).remove(ruta); // Elimino la ruta actual del listado de rutas de la parada a cambiar
            ruta.setInicio(nuevoInicio); // Se realiza el cambio de la parada antigua por la nueva
            mapa.get(ruta.getInicio()).add(ruta); // Se agrega la ruta actaul al listado de rutas de la nueva parada
        }
        if (nuevoDestino != null && nuevoDestino != ruta.getDestino()) {
            mapa.putIfAbsent(nuevoDestino, new ArrayList<>());
            ruta.getDestino().eliminarRutaDeEntrada(ruta); // Se accede al listado de rutas que apuntan a la parada antigua y se elimina la ruta actual
            ruta.setDestino(nuevoDestino); // Se realiza el cambio de la parada antigua por la nueva
            nuevoDestino.agregarRutaDeEntrada(ruta); // Se accede al listado de rutas que apuntan a la parada nueva y se agrega la ruta actual
        }
        if (nuevaDistancia != null) {
            ruta.setDistancia((double) nuevaDistancia);
        }
        if (nuevoCosto != null) {
            ruta.setCosto((double) nuevoCosto);
        }
        if (nuevoTiempo != null) {
            ruta.setTiempo((double) nuevoTiempo);
        }

    }

    /**
     * Nombre: modificarParada
     * Paramétros: Parada, String.
     * Funcionamiento: se modifican los parametros de la parada proporcionada.
     * Retorno: no retorna
     */

    public void modificarParada(Parada parada, String nombre) {
        if (nombre != null && parada != null) {
            parada.setNombre(nombre);
        } else {
            throw new IllegalArgumentException("Parada/nombre no puede ser null");
        }

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

    public void mostrarMapa() {
        for (Parada parada : mapa.keySet()) {
            System.out.println(parada + "--> " + mapa.get(parada));
        }
    }

    public List<Ruta> getRutasDeSalida(Parada parada) {

        return mapa.get(parada);
    }

}




