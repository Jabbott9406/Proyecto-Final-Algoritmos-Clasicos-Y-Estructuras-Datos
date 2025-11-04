package models;

import java.util.List;

public class RutaMasCorta {

    private List<Ruta> rutas;
    private double totalTiempo;
    private double totalCosto;
    private double totalDistancia;
    private double totalPeso;
    private String filtro;

    public RutaMasCorta(List<Ruta> rutas, double totalTiempo, double totalCosto, double totalDistancia, double totalPeso, String filtro) {
        this.rutas = rutas;
        this.totalTiempo = totalTiempo;
        this.totalCosto = totalCosto;
        this.totalDistancia = totalDistancia;
        this.totalPeso = totalPeso;
        this.filtro = filtro;
    }

    public List<Ruta> getRutas() {
        return rutas;
    }

    public double getTotalTiempo() {
        return totalTiempo;
    }

    public double getTotalCosto() {
        return totalCosto;
    }

    public double getTotalDistancia() {
        return totalDistancia;
    }

    public String toString(){
        return "Filtro = " + filtro + "\n"
                +"TotalTiempo = " + totalTiempo + "\n"
                +"TotalCosto = " + totalCosto + "\n"
                +"TotalDistancia = " + totalDistancia + "\n"
                +"TotalPeso = " + totalPeso + "\n"
                +"Rutas : " + rutas.toString() + "\n";
    }
}
