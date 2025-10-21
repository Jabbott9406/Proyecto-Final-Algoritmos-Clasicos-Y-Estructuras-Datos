import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grafo {

    Map<Parada, List<Ruta>> mapa;

    public Grafo() {
        mapa = new HashMap<>();
    }
    /**
     *  Nombre: agregarParada
     *  Paramétros: Parada.
     *  Funcionamiento: Creación de nueva parada y agregación al HashMap.
     *  Retorno: no retorna
     * */
    public void agregarParada(Parada parada) {
        mapa.putIfAbsent(parada,new ArrayList<>()); // Se crea la parada y la inicialización de un listado de rutas
        // dependientes a ella.
    }
    //Validar duplicados

    /**
     *  Nombre: agregarRuta
     *  Paramétros: String, Parada, Parada, double, double, double.
     *  Funcionamiento: Creación de nueva ruta y agregación al HashMap.
     *  Retorno: no retorna
     * */
    public void agregarRuta(String nombre, Parada inicio, Parada destino,  double distancia, double tiempo, double costo) {
        mapa.putIfAbsent(inicio,new ArrayList<>());
        mapa.get(inicio).add( new Ruta(nombre, inicio, destino, distancia, tiempo, costo));
        destino.agregarRuta(new Ruta(nombre, inicio, destino, distancia, tiempo, costo));
    }

    /**
     *  Nombre: eliminarParada
     *  Paramétros: Parada.
     *  Funcionamiento: se elimina del grafo la parada seleccionada y las rutas que salen de esta
     *  Retorno: no retorna
     * */
    public void eliminarParada(Parada parada) {
        for(int i = mapa.get(parada).size() - 1 ; i >= 0; i++){ // Se recorre el listado de rutas dependientes de la parada
            eliminarRuta(parada.getMisRutas().get(i)); // Se eliminan las rutas mediante el llamado de la función.
        }
        mapa.remove(parada); // Se elimina la parada del HashMap.

    }

    /**
     *  Nombre: eliminarRuta
     *  Paramétros: Ruta.
     *  Funcionamiento: se elimina del grafo la ruta seleccionada
     *  Retorno: no retorna
     * */
    public void eliminarRuta(Ruta ruta) {
        //Preguntar acerca de quedarse una parada sola
        mapa.get(ruta.getInicio()).remove(ruta); // Se remueve la ruta de la parada inicial.
        ruta.getDestino().eliminarRuta(ruta); // Se remueve la ruta de la parada final.
    }

    /**
     *  Nombre: modificarRuta
     *  Paramétros: Ruta, Parada, String, Parada, Parada, Double, Double, Double.
     *  Funcionamiento: se modifican los parametros diferentes a null de la ruta proporcionada,
     *  si no se desea modificar ciertos datos, entonces se coloca null para obviarlos.
     *  Retorno: no retorna
    */
    public void modificarRuta(Ruta ruta, Parada parada, String nuevoNombre, Parada nuevoInicio, Parada nuevoDestino,  Double nuevaDistancia, Double nuevoTiempo, Double nuevoCosto) {

        if(nuevoNombre != null){
            ruta.setNombre(nuevoNombre);
        }
        if(nuevoInicio != null){
            mapa.get(ruta.getInicio()).remove(ruta); // Elimino la ruta actual del listado de rutas de la parada a cambiar
            ruta.setInicio(nuevoInicio); // Se realiza el cambio de la parada antigua por la nueva
            mapa.get(ruta.getInicio()).add(ruta); // Se agrega la ruta actaul al listado de rutas de la nueva parada
        }
        if(nuevoDestino != null){
            ruta.getDestino().eliminarRuta(ruta); // Se accede al listado de rutas que apuntan a la parada antigua y se elimina la ruta actual
            ruta.setDestino(nuevoDestino); // Se realiza el cambio de la parada antigua por la nueva
            nuevoDestino.agregarRuta(ruta); // Se accede al listado de rutas que apuntan a la parada nueva y se agrega la ruta actual
        }
        if(nuevaDistancia !=  null){
            ruta.setDistancia((double)nuevaDistancia);
        }
        if(nuevoCosto !=  null){
            ruta.setCosto((double)nuevoCosto);
        }
        if(nuevoTiempo !=  null){
            ruta.setTiempo((double)nuevoTiempo);
        }

    }
    /**
     *  Nombre: modificarParada
     *  Paramétros: Parada, String.
     *  Funcionamiento: se modifican los parametros de la parada proporcionada.
     *  Retorno: no retorna
     */

    public void modificarParada(Parada parada, String nombre){
        if(nombre != null && parada != null){
            parada.setNombre(nombre);
        }

    }

}
