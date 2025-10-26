public class Ruta {

    private String nombre;
    private Parada inicio;
    private Parada destino;
    private double distancia;
    private double tiempo;
    private double costo;
    private boolean estado; // True: Esta habilitada. False: ruta no disponible por accidente o x situación.

    public Ruta(String nombre, Parada inicio, Parada destino, double distancia, double tiempo, double costo) {
        if(nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre no puede ser null/estar vacio");
        if(inicio == null || destino == null) throw  new IllegalArgumentException("Inicio/destino no pueden ser null");
        if(distancia < 0 || tiempo < 0 || costo < 0) throw new IllegalArgumentException("Distancia/tiempo/costo no pueden ser negativos");
        this.nombre = nombre;
        this.inicio = inicio;
        this.destino = destino;
        this.distancia = distancia;
        this.tiempo = tiempo;
        this.costo = costo;
        this.estado = true;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if(nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre no puede ser null");
        this.nombre = nombre;
    }

    public Parada getInicio() {
        return inicio;
    }

    public void setInicio(Parada inicio) {
        if(inicio == null) throw new IllegalArgumentException("Inicio no puede ser null");
        this.inicio = inicio;
    }

    public Parada getDestino() {
        return destino;
    }

    public void setDestino(Parada destino) {
        if(destino == null) throw new IllegalArgumentException("Destino no puede ser null");
        this.destino = destino;
    }

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        if(distancia < 0) throw new IllegalArgumentException("Distancia no puede ser negativo");
        this.distancia = distancia;
    }

    public double getTiempo() {
        return tiempo;
    }

    public void setTiempo(double tiempo) {
        if(tiempo < 0)throw new IllegalArgumentException("Tiempo no puede ser negativo");
        this.tiempo = tiempo;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        if(costo < 0) throw new IllegalArgumentException("Costo no puede ser negativo");
        this.costo = costo;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public Double getPesoByFiltro(String filtro) {
        switch (filtro) {
            case "distancia":
                return distancia;
            case "tiempo":
                return tiempo;
            case "costo":
                return costo;
        }

        return null;
    }

    @Override
    public String toString() {
        return nombre + " --> " + destino;
    }
}
