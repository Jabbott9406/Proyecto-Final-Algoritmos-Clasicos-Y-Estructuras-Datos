//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        Parada Colegio = new Parada("Colegio");
        Parada Casa = new Parada("Casa");
        Parada Utesa = new Parada("Utesa");
        Parada CasaFrancisco = new Parada("CasaFrancisco");


        Grafo mapa = new Grafo();
        mapa.agregarParada(Colegio);
        mapa.agregarParada(Casa);
        mapa.agregarParada(Utesa);
        mapa.agregarParada(CasaFrancisco);

        Ruta r1 = mapa.agregarRuta("Avenida",Casa,Colegio,2,5,100);
        Ruta r2 = mapa.agregarRuta("Kennedy",Colegio,Utesa,1,2,50);
        Ruta r3 = mapa.agregarRuta("Yerba de Guinea", Utesa, CasaFrancisco,1,8,0);
        Ruta r4 = mapa.agregarRuta("Duarte", CasaFrancisco, Casa,5,10,100);
        System.out.println("Antes de eliminar");
        mapa.mostrarMapa();
        System.out.println("\n---------------Despues de eliminar-----------------\n");
        mapa.eliminarParada(Colegio);
//        mapa.eliminarRuta(r1);
//        mapa.eliminarRuta(r2);
        mapa.mostrarMapa();
        System.out.println("Rutas de entrada de casa: " + Casa.getRutasDeEntrada());
        System.out.println("Rutas de entrada de colegio: " + Colegio.getRutasDeEntrada());
        System.out.println("Rutas de entrada de utesa: " + Utesa.getRutasDeEntrada());
        System.out.println("Rutas de entrada de C. Fran: " + CasaFrancisco.getRutasDeEntrada());

        System.out.println("\n---------------Despues de modificar-----------------\n");
        mapa.modificarRuta(r4,null,null,Colegio,null,null,null);
        mapa.mostrarMapa();
        System.out.println("Rutas de entrada de casa: " + Casa.getRutasDeEntrada());
        System.out.println("Rutas de entrada de colegio: " + Colegio.getRutasDeEntrada());
        System.out.println("Rutas de entrada de utesa: " + Utesa.getRutasDeEntrada());
        System.out.println("Rutas de entrada de C. Fran: " + CasaFrancisco.getRutasDeEntrada());

    }
}