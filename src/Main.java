//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        Parada p1 = new Parada("A");
        Parada p2 = new Parada("B");
        Parada p3 = new Parada("C");
        Parada p4 = new Parada("D");
        Parada p5 = new Parada("E");
        Parada p6 = new Parada("F");
        Parada p7 = new Parada("G");
        Parada p8 = new Parada("H");


        Grafo mapa = new Grafo();
        mapa.agregarParada(p1);
        mapa.agregarParada(p2);
        mapa.agregarParada(p3);
        mapa.agregarParada(p4);
        mapa.agregarParada(p5);
        mapa.agregarParada(p6);
        mapa.agregarParada(p7);
        mapa.agregarParada(p8);

        Ruta r1 = mapa.agregarRuta("R1", p1, p2,10,5,100);
        Ruta r2 = mapa.agregarRuta("R2", p1, p3,8,2,50);
        Ruta r3 = mapa.agregarRuta("R3", p3, p5,5,8,0);
        Ruta r4 = mapa.agregarRuta("R4", p2, p4,4,10,100);
        Ruta r5 = mapa.agregarRuta("R5", p3, p2,8,8,0);
        Ruta r6 = mapa.agregarRuta("R6", p5, p4,5,8,0);
        Ruta r7 = mapa.agregarRuta("R7", p7, p3,7,8,0);
        Ruta r8 = mapa.agregarRuta("R8", p7, p5,2,8,0);
        Ruta r9 = mapa.agregarRuta("R9", p5, p6,7,8,0);
        Ruta r10 = mapa.agregarRuta("R10", p4, p6,5,8,0);
        Ruta r11 = mapa.agregarRuta("R11", p6, p7,12,8,0);
        Ruta r12 = mapa.agregarRuta("R12", p7, p8,5,8,0);
        Ruta r13 = mapa.agregarRuta("R13", p6, p8,4,8,0);

    }
}