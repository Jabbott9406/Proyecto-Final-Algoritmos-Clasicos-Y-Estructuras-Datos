package models;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class Grafo {

    private Map<Parada, List<Ruta>> mapa;
    private ObservableList<Parada> paradas;

    private static final Grafo INSTANCE = new Grafo();

    /**
     * Grafo
     * Objetivo: preparar la estructura base del grafo en memoria.
     *           Aquí iniciamos el mapa (parada -> rutas que salen) y la lista observable de paradas.
     * Retorno: ninguno.
     */
    private Grafo() {
        mapa = new HashMap<>();
        paradas = FXCollections.observableArrayList();
    }

    /**
     * getInstance
     * Objetivo: patrón singleton para usar un único grafo en toda la app.
     *           Pedimos la instancia y trabajamos siempre con la misma.
     * Retorno: Grafo (instancia única).
     */
    public static Grafo getInstance() {
        return INSTANCE;
    }

    /**
     * getParadas
     * Objetivo: obtener la lista observable de paradas para que la UI se actualice cuando cambie.
     * Retorno: ObservableList<Parada>.
     */
    public ObservableList<Parada> getParadas() {
        return paradas;
    }

    /**
     * getMapa
     * Objetivo: exponer el mapa interno (parada -> rutas que salen) para operaciones del modelo/algoritmos.
     * Retorno: Map<Parada, List<Ruta>>.
     */
    public Map<Parada, List<Ruta>> getMapa() {
        return mapa;
    }

    /**
     * agregarParada
     * Objetivo: añadir una parada al grafo si no existe todavía y registrarla en la lista observable.
     *           Esto permite que el resto de la app (tablas, combos) se entere al toque.
     * Retorno: ninguno.
     */
    public void agregarParada(Parada parada) {
        if (parada == null) throw new IllegalArgumentException("Parada no puede ser null");
        if (!mapa.containsKey(parada)) {
            mapa.put(parada, new ArrayList<>());
            paradas.add(parada);
        }
    }

    /**
     * agregarRuta
     * Objetivo: crear una ruta entre dos paradas, registrarla en el mapa y marcarla
     *           como ruta de entrada del destino. Aquí validamos y preparamos claves si faltaban.
     * Retorno: Ruta creada.
     */
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
     * eliminarParada
     * Objetivo: borrar una parada del grafo limpiando primero todas las rutas
     *           que entran y salen de ella para no dejar referencias colgando.
     *           Luego quitamos de la lista observable y avisamos a la DB.
     * Retorno: ninguno.
     */
    public void eliminarParada(Parada parada) {
        if (parada == null) throw new IllegalArgumentException("Parada no puede ser null");

        for (int i = parada.getRutasDeEntrada().size() - 1; i >= 0; i--) {
            eliminarRuta(parada.getRutasDeEntrada().get(i));
        }

        if (mapa.get(parada) != null) {
            for (int j = mapa.get(parada).size() - 1; j >= 0; j--) {
                eliminarRuta(mapa.get(parada).get(j));
            }
        }

        mapa.remove(parada);
        paradas.remove(parada);
        ParadaDAO.getInstance().eliminarParada(parada.getId());
    }

    /**
     * eliminarRuta
     * Objetivo: remover una ruta del mapa y también del listado de rutas de entrada del destino.
     *           Así mantenemos la consistencia en ambas direcciones.
     * Retorno: ninguno.
     */
    public void eliminarRuta(Ruta ruta) {
        if (ruta == null) throw new IllegalArgumentException("Ruta no puede ser null");
        mapa.get(ruta.getInicio()).remove(ruta);
        ruta.getDestino().eliminarRutaDeEntrada(ruta);
    }

    /**
     * modificarRuta
     * Objetivo: actualizar los atributos de una ruta existente. Si cambia inicio/destino,
     *           movemos la ruta entre listas del mapa y actualizamos las entradas del destino.
     *           También reseteamos estado/evento a “Normal” para partir de valores consistentes.
     * Retorno: ninguno.
     */
    public void modificarRuta(Ruta ruta, String nuevoNombre, Parada nuevoInicio, Parada nuevoDestino, Double nuevaDistancia, Double nuevoTiempo, Double nuevoCosto) {

        if (ruta == null) throw new IllegalArgumentException("Ruta no puede ser null");

        // Cambiar nombre
        if (nuevoNombre != null) ruta.setNombre(nuevoNombre);

        // Cambiar inicio
        if (nuevoInicio != null && nuevoInicio != ruta.getInicio()) {
            mapa.putIfAbsent(nuevoInicio, new ArrayList<>());
            mapa.get(ruta.getInicio()).remove(ruta);
            ruta.setInicio(nuevoInicio);
            mapa.get(nuevoInicio).add(ruta);
        }

        // Cambiar destino
        if (nuevoDestino != null && nuevoDestino != ruta.getDestino()) {
            mapa.putIfAbsent(nuevoDestino, new ArrayList<>());
            ruta.getDestino().eliminarRutaDeEntrada(ruta);
            ruta.setDestino(nuevoDestino);
            nuevoDestino.agregarRutaDeEntrada(ruta);
        }

        if (nuevaDistancia != null) {
            ruta.setDistancia(nuevaDistancia);
        }

        if (nuevoTiempo != null) {
            ruta.setTiempo(nuevoTiempo);
            ruta.setTiempoBase(nuevoTiempo);
        }

        if (nuevoCosto != null) {
            ruta.setCosto(nuevoCosto);
            ruta.setCostoBase(nuevoCosto);
        }

        ruta.setEvento("Normal");
        ruta.setEstado(true);
    }

    /**
     * obtenerMejorRuta
     * Objetivo: decidir qué algoritmo usar según el filtro y el estado de las rutas,
     *           simular eventos antes de calcular y devolver el resumen de la mejor ruta.
     * Retorno: RutaMasCorta con el camino y totales.
     */
    public RutaMasCorta obtenerMejorRuta(Parada inicio, Parada destino, String filtro) {
        if (inicio == null || destino == null || filtro == null || filtro.isBlank())
            throw new IllegalArgumentException("inicio/destino/filtro no pueden ser null");

        // Simular eventos antes de calcular rutas
        simularEventosRetorno();

        switch (filtro.toLowerCase()) {
            case "distancia" -> {
                return rutaMasCortaFloyd(inicio, destino, "distancia");
            }
            case "tiempo", "costo" -> {
                // Usar Dijkstra si todas las rutas son normales, Bellman-Ford si hay rutas cerradas
                boolean hayRutasCerradas = getRutas().stream().anyMatch(r -> !r.isEstado());
                if (!hayRutasCerradas) {
                    return new Dijkstra().rutaMasCorta(this, inicio, destino, filtro);
                } else {
                    return new BellmanFord().calcular(this, inicio, destino, filtro);
                }
            }
            case "transbordos" -> {
                return new BellmanFord().calcular(this, inicio, destino, "transbordos");
            }
            default -> throw new IllegalArgumentException("Filtro desconocido: " + filtro);
        }
    }

    /**
     * simularEventosRetorno
     * Objetivo: aplicar una simulación ligera de eventos a cada ruta (accidente, lluvia, retraso, etc.)
     *           para que los algoritmos trabajen con valores dinámicos. Luego se resetean en las partes necesarias.
     * Retorno: boolean indicando que la simulación corrió.
     */
    private boolean simularEventosRetorno() {
        Random rand = new Random();

        for (Parada p : mapa.keySet()) {
            for (Ruta r : mapa.get(p)) {

                r.resetValores(); // solo al inicio de la simulación

                int prob = rand.nextInt(100) + 1;

                if (prob <= 5) { // ACCIDENTE GRAVE – RUTA CERRADA
                    r.setEstado(false);
                    r.setEvento("Accidente grave (Ruta cerrada)");
                }
                else if (prob <= 15) { // ACCIDENTE LEVE
                    r.setEstado(true);
                    r.setEvento("Accidente leve (Retraso severo)");
                    r.setTiempo(r.getTiempoBase() * 2.0);
                    r.setCosto(r.getCostoBase() * 1.3);
                }
                else if (prob <= 30) { // RETRASO
                    r.setEstado(true);
                    r.setEvento("Retraso");
                    r.setTiempo(r.getTiempoBase() * 1.5);
                    r.setCosto(r.getCostoBase() * 1.2);
                }
                else if (prob <= 40) { // LLUVIA
                    r.setEstado(true);
                    r.setEvento("Lluvia");
                    r.setTiempo(r.getTiempoBase() * 1.2);
                    r.setCosto(r.getCostoBase() * 1.1);
                }
                else { // NORMAL
                    r.setEstado(true);
                    r.setEvento("Normal");
                    r.setTiempo(r.getTiempoBase());
                    r.setCosto(r.getCostoBase());
                }
            }
        }
        return true;
    }

    /**
     * rutaMasCortaFloyd
     * Objetivo: usar Floyd-Warshall para construir el camino óptimo según el filtro,
     *           recalcular totales y devolver el resumen en RutaMasCorta.
     *           También contamos transbordos con la misma lógica que el resto de la app.
     * Retorno: RutaMasCorta o null si no hay camino.
     */
    private RutaMasCorta rutaMasCortaFloyd(Parada inicio, Parada destino, String filtro) {
        Map<Parada, Map<Parada, List<Ruta>>> rutas = FloydWarshall.calcular(this, filtro);

        for (Parada p : mapa.keySet()) {
            for (Ruta r : mapa.get(p)) {
                r.resetValores();
            }
        }

        List<Ruta> caminoRutas = rutas
                .getOrDefault(inicio, Collections.emptyMap())
                .getOrDefault(destino, Collections.emptyList());

        if (caminoRutas.isEmpty()) return null;

        double totalTiempo = 0;
        double totalCosto = 0;
        double totalDistancia = 0;
        double totalPeso = 0;

        for (Ruta r : caminoRutas) {
            totalTiempo += r.getTiempo();
            totalCosto += r.getCosto();
            totalDistancia += r.getDistancia();

            switch (filtro.toLowerCase()) {
                case "distancia" -> totalPeso += r.getDistancia();
                case "tiempo"    -> totalPeso += r.getTiempo();
                case "costo"     -> totalPeso += r.getCosto();
            }
        }

        String evento = caminoRutas.get(caminoRutas.size() - 1).getEvento();

        RutaMasCorta rm = new RutaMasCorta(caminoRutas, totalTiempo, totalCosto, totalDistancia, totalPeso, filtro, evento);
        // Nuevo: contar transbordos en FloydWarshall también
        rm.setTransbordos(contarTransbordos(caminoRutas));
        return rm;
    }

    /**
     * getRutasDeSalida
     * Objetivo: devolver las rutas que salen desde una parada específica.
     * Retorno: List<Ruta> (puede ser null si la parada no existe en el mapa).
     */
    public List<Ruta> getRutasDeSalida(Parada parada) {
        return mapa.get(parada);
    }

    /**
     * getParadasList
     * Objetivo: entregar una copia de las paradas registradas en el grafo.
     *           Devolvemos una lista nueva para no exponer la estructura interna directamente.
     * Retorno: List<Parada>.
     */
    public List<Parada> getParadasList() {
        return new ArrayList<>(mapa.keySet());
    }

    /**
     * getRutas
     * Objetivo: recopilar todas las rutas del grafo en una sola lista.
     *           Útil para chequeos globales, como ver si hay rutas cerradas.
     * Retorno: List<Ruta>.
     */
    public List<Ruta> getRutas() {
        List<Ruta> todasRutas = new ArrayList<>();
        for (List<Ruta> rutas : mapa.values()) {
            todasRutas.addAll(rutas);
        }
        return todasRutas;
    }

    /**
     * cargarDesdeDB
     * Objetivo: traer paradas y rutas desde la base de datos y reconstruir el grafo en memoria.
     *           Validamos que las paradas de cada ruta existan; si no, la ignoramos con log.
     * Retorno: ninguno.
     */
    public void cargarDesdeDB() {
        ParadaDAO paradaDAO = ParadaDAO.getInstance();
        HashMap<Long, Parada> paradasDB = paradaDAO.obtenerParadas();
        for (Parada p : paradasDB.values()) {
            agregarParada(p);
        }

        RutaDAO rutaDAO = RutaDAO.getInstance();
        HashMap<Long, Ruta> rutasDB = rutaDAO.obtenerRutas(new HashMap<>(), paradasDB);

        for (Ruta r : rutasDB.values()) {
            Parada inicio = r.getInicio();
            Parada destino = r.getDestino();

            if (inicio != null && destino != null) {
                mapa.putIfAbsent(inicio, new ArrayList<>());
                mapa.get(inicio).add(r);
                destino.agregarRutaDeEntrada(r);
            } else {
                System.out.println("Ruta ignorada por tener parada inexistente: " + r.getNombre() +
                        " | Inicio: " + (inicio != null ? inicio.getNombre() : "null") +
                        " | Destino: " + (destino != null ? destino.getNombre() : "null"));
            }
        }
    }

    // Método para contar transbordos (no altera comentarios existentes)
    private int contarTransbordos(List<Ruta> path) {
        if (path == null || path.isEmpty()) return 0;
        java.util.function.Function<Ruta,String> lineaDe = r -> {
            Parada p = r.getInicio();
            String tipo = (p != null && p.getTipo() != null && !p.getTipo().isBlank()) ? p.getTipo()
                    : (r.getDestino()!=null && r.getDestino().getTipo()!=null && !r.getDestino().getTipo().isBlank()
                    ? r.getDestino().getTipo()
                    : r.getNombre());
            String s = java.text.Normalizer.normalize(tipo, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                    .toLowerCase(java.util.Locale.ROOT)
                    .trim();
            return s.isBlank() ? "-" : s;
        };
        String prev = null;
        int trans = 0;
        for (Ruta r : path) {
            String lin = lineaDe.apply(r);
            if (prev != null && !prev.equals(lin)) trans++;
            prev = lin;
        }
        return trans;
    }
}