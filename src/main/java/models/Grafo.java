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

        // Elimina rutas de entrada
        for (int i = parada.getRutasDeEntrada().size() - 1; i >= 0; i--) {
            eliminarRuta(parada.getRutasDeEntrada().get(i));
        }

        // Elimina rutas de salida
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

        if (nuevoNombre != null) ruta.setNombre(nuevoNombre);

        if (nuevoInicio != null && nuevoInicio != ruta.getInicio()) {
            mapa.putIfAbsent(nuevoInicio, new ArrayList<>());
            mapa.get(ruta.getInicio()).remove(ruta);
            ruta.setInicio(nuevoInicio);
            mapa.get(ruta.getInicio()).add(ruta);
        }

        if (nuevoDestino != null && nuevoDestino != ruta.getDestino()) {
            mapa.putIfAbsent(nuevoDestino, new ArrayList<>());
            ruta.getDestino().eliminarRutaDeEntrada(ruta);
            ruta.setDestino(nuevoDestino);
            nuevoDestino.agregarRutaDeEntrada(ruta);
        }

        if (nuevaDistancia != null) ruta.setDistancia(nuevaDistancia);
        if (nuevoTiempo != null) ruta.setTiempo(nuevoTiempo);
        if (nuevoCosto != null) ruta.setCosto(nuevoCosto);
    }

    public void modificarParada(Parada parada, String nombre) {
        if (parada != null && nombre != null) parada.setNombre(nombre);
        else throw new IllegalArgumentException("Parada/nombre no puede ser null");
    }

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
                    case 1 -> {
                        r.setEstado(false);
                        r.setEvento("Accidente");
                        System.out.println("Ha ocurrido un accidente en " + r.getNombre() +
                                " (" + r.getInicio().getNombre() + " → " + r.getDestino().getNombre() + ")");
                    }
                    case 2 -> {
                        r.setEstado(true);
                        r.setEvento("Retraso");
                        r.setTiempo(r.getTiempo() * 1.5);
                        System.out.println("Hay retraso en " + r.getNombre());
                    }
                    case 3 -> {
                        r.setEstado(true);
                        r.setEvento("Lluvia");
                        r.setTiempo(r.getTiempo() * 1.2);
                        System.out.println("Lluvia en " + r.getNombre());
                    }
                    case 4 -> {
                        r.setEstado(true);
                        r.setEvento("Normal");
                    }
                }
            }
        }
    }

    public void mostrarMapa() {
        for (Parada parada : mapa.keySet()) {
            System.out.println(parada + " --> " + mapa.get(parada));
        }
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
        // Cargar paradas primero
        ParadaDAO paradaDAO = ParadaDAO.getInstance();
        HashMap<Long, Parada> paradasDB = paradaDAO.obtenerParadas();
        for (Parada p : paradasDB.values()) {
            agregarParada(p);
        }

        // Cargar rutas
        RutaDAO rutaDAO = RutaDAO.getInstance();
        HashMap<Long, Ruta> rutasDB = rutaDAO.obtenerRutas(new HashMap<>(), paradasDB); // pasamos HashMap vacío y las paradas

        for (Ruta r : rutasDB.values()) {
            Parada inicio = r.getInicio();
            Parada destino = r.getDestino();

            if (inicio != null && destino != null) {
                // Insertamos en mapa y en rutas de entrada de la parada destino
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
