package models;

import java.util.ArrayList;
import java.util.List;

public class Parada {

    private long id;
    private String nombre;
    private String tipo;
    private List<Ruta> rutasDeEntrada;

    public Parada(String nombre, String tipo) {
        if(nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre no puede ser null/estar vacio");
        this.nombre = nombre;
        this.rutasDeEntrada = new ArrayList<>();
        this.tipo = tipo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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
