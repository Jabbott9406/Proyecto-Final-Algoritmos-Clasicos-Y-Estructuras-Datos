public class Ruta {

    private String nombre;
    private Parada inicio;
    private Parada destino;
    private double distancia;
    private double tiempo;
    private double tiempoBase;
    private double costo;
    private boolean estado; // True: Esta habilitada. False: ruta no disponible por accidente o x situación.
    private String evento;
    private String transbordo;

    public Ruta(String nombre, Parada inicio, Parada destino, double distancia, double tiempo, double costo, String evento, String transbordo) {
        this.nombre = nombre;
        this.inicio = inicio;
        this.destino = destino;
        this.distancia = distancia;
        this.tiempo = tiempo;
        this.tiempoBase = tiempo; // tiempo original
        this.costo = costo;
        this.estado = true;        // por defecto habilitada
        this.evento = "Normal";    // por defecto normal
        this.transbordo = transbordo;
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

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public String getTransbordo() {
        return transbordo;
    }

    public void setTransbordo(String transbordo) {
        this.transbordo = transbordo;
    }

    public void resetTiempo() {
        this.tiempo = this.tiempoBase;
    }

}

/**
 * Nombre: resetTiempo
 * Parámetros: ninguno
 * Funcionamiento: restablece el tiempo de la ruta al tiempo base original.
 * Esto se utiliza antes de aplicar cualquier evento que pueda modificar la duración
 * de la ruta, como retrasos, lluvia u otros incidentes, asegurando que el tiempo
 * no se acumule al simular múltiples eventos.
 * Retorno: no retorna ningún valor.
 */

