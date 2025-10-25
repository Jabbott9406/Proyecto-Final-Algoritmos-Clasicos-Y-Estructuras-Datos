import java.util.ArrayList;
import java.util.List;

public class Parada {

    private String nombre;
    private List<Ruta> misRutas;

    public Parada(String nombre) {
        this.nombre = nombre;
        this.misRutas = new ArrayList<>();
    }

    public String getNombre() { return nombre;}
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Ruta> getMisRutas() {
        return misRutas;
    }

    public void agregarRuta(Ruta ruta) {
        this.misRutas.add(ruta);
    }
}
