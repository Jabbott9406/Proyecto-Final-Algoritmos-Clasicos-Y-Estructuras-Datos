public class Ruta {

    private String nombre;
    private Parada inicio;
    private Parada destino;
    private double distancia;
    private double tiempo;
    private double costo;
    private boolean estado; // True: Esta habilitada. False: ruta no disponible por accidente o x situación.

    public Ruta(String nombre, Parada inicio, Parada destino, double distancia, double tiempo, double costo) {
        this.nombre = nombre;
        this.inicio = inicio;
        this.destino = destino;
        this.distancia = distancia;
        this.tiempo = tiempo;
        this.costo = costo;
        this.estado = false;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Parada getInicio() {
        return inicio;
    }

    public void setInicio(Parada inicio) {
        this.inicio = inicio;
    }

    public Parada getDestino() {
        return destino;
    }

    public void setDestino(Parada destino) {
        this.destino = destino;
    }

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public double getTiempo() {
        return tiempo;
    }

    public void setTiempo(double tiempo) {
        this.tiempo = tiempo;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
