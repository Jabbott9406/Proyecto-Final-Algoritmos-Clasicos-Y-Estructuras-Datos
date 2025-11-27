package models;

import java.util.*;

public class Dijkstra {

    public static final double constante = Double.POSITIVE_INFINITY; // Constante para comparación, contiene un valor númerico muy grande

    public RutaMasCorta rutaMasCorta(Grafo grafo, Parada inicio, Parada destino, String filtro) {
        if(grafo == null || inicio == null || destino == null || (filtro == null || filtro.isBlank())) {
            throw new IllegalArgumentException("grafo/inicio/destino/filtro no pueden ser null");
        }

        // Reiniciar rutas para evitar acumulación de eventos
        for (Parada p : grafo.getParadasList()) {
            for (Ruta r : grafo.getRutasDeSalida(p)) {
                r.resetValores();
            }
        }

        /// Se crean cuatro contenedores de datos
        Map<Parada, Double> distancia = new HashMap<>(); // Se encarga de guardar la distancia que se necesita
        // para llegar a x parada.

        Map<Parada, Ruta> paradasPrev = new HashMap<>(); // Se encarga de guardar la ruta por la que se llegó a x parada,
        // este mapa será útil para la parte de reconstrucción del camino o recorrido.

        List<Parada> visitadas = new ArrayList<>(); // Marca las paradas ya visitadas

        PriorityQueue<Nodo> paradasQueue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.peso)); // Mediante la PriorityQueue se puede manejar de forma
        //eficiente la organización en base al filtro suministrado

        paradasQueue.add(new Nodo(inicio,0)); // Se agrega la primera parada con peso 0, pues no hemos recorrido nada aún
        distancia.put(inicio,0.0);

        while(!paradasQueue.isEmpty()) {
            Nodo nodo = paradasQueue.poll(); // Obtenemos el nodo que se encuentre a la cabeza de la queue
            Parada parada = nodo.getParada(); // Obtenemos la parada

            if(visitadas.contains(parada)) { // Validamos de que la parada se encuentre en la lista de visitadas
                continue;
            }
            visitadas.add(parada); // En caso de que no se encuentre, pues se agrega

            if(parada == destino){
                break;
            }

            for(Ruta r : grafo.getRutasDeSalida(parada)) { // Iteramos en base a las rutas que salen de la parada seleccionada

                if(!r.isEstado()) continue; // Ignorar rutas cerradas

                Double peso = r.getPesoByFiltro(filtro); // Se obtiene el peso de la ruta
                if (peso == null) {
                    throw new IllegalArgumentException("Filtro inválido: " + filtro);
                }

                Parada direccion = r.getDestino(); // Parada a la que apunta la ruta
                Double sumaPeso = nodo.peso + peso; // Se calcula el peso necesario para llegar a esa parada

                if(sumaPeso < distancia.getOrDefault(direccion, constante)) { // Se valida que este peso sea el menor que se guardado para esa parada
                    distancia.put(direccion, sumaPeso); // Se agrega la parada y su distancia al mapa
                    paradasQueue.add(new Nodo(direccion,sumaPeso)); // De igual forma se guarda en la queue
                    paradasPrev.put(direccion,r); // Se guarda la ruta por donde se llegó a esa parada
                }
            }
        }

        Double finalDist = distancia.get(destino);
        if (finalDist == null || finalDist.isInfinite()) {
            return null; // No existe camino
        }

        List<Ruta> rutaCorta = reconstruirCamino(paradasPrev,inicio,destino); // Se llama a la función para guardar la lista de rutas a seguir para llegar al destino

        if(rutaCorta == null) return null;

        double totalTiempo = 0;
        double totalCosto = 0;
        double totalDistancia = 0;
        double totalPeso = 0;
        String evento = null;

        for(Ruta r: rutaCorta) {
            totalTiempo += r.getTiempo();
            totalCosto += r.getCosto();
            totalDistancia += r.getDistancia();
            totalPeso += r.getPesoByFiltro(filtro);

            if(evento == null && r.getEvento() != null && !r.getEvento().isBlank()) {
                evento = r.getEvento();
            }
        }

        // Para distancia, el evento se puede ignorar
        if(filtro.equalsIgnoreCase("distancia")) evento = null;

        RutaMasCorta rm = new RutaMasCorta(rutaCorta,totalTiempo,totalCosto,totalDistancia,totalPeso,filtro,evento);
        // Nuevo: contar transbordos también en distancia/tiempo/costo
        rm.setTransbordos(contarTransbordos(rutaCorta));
        return rm;
    }

    public List<Ruta> reconstruirCamino(Map<Parada, Ruta> paradasPrev, Parada inicio, Parada destino) {
        List<Ruta> camino = new LinkedList<>();
        Parada actualParada = destino;

        while (actualParada != null && actualParada != inicio) {
            Ruta ruta = paradasPrev.get(actualParada);
            if (ruta == null) {
                break; // si no hay una ruta anterior pues ya no hay más camino
            }
            camino.addFirst(ruta);
            actualParada = ruta.getInicio();
        }

        // Comprobar si la primera parada es la misma que la parada de partida
        if (!camino.isEmpty() && camino.getFirst().getInicio() != inicio) {
            return null; // si no lo es, pues devuelve null.
        }
        return camino;
    }

    // Método para contar transbordos
// Objetivo: calcular cuántas veces cambiamos de “línea” (tipo de transporte) a lo largo del camino.
// Idea: para cada tramo, sacamos un identificador de línea. Preferimos el tipo de la parada de inicio;
// si no hay, usamos el tipo del destino; si tampoco hay, caemos al nombre de la ruta.
// Luego comparamos línea actual vs. línea anterior: si cambió, sumamos un transbordo.
    private int contarTransbordos(List<Ruta> path) {
        // Si el camino está vacío, obviamente no hay transbordos.
        if (path == null || path.isEmpty()) return 0;

        // Esta lambda nos da la “línea” de un tramo. Normalizamos a minúsculas y sin acentos
        // para evitar que “Autobús” vs “Autobus” cuenten como diferentes.
        java.util.function.Function<Ruta,String> lineaDe = r -> {
            // Preferimos el tipo de la parada donde arrancamos el tramo.
            Parada p = r.getInicio();
            String tipo = (p != null && p.getTipo() != null && !p.getTipo().isBlank()) ? p.getTipo()
                    // Si no hay tipo en inicio, probamos el del destino.
                    : (r.getDestino()!=null && r.getDestino().getTipo()!=null && !r.getDestino().getTipo().isBlank()
                    ? r.getDestino().getTipo()
                    // Último recurso: el nombre de la ruta. Esto evita devolver null/empty.
                    : r.getNombre());

            // Normalizamos el texto para que “Autobús”, “autobus” y “AUTOBUS” sean la misma línea.
            String s = java.text.Normalizer.normalize(tipo, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                    .toLowerCase(java.util.Locale.ROOT)
                    .trim();

            // Si queda vacío por cualquier cosa rara, usamos un guion para representar “sin línea”.
            return s.isBlank() ? "-" : s;
        };

        // Recorremos el camino comparando línea anterior vs. actual.
        String prev = null;
        int trans = 0;
        for (Ruta r : path) {
            String lin = lineaDe.apply(r);
            // Si ya teníamos una línea anterior y esta es diferente, ahí contamos un transbordo.
            if (prev != null && !prev.equals(lin)) trans++;
            prev = lin;
        }

        // Resultado final: número de cambios de línea a lo largo del camino.
        return trans;
    }

    // Clase interna Nodo
// Objetivo: representar una parada con su peso acumulado para la cola de prioridad.
// Esta clase nos ayuda a manejar Dijkstra de forma cómoda, guardando aquí mismo
// cuál parada estamos procesando y cuánto cuesta llegar hasta ella.
    private static class Nodo {
        // La parada asociada a este nodo.
        private final Parada parada;
        // El peso acumulado (según filtro: distancia/tiempo/costo) hasta esta parada.
        private final double peso;

        // Constructor sencillo: recibe la parada y el valor de peso acumulado.
        public Nodo(Parada p, double peso){ this.parada = p; this.peso = peso; }

        // Getter para la parada. Lo usamos cuando sacamos el nodo de la cola y necesitamos
        // saber sobre cuál parada estamos iterando sus rutas de salida.
        public Parada getParada(){ return parada; }
    }

}