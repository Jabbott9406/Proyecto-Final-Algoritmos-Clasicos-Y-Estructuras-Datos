package models;

import java.util.List;

public class RutaMasCorta {

    private List<Ruta> rutas;
    private double totalTiempo;
    private double totalCosto;
    private double totalDistancia;
    private double totalPeso;
    private String filtro;
    private String evento;
    private int transbordos;


    public RutaMasCorta(List<Ruta> rutas,
                        double totalTiempo,
                        double totalCosto,
                        double totalDistancia,
                        double totalPeso,
                        String filtro,
                        String evento) {
        this.rutas = rutas;
        this.totalTiempo = totalTiempo;
        this.totalCosto = totalCosto;
        this.totalDistancia = totalDistancia;
        this.totalPeso = totalPeso;
        this.filtro = filtro;
        this.evento = evento;
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

    public double getTotalPeso() {
        return totalPeso;
    }

    public String getFiltro() {
        return filtro;
    }

    public String getEvento() {
        return evento;
    }

    public int getTransbordos() {
        return transbordos;
    }

    public void setTransbordos(int transbordos) {
        this.transbordos = transbordos;
    }

    @Override
    public String toString() {
        return "Filtro = " + filtro + "\n"
                + "Evento = " + evento + "\n"
                + "TotalTiempo = " + totalTiempo + "\n"
                + "TotalCosto = " + totalCosto + "\n"
                + "TotalDistancia = " + totalDistancia + "\n"
                + "TotalPeso = " + totalPeso + "\n"
                + "Rutas : " + rutas.toString() + "\n";
    }
}
