package models;

public class Ruta {

    private long id;
    private String nombre;
    private Parada inicio;
    private Parada destino;
    private double distancia;
    private double tiempoBase;
    private double tiempo;
    private double costoBase;
    private double costo;
    private boolean estado;
    private String evento;
    private String transbordo;


    public Ruta(String nombre, Parada inicio, Parada destino, double distancia, double tiempo, double costo) {
        this.nombre = nombre;
        this.inicio = inicio;
        this.destino = destino;
        this.distancia = distancia;
        this.tiempoBase = tiempo;
        this.costoBase = costo;
        this.tiempo = tiempo;
        this.costo = costo;

        this.estado = true;
        this.evento = "Normal";
        this.transbordo = "No aplica";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Parada getInicio() { return inicio; }
    public void setInicio(Parada inicio) { this.inicio = inicio; }

    public Parada getDestino() { return destino; }
    public void setDestino(Parada destino) { this.destino = destino; }

    public double getDistancia() { return distancia; }
    public void setDistancia(double distancia) { this.distancia = distancia; }

    public double getTiempo() { return tiempo; }
    public void setTiempo(double tiempo) { this.tiempo = tiempo; }

    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }

    public String getTransbordo() { return transbordo; }

    public void setTransbordo(String transbordo) { this.transbordo = transbordo; }

    public double getTiempoBase() {
        return tiempoBase;
    }

    public void setTiempoBase(double t) { this.tiempoBase = t; }

    public double getCostoBase() {
        return costoBase;
    }

    public void setCostoBase(double c) { this.costoBase = c; }


    /**
     * Restablece valores al valor base original.
     * Evita acumulaciones infinitas cuando se simulan eventos repetidos.
     */


    public void resetValores() {
        this.tiempo = this.tiempoBase;
        this.costo = this.costoBase;
        this.estado = true;
        this.evento = "Normal";
        this.transbordo = "No aplica";
    }

    public Double getPesoByFiltro(String filtro) {
        return switch (filtro) {
            case "distancia" -> distancia;
            case "tiempo" -> tiempo;
            case "costo" -> costo;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return nombre + " --> " + destino;
    }
}
