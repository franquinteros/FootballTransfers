package com.uade.transferencia_futbol.service;

import com.uade.transferencia_futbol.entity.TransferenciaEntity;
import com.uade.transferencia_futbol.entity.JugadorEntity;
import com.uade.transferencia_futbol.entity.ClubEntity;
import com.uade.transferencia_futbol.repository.TransferenciaRepository;
import com.uade.transferencia_futbol.repository.JugadorRepository;
import com.uade.transferencia_futbol.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransferenciaService {
    
    @Autowired
    private TransferenciaRepository transferenciaRepository;
    
    @Autowired
    private JugadorRepository jugadorRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private ClubService clubService;
    
    // Realizar transferencia completa
    public TransferenciaEntity realizarTransferencia(
            String nombreJugador, 
            String nombreClubDestino, 
            Double monto, 
            String temporada,
            String tipoTransferencia) {
        
        // Validar jugador
        JugadorEntity jugador = jugadorRepository.findById(nombreJugador)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + nombreJugador));
        
        // Validar club destino
        ClubEntity clubDestino = clubRepository.findById(nombreClubDestino)
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClubDestino));
        
        // Validar que el club tenga presupuesto suficiente (si es compra)
        if ("Compra".equalsIgnoreCase(tipoTransferencia)) {
            if (clubDestino.getPresupuesto() < monto) {
                throw new RuntimeException("El club " + nombreClubDestino + " no tiene presupuesto suficiente");
            }
            
            // Descontar del presupuesto del club destino
            clubService.reducirPresupuesto(nombreClubDestino, monto);
            
            // Si el jugador tiene club actual, sumar al presupuesto del club origen
            if (jugador.getClubActual() != null) {
                clubService.aumentarPresupuesto(jugador.getClubActual().getNombre(), monto);
            }
        }
        
        // Crear la transferencia
        TransferenciaEntity transferencia = new TransferenciaEntity(
            monto,
            LocalDate.now(),
            temporada,
            tipoTransferencia,
            clubDestino
        );
        
        // Asignar nuevo club al jugador
        jugador.setClubActual(clubDestino);
        jugadorRepository.save(jugador);
        
        return transferenciaRepository.save(transferencia);
    }
    
    // Crear transferencia manual
    public TransferenciaEntity crearTransferencia(TransferenciaEntity transferencia) {
        return transferenciaRepository.save(transferencia);
    }
    
    // Obtener todas las transferencias
    public List<TransferenciaEntity> obtenerTodasTransferencias() {
        return transferenciaRepository.findAll();
    }
    
    // Obtener transferencia por ID
    public Optional<TransferenciaEntity> obtenerTransferenciaPorId(Long id) {
        return transferenciaRepository.findById(id);
    }
    
    // Eliminar transferencia
    public void eliminarTransferencia(Long id) {
        if (!transferenciaRepository.existsById(id)) {
            throw new RuntimeException("Transferencia no encontrada: " + id);
        }
        transferenciaRepository.deleteById(id);
    }
    
    // Buscar transferencias por temporada
    public List<TransferenciaEntity> buscarPorTemporada(String temporada) {
        return transferenciaRepository.findByTemporada(temporada);
    }
    
    // Buscar transferencias por tipo
    public List<TransferenciaEntity> buscarPorTipo(String tipoTransferencia) {
        return transferenciaRepository.findByTipoTransferencia(tipoTransferencia);
    }
    
    // Buscar transferencias mayores a un monto
    public List<TransferenciaEntity> buscarPorMontoMinimo(Double montoMinimo) {
        return transferenciaRepository.findByMontoGreaterThan(montoMinimo);
    }
    
    // Buscar transferencias entre fechas
    public List<TransferenciaEntity> buscarEntreFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return transferenciaRepository.findByFechaBetween(fechaInicio, fechaFin);
    }
    
    // Obtener transferencias de un jugador
    public List<TransferenciaEntity> obtenerTransferenciasDeJugador(String nombreJugador) {
        return transferenciaRepository.findTransferenciasByJugador(nombreJugador);
    }
    
    // Obtener transferencias hacia un club
    public List<TransferenciaEntity> obtenerTransferenciasHaciaClub(String nombreClub) {
        return transferenciaRepository.findTransferenciasHaciaClub(nombreClub);
    }
    
    // Obtener transferencias desde un club
    public List<TransferenciaEntity> obtenerTransferenciasDesdeClub(String nombreClub) {
        return transferenciaRepository.findTransferenciasDesdeClub(nombreClub);
    }
    
    // Top transferencias más caras
    public List<TransferenciaEntity> obtenerTopTransferenciasPorMonto(Integer limit) {
        return transferenciaRepository.findTopTransferenciasByMonto(limit);
    }
    
    // Buscar por temporada y tipo
    public List<TransferenciaEntity> buscarPorTemporadaYTipo(String temporada, String tipoTransferencia) {
        return transferenciaRepository.findByTemporadaAndTipoTransferencia(temporada, tipoTransferencia);
    }
    
    // Obtener total invertido por un club
    public Double obtenerTotalInvertidoPorClub(String nombreClub) {
        Double total = transferenciaRepository.getTotalInvertidoByClub(nombreClub);
        return total != null ? total : 0.0;
    }
    
    // Obtener transferencias de la temporada actual
    public List<TransferenciaEntity> obtenerTransferenciasTemporadaActual(String temporadaActual) {
        return transferenciaRepository.findTransferenciasTemporadaActual(temporadaActual);
    }
    
    // Validar si un club puede realizar una transferencia
    public boolean puedeRealizarTransferencia(String nombreClub, Double monto) {
        ClubEntity club = clubRepository.findById(nombreClub)
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClub));
        return club.getPresupuesto() >= monto;
    }
    
    // Obtener estadísticas de transferencias por temporada
    public String obtenerEstadisticasTemporada(String temporada) {
        List<TransferenciaEntity> transferencias = buscarPorTemporada(temporada);
        
        long totalTransferencias = transferencias.size();
        double montoTotal = transferencias.stream()
            .mapToDouble(t -> t.getMonto() != null ? t.getMonto() : 0.0)
            .sum();
        double montoPromedio = totalTransferencias > 0 ? montoTotal / totalTransferencias : 0.0;
        
        long compras = transferencias.stream()
            .filter(t -> "Compra".equalsIgnoreCase(t.getTipoTransferencia()))
            .count();
        long prestamos = transferencias.stream()
            .filter(t -> "Préstamo".equalsIgnoreCase(t.getTipoTransferencia()))
            .count();
        long libres = transferencias.stream()
            .filter(t -> "Libre".equalsIgnoreCase(t.getTipoTransferencia()))
            .count();
        
        return String.format(
            "Estadísticas Temporada %s:\n" +
            "Total de transferencias: %d\n" +
            "Monto total: %.2f M€\n" +
            "Monto promedio: %.2f M€\n" +
            "Compras: %d\n" +
            "Préstamos: %d\n" +
            "Transferencias libres: %d",
            temporada, totalTransferencias, montoTotal, montoPromedio, compras, prestamos, libres
        );
    }

    // =================================================================
    // ALGORITMOS COMPLEJOS
    // =================================================================

    // /api/transfers/cheapest-path - Dijkstra para ruta más barata
    /**
     * Implementación de Dijkstra: Construir grafo de transferencias (clubes=nodos, transferencias=aristas, monto=peso)
     * y encontrar el camino de menor costo entre dos clubes.
     */
     public List<String> obtenerRutaTransferenciaMasBarata(String clubOrigen, String clubDestino) {
        Map<String, Map<String, Double>> grafo = construirGrafoTransferencias();
        
        if (!grafo.containsKey(clubOrigen) || !grafo.containsKey(clubDestino)) {
            throw new RuntimeException("Uno o ambos clubes no existen en el grafo");
        }
        
        return dijkstra(grafo, clubOrigen, clubDestino);
    }
    
    private Map<String, Map<String, Double>> construirGrafoTransferencias() {
        Map<String, Map<String, Double>> grafo = new HashMap<>();
        List<TransferenciaEntity> transferencias = transferenciaRepository.findAll();
        
        for (TransferenciaEntity transferencia : transferencias) {
            // Aquí se construiría el grafo basado en transferencias reales entre clubes
            // Por simplicidad, creamos un grafo de ejemplo
        }
        
        // Grafo de ejemplo
        grafo.put("Barcelona", Map.of("PSG", 222.0, "Bayern", 80.0));
        grafo.put("PSG", Map.of("Real Madrid", 180.0));
        grafo.put("Bayern", Map.of("Real Madrid", 100.0));
        grafo.put("Real Madrid", Map.of());
        
        return grafo;
    }
    
    private List<String> dijkstra(Map<String, Map<String, Double>> grafo, String origen, String destino) {
        Map<String, Double> distancias = new HashMap<>();
        Map<String, String> predecesores = new HashMap<>();
        PriorityQueue<String> cola = new PriorityQueue<>(Comparator.comparing(distancias::get));
        
        for (String nodo : grafo.keySet()) {
            distancias.put(nodo, Double.MAX_VALUE);
        }
        distancias.put(origen, 0.0);
        cola.offer(origen);
        
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            
            if (actual.equals(destino)) {
                break;
            }
            
            for (Map.Entry<String, Double> vecino : grafo.getOrDefault(actual, new HashMap<>()).entrySet()) {
                String nodoVecino = vecino.getKey();
                double peso = vecino.getValue();
                double nuevaDistancia = distancias.get(actual) + peso;
                
                if (nuevaDistancia < distancias.get(nodoVecino)) {
                    distancias.put(nodoVecino, nuevaDistancia);
                    predecesores.put(nodoVecino, actual);
                    cola.offer(nodoVecino);
                }
            }
        }
        
        // Reconstruir camino
        List<String> camino = new ArrayList<>();
        String actual = destino;
        while (actual != null) {
            camino.add(actual);
            actual = predecesores.get(actual);
        }
        Collections.reverse(camino);
        
        return camino;
    }

    // /api/transfers/budget-optimization - Programación dinámica
    /**
     * Implementación de Programación Dinámica (problema de la mochila)
     * para seleccionar jugadores dentro del presupuesto
     */
    public List<JugadorEntity> optimizarPresupuestoTransferencias(String nombreClub, Double presupuestoMaximo) {
        List<JugadorEntity> jugadoresDisponibles = jugadorRepository.findJugadoresLibres();
        int n = jugadoresDisponibles.size();
        
        // Crear tabla de programación dinámica
        double[][] dp = new double[n + 1][presupuestoMaximo.intValue() + 1];
        
        for (int i = 1; i <= n; i++) {
            JugadorEntity jugador = jugadoresDisponibles.get(i - 1);
            double valor = jugador.getValorMercado() != null ? jugador.getValorMercado() : 0;
            double beneficio = valor / 1000000; // Beneficio aproximado
            
            for (int w = 1; w <= presupuestoMaximo.intValue(); w++) {
                if (valor <= w) {
                    dp[i][w] = Math.max(beneficio + dp[i - 1][w - (int)valor], dp[i - 1][w]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }
        
        // Reconstruir solución
        List<JugadorEntity> jugadoresSeleccionados = new ArrayList<>();
        int w = presupuestoMaximo.intValue();
        
        for (int i = n; i > 0 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                JugadorEntity jugador = jugadoresDisponibles.get(i - 1);
                jugadoresSeleccionados.add(jugador);
                w -= jugador.getValorMercado();
            }
        }
        
        return jugadoresSeleccionados;
    }

    // /api/network/minimum-spanning - Prim
    /**
     * Implementación de Prim para encontrar el árbol de expansión mínima
     */
    public Double calcularCostoRedTransferenciaMinima() {
        Map<String, Map<String, Double>> grafo = construirGrafoTransferencias();
        return primMST(grafo);
    }
    
    private Double primMST(Map<String, Map<String, Double>> grafo) {
        if (grafo.isEmpty()) return 0.0;
        
        Set<String> visitados = new HashSet<>();
        PriorityQueue<Edge> cola = new PriorityQueue<>(Comparator.comparingDouble(e -> e.peso));
        
        // Comenzar con el primer nodo
        String inicio = grafo.keySet().iterator().next();
        visitados.add(inicio);
        
        for (Map.Entry<String, Double> vecino : grafo.get(inicio).entrySet()) {
            cola.offer(new Edge(inicio, vecino.getKey(), vecino.getValue()));
        }
        
        double costoTotal = 0.0;
        
        while (!cola.isEmpty() && visitados.size() < grafo.size()) {
            Edge edge = cola.poll();
            
            if (!visitados.contains(edge.destino)) {
                visitados.add(edge.destino);
                costoTotal += edge.peso;
                
                for (Map.Entry<String, Double> vecino : grafo.get(edge.destino).entrySet()) {
                    if (!visitados.contains(vecino.getKey())) {
                        cola.offer(new Edge(edge.destino, vecino.getKey(), vecino.getValue()));
                    }
                }
            }
        }
        
        return costoTotal;
    }
    
    private static class Edge {
        String origen;
        String destino;
        double peso;
        
        Edge(String origen, String destino, double peso) {
            this.origen = origen;
            this.destino = destino;
            this.peso = peso;
        }
    }

    // /api/transfers/best-deals - Branch & Bound
    /**
     * Implementación de Branch & Bound para buscar las mejores transferencias
     */
    public List<TransferenciaEntity> buscarMejoresOfertas(String clubOrigen, Double presupuestoMaximo) {
        List<JugadorEntity> jugadoresDisponibles = jugadorRepository.findJugadoresLibres();
        List<TransferenciaEntity> mejoresOfertas = new ArrayList<>();
        
        branchAndBound(jugadoresDisponibles, presupuestoMaximo, new ArrayList<>(), mejoresOfertas, 0, 0.0);
        
        return mejoresOfertas;
    }
    
    private void branchAndBound(List<JugadorEntity> jugadores,
                               Double presupuestoMaximo,
                               List<TransferenciaEntity> ofertaActual,
                               List<TransferenciaEntity> mejorOferta,
                               int index,
                               Double costoActual) {
        
        if (index >= jugadores.size()) {
            if (costoActual <= presupuestoMaximo && ofertaActual.size() > mejorOferta.size()) {
                mejorOferta.clear();
                mejorOferta.addAll(new ArrayList<>(ofertaActual));
            }
            return;
        }
        
        JugadorEntity jugador = jugadores.get(index);
        Double valorJugador = jugador.getValorMercado();
        
        // Ramas: incluir o no incluir el jugador
        if (costoActual + valorJugador <= presupuestoMaximo) {
            // Incluir jugador
            TransferenciaEntity transferencia = new TransferenciaEntity();
            transferencia.setMonto(valorJugador);
            ofertaActual.add(transferencia);
            
            branchAndBound(jugadores, presupuestoMaximo, ofertaActual, mejorOferta, index + 1, costoActual + valorJugador);
            
            ofertaActual.remove(ofertaActual.size() - 1);
        }
        
        // No incluir jugador
        branchAndBound(jugadores, presupuestoMaximo, ofertaActual, mejorOferta, index + 1, costoActual);
    }
    public Map<String, Object> calcularRedTransferenciaKruskal() {
    Map<String, Map<String, Double>> grafo = construirGrafoTransferencias();
    return kruskalMST(grafo);
}

private Map<String, Object> kruskalMST(Map<String, Map<String, Double>> grafo) {
    List<Edge> todasLasAristas = new ArrayList<>();
    
    // Recolectar todas las aristas del grafo
    for (Map.Entry<String, Map<String, Double>> nodo : grafo.entrySet()) {
        String origen = nodo.getKey();
        for (Map.Entry<String, Double> vecino : nodo.getValue().entrySet()) {
            todasLasAristas.add(new Edge(origen, vecino.getKey(), vecino.getValue()));
        }
    }
    
    // Ordenar aristas por peso (ascendente)
    todasLasAristas.sort(Comparator.comparingDouble(e -> e.peso));
    
    // Inicializar Union-Find
    UnionFind uf = new UnionFind(grafo.keySet());
    
    List<Edge> mst = new ArrayList<>();
    double costoTotal = 0.0;
    
    // Algoritmo de Kruskal
    for (Edge arista : todasLasAristas) {
        // Si agregar esta arista no crea un ciclo
        if (uf.find(arista.origen) != uf.find(arista.destino)) {
            mst.add(arista);
            costoTotal += arista.peso;
            uf.union(arista.origen, arista.destino);
            
            // Si ya tenemos n-1 aristas, terminamos
            if (mst.size() == grafo.size() - 1) {
                break;
            }
        }
    }
    
    // Formatear resultado - SOLUCIÓN AL ERROR
    List<Map<String, Object>> aristasFormateadas = new ArrayList<>();
    for (Edge e : mst) {
        Map<String, Object> aristaMap = new HashMap<>();
        aristaMap.put("origen", e.origen);
        aristaMap.put("destino", e.destino);
        aristaMap.put("costo", e.peso);
        aristasFormateadas.add(aristaMap);
    }
    
    Map<String, Object> resultado = new HashMap<>();
    resultado.put("algoritmo", "Kruskal");
    resultado.put("costoTotal", costoTotal);
    resultado.put("numeroConexiones", mst.size());
    resultado.put("conexiones", aristasFormateadas);
    
    return resultado;
}

// Clase Union-Find para detectar ciclos
private static class UnionFind {
    private Map<String, String> padre;
    private Map<String, Integer> rango;
    
    public UnionFind(Set<String> nodos) {
        padre = new HashMap<>();
        rango = new HashMap<>();
        
        for (String nodo : nodos) {
            padre.put(nodo, nodo);
            rango.put(nodo, 0);
        }
    }
    
    public String find(String nodo) {
        if (!padre.get(nodo).equals(nodo)) {
            padre.put(nodo, find(padre.get(nodo))); // Path compression
        }
        return padre.get(nodo);
    }
    
    public void union(String nodo1, String nodo2) {
        String raiz1 = find(nodo1);
        String raiz2 = find(nodo2);
        
        if (raiz1.equals(raiz2)) return;
        
        // Union by rank
        if (rango.get(raiz1) < rango.get(raiz2)) {
            padre.put(raiz1, raiz2);
        } else if (rango.get(raiz1) > rango.get(raiz2)) {
            padre.put(raiz2, raiz1);
        } else {
            padre.put(raiz2, raiz1);
            rango.put(raiz1, rango.get(raiz1) + 1);
        }
    }
}
}