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

        return new RutaMasCorta(rutaCorta,totalTiempo,totalCosto,totalDistancia,totalPeso,filtro,evento);
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

}
