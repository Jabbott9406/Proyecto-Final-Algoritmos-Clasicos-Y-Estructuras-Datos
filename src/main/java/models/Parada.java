package models;

import java.util.ArrayList;
import java.util.List;

public class Parada {

    private String nombre;
    private List<Ruta> rutasDeEntrada;

    public Parada(String nombre) {
        if(nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre no puede ser null/estar vacio");
        this.nombre = nombre;
        this.rutasDeEntrada = new ArrayList<>();
    }

    public String getNombre() { return nombre;}
    public void setNombre(String nombre) {
        if(nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre no puede ser null/estar vacio");
        this.nombre = nombre;
    }

    public List<Ruta> getRutasDeEntrada() {
        return rutasDeEntrada;
    }

    public void agregarRutaDeEntrada(Ruta ruta) {
       if(ruta != null){
           this.rutasDeEntrada.add(ruta);
       }
    }

    public void eliminarRutaDeEntrada(Ruta ruta) {
        this.rutasDeEntrada.remove(ruta);
    }

    public String toString() {
        return nombre;
    }
}
