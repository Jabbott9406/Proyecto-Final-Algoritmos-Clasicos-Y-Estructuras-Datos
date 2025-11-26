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

    private Grafo() {
        mapa = new HashMap<>();
        paradas = FXCollections.observableArrayList();
    }

    public static Grafo getInstance() {
        return INSTANCE;
    }

    public ObservableList<Parada> getParadas() {
        return paradas;
    }

    public Map<Parada, List<Ruta>> getMapa() {
        return mapa;
    }

    public void agregarParada(Parada parada) {
        if (parada == null) throw new IllegalArgumentException("Parada no puede ser null");
        if (!mapa.containsKey(parada)) {
            mapa.put(parada, new ArrayList<>());
            paradas.add(parada);
        }
    }

    public Ruta agregarRuta(String nombre, Parada inicio, Parada destino, double distancia, double tiempo, double costo) {
        if (inicio == null || destino == null) throw new IllegalArgumentException("inicio/destino no puede ser null");
        mapa.putIfAbsent(inicio, new ArrayList<>());
        mapa.putIfAbsent(destino, new ArrayList<>());

        Ruta nuevaRuta = new Ruta(nombre, inicio, destino, distancia, tiempo, costo);
        mapa.get(inicio).add(nuevaRuta);
        destino.agregarRutaDeEntrada(nuevaRuta);

        return nuevaRuta;
    }

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

    public void eliminarRuta(Ruta ruta) {
        if (ruta == null) throw new IllegalArgumentException("Ruta no puede ser null");
        mapa.get(ruta.getInicio()).remove(ruta);
        ruta.getDestino().eliminarRutaDeEntrada(ruta);
    }

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
            default -> throw new IllegalArgumentException("Filtro desconocido: " + filtro);
        }
    }

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

        return new RutaMasCorta(caminoRutas, totalTiempo, totalCosto, totalDistancia, totalPeso, filtro, evento
        );
    }

    public List<Ruta> getRutasDeSalida(Parada parada) {
        return mapa.get(parada);
    }

    public List<Parada> getParadasList() {
        return new ArrayList<>(mapa.keySet());
    }

    public List<Ruta> getRutas() {
        List<Ruta> todasRutas = new ArrayList<>();
        for (List<Ruta> rutas : mapa.values()) {
            todasRutas.addAll(rutas);
        }
        return todasRutas;
    }

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
}
