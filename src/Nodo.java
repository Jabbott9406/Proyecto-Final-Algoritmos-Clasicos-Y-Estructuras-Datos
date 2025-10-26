public class Nodo {

    private Parada parada;
    public double peso;

    public Nodo(Parada parada, double peso) {
        this.parada = parada;
        this.peso = peso;
    }
    public Parada getParada() {
        return parada;
    }

    public void getParada(Parada parada) {
        this.parada = parada;
    }
}
