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
    
    // ==================== MÉTODOS CRUD Y BÁSICOS ====================
    
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
    
    public TransferenciaEntity crearTransferencia(TransferenciaEntity transferencia) {
        return transferenciaRepository.save(transferencia);
    }
    
    public List<TransferenciaEntity> obtenerTodasTransferencias() {
        return transferenciaRepository.findAll();
    }
    
    public Optional<TransferenciaEntity> obtenerTransferenciaPorId(Long id) {
        return transferenciaRepository.findById(id);
    }
    
    public void eliminarTransferencia(Long id) {
        if (!transferenciaRepository.existsById(id)) {
            throw new RuntimeException("Transferencia no encontrada: " + id);
        }
        transferenciaRepository.deleteById(id);
    }
    
    public List<TransferenciaEntity> buscarPorTemporada(String temporada) {
        return transferenciaRepository.findByTemporada(temporada);
    }
    
    public List<TransferenciaEntity> buscarPorTipo(String tipoTransferencia) {
        return transferenciaRepository.findByTipoTransferencia(tipoTransferencia);
    }
    
    public List<TransferenciaEntity> buscarPorMontoMinimo(Double montoMinimo) {
        return transferenciaRepository.findByMontoGreaterThan(montoMinimo);
    }
    
    public List<TransferenciaEntity> buscarEntreFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return transferenciaRepository.findByFechaBetween(fechaInicio, fechaFin);
    }
    
    public List<TransferenciaEntity> obtenerTransferenciasDeJugador(String nombreJugador) {
        return transferenciaRepository.findTransferenciasByJugador(nombreJugador);
    }
    
    public List<TransferenciaEntity> obtenerTransferenciasHaciaClub(String nombreClub) {
        return transferenciaRepository.findTransferenciasHaciaClub(nombreClub);
    }
    
    public List<TransferenciaEntity> obtenerTransferenciasDesdeClub(String nombreClub) {
        return transferenciaRepository.findTransferenciasDesdeClub(nombreClub);
    }
    
    public List<TransferenciaEntity> obtenerTopTransferenciasPorMonto(Integer limit) {
        return transferenciaRepository.findTopTransferenciasByMonto(limit);
    }
    
    public List<TransferenciaEntity> buscarPorTemporadaYTipo(String temporada, String tipoTransferencia) {
        return transferenciaRepository.findByTemporadaAndTipoTransferencia(temporada, tipoTransferencia);
    }
    
    public Double obtenerTotalInvertidoPorClub(String nombreClub) {
        Double total = transferenciaRepository.getTotalInvertidoByClub(nombreClub);
        return total != null ? total : 0.0;
    }
    
    public List<TransferenciaEntity> obtenerTransferenciasTemporadaActual(String temporadaActual) {
        return transferenciaRepository.findTransferenciasTemporadaActual(temporadaActual);
    }
    
    public boolean puedeRealizarTransferencia(String nombreClub, Double monto) {
        ClubEntity club = clubRepository.findById(nombreClub)
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClub));
        return club.getPresupuesto() >= monto;
    }
    
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
    // ALGORITMOS COMPLEJOS - DIJKSTRA
    // =================================================================

    public List<String> obtenerRutaTransferenciaMasBarata(String clubOrigen, String clubDestino) {
        Map<String, Map<String, Double>> grafo = construirGrafoTransferencias();
        
        if (!grafo.containsKey(clubOrigen) || !grafo.containsKey(clubDestino)) {
            throw new RuntimeException("Uno o ambos clubes no existen en el grafo");
        }
        
        return dijkstra(grafo, clubOrigen, clubDestino);
    }
    
    private Map<String, Map<String, Double>> construirGrafoTransferencias() {
        Map<String, Map<String, Double>> grafo = new HashMap<>();
        
        // Grafo de ejemplo basado en transferencias reales
        grafo.put("Barcelona", Map.of("PSG", 222.0, "Bayern", 80.0));
        grafo.put("PSG", Map.of("Real Madrid", 180.0));
        grafo.put("Bayern", Map.of("Real Madrid", 100.0));
        grafo.put("Real Madrid", Map.of());
        grafo.put("Manchester United", Map.of("Juventus", 85.0, "Chelsea", 75.0));
        grafo.put("Juventus", Map.of("Barcelona", 120.0));
        grafo.put("Chelsea", Map.of("PSG", 65.0));
        
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

    // =================================================================
    // ALGORITMOS COMPLEJOS - PROGRAMACIÓN DINÁMICA
    // =================================================================

    public Map<String, Object> optimizarPresupuestoTransferencias(String nombreClub, Double presupuestoMaximo) {
        ClubEntity club = clubRepository.findById(nombreClub)
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClub));
        
        // Obtener jugadores libres disponibles
        List<JugadorEntity> jugadoresLibres = jugadorRepository.findJugadoresLibres();
        
        // Filtrar jugadores que están dentro del presupuesto y tienen valor de mercado
        List<JugadorEntity> jugadoresDisponibles = jugadoresLibres.stream()
            .filter(j -> j.getValorMercado() != null && j.getValorMercado() > 0 && j.getValorMercado() <= presupuestoMaximo)
            .sorted((j1, j2) -> Double.compare(j2.getValorMercado(), j1.getValorMercado()))
            .collect(Collectors.toList());
        
        if (jugadoresDisponibles.isEmpty()) {
            throw new RuntimeException("No hay jugadores disponibles dentro del presupuesto de " + presupuestoMaximo);
        }
        
        // Aplicar programación dinámica
        List<JugadorEntity> jugadoresSeleccionados = programacionDinamicaMochila(jugadoresDisponibles, presupuestoMaximo);
        
        // Calcular métricas
        double valorTotal = calcularValorTotal(jugadoresSeleccionados);
        double costoTotal = calcularCostoTotal(jugadoresSeleccionados);
        double eficiencia = costoTotal > 0 ? valorTotal / costoTotal : 0;
        
        // Preparar respuesta
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("club", nombreClub);
        resultado.put("presupuestoMaximo", presupuestoMaximo);
        resultado.put("algoritmo", "Programación Dinámica (Problema de la Mochila)");
        resultado.put("jugadoresSeleccionados", jugadoresSeleccionados);
        resultado.put("totalJugadores", jugadoresSeleccionados.size());
        resultado.put("valorTotal", valorTotal);
        resultado.put("costoTotal", costoTotal);
        resultado.put("presupuestoRestante", presupuestoMaximo - costoTotal);
        resultado.put("eficiencia", eficiencia);
        resultado.put("jugadoresDisponibles", jugadoresDisponibles.size());
        resultado.put("desglosePosiciones", obtenerDesglosePorPosicion(jugadoresSeleccionados));
        
        return resultado;
    }

    /**
 * Implementación del algoritmo de Programación Dinámica (Problema de la Mochila 0/1)
 * Maximiza el valor total sin exceder la capacidad (presupuesto)
 */
private List<JugadorEntity> programacionDinamicaMochila(List<JugadorEntity> jugadores, Double presupuestoMaximo) {
    int n = jugadores.size();
    int capacidad = presupuestoMaximo.intValue(); // CORREGIDO: usa intValue()
    
    // Crear tabla de programación dinámica
    double[][] dp = new double[n + 1][capacidad + 1];
    
    // Llenar la tabla dp
    for (int i = 1; i <= n; i++) {
        JugadorEntity jugador = jugadores.get(i - 1);
        double valorJugador = jugador.getValorMercado();
        int peso = (int) Math.round(valorJugador); // CORREGIDO: usa Math.round para mejor precisión
        double beneficio = calcularBeneficioJugador(jugador);
        
        for (int w = 1; w <= capacidad; w++) {
            if (peso <= w) {
                dp[i][w] = Math.max(beneficio + dp[i - 1][w - peso], dp[i - 1][w]);
            } else {
                dp[i][w] = dp[i - 1][w];
            }
        }
    }
    
    // Reconstruir la solución
    List<JugadorEntity> seleccionados = new ArrayList<>();
    int w = capacidad;
    
    for (int i = n; i > 0 && w > 0; i--) {
        if (dp[i][w] != dp[i - 1][w]) {
            JugadorEntity jugador = jugadores.get(i - 1);
            seleccionados.add(jugador);
            int peso = (int) Math.round(jugador.getValorMercado()); // CORREGIDO: usa Math.round
            w -= peso;
        }
    }
    
    return seleccionados;
}

    /**
 * Versión mejorada que considera balance de posiciones - CORREGIDO SIN LAMBDA
 */
public Map<String, Object> optimizarPresupuestoBalanceado(String nombreClub, Double presupuestoMaximo, 
                                                         Map<String, Integer> posicionesRequeridas) {
    ClubEntity club = clubRepository.findById(nombreClub)
        .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClub));
    
    // Obtener jugadores libres agrupados por posición
    List<JugadorEntity> jugadoresLibres = jugadorRepository.findJugadoresLibres();
    Map<String, List<JugadorEntity>> jugadoresPorPosicion = agruparJugadoresPorPosicion(jugadoresLibres);
    
    // Aplicar programación dinámica por posición
    List<JugadorEntity> equipoOptimo = new ArrayList<>();
    double presupuestoRestante = presupuestoMaximo;
    
    for (Map.Entry<String, Integer> entry : posicionesRequeridas.entrySet()) {
        String posicion = entry.getKey();
        int cantidad = entry.getValue();
        
        // CORREGIDO: Filtrar manualmente sin lambda
        List<JugadorEntity> jugadoresFiltrados = new ArrayList<>();
        List<JugadorEntity> jugadoresPosicion = jugadoresPorPosicion.getOrDefault(posicion, new ArrayList<>());
        
        for (JugadorEntity jugador : jugadoresPosicion) {
            if (jugador.getValorMercado() != null && jugador.getValorMercado() <= presupuestoRestante) {
                jugadoresFiltrados.add(jugador);
            }
        }
        
        if (!jugadoresFiltrados.isEmpty()) {
            List<JugadorEntity> mejoresParaPosicion = programacionDinamicaPorPosicion(
                jugadoresFiltrados, presupuestoRestante, cantidad);
            
            equipoOptimo.addAll(mejoresParaPosicion);
            presupuestoRestante -= calcularCostoTotal(mejoresParaPosicion);
        }
    }
    
    // Calcular métricas
    double valorTotal = calcularValorTotal(equipoOptimo);
    double costoTotal = calcularCostoTotal(equipoOptimo);
    
    Map<String, Object> resultado = new HashMap<>();
    resultado.put("club", nombreClub);
    resultado.put("presupuestoMaximo", presupuestoMaximo);
    resultado.put("algoritmo", "Programación Dinámica Balanceada");
    resultado.put("equipoOptimo", equipoOptimo);
    resultado.put("totalJugadores", equipoOptimo.size());
    resultado.put("valorTotal", valorTotal);
    resultado.put("costoTotal", costoTotal);
    resultado.put("presupuestoRestante", presupuestoMaximo - costoTotal);
    resultado.put("desglosePosiciones", obtenerDesglosePorPosicion(equipoOptimo));
    
    return resultado;
}

/**
 * Programación dinámica para una posición específica - CORREGIDO
 */
private List<JugadorEntity> programacionDinamicaPorPosicion(List<JugadorEntity> jugadores, 
                                                           Double presupuesto, int cantidadRequerida) {
    if (jugadores.size() <= cantidadRequerida) {
        return new ArrayList<>(jugadores);
    }
    
    // Ordenar por eficiencia (valor/costo)
    jugadores.sort((j1, j2) -> {
        double eficiencia1 = calcularBeneficioJugador(j1) / j1.getValorMercado();
        double eficiencia2 = calcularBeneficioJugador(j2) / j2.getValorMercado();
        return Double.compare(eficiencia2, eficiencia1);
    });
    
    // Tomar los mejores hasta la cantidad requerida o agotar presupuesto
    List<JugadorEntity> seleccionados = new ArrayList<>();
    double presupuestoActual = presupuesto; // Variable local para el bucle
    
    for (JugadorEntity jugador : jugadores) {
        if (seleccionados.size() < cantidadRequerida && jugador.getValorMercado() <= presupuestoActual) {
            seleccionados.add(jugador);
            presupuestoActual -= jugador.getValorMercado();
        }
    }
    
    return seleccionados;
}


    // ==================== MÉTODOS AUXILIARES ====================

    private double calcularBeneficioJugador(JugadorEntity jugador) {
        // El beneficio considera edad, valor de mercado y posición
        double base = jugador.getValorMercado() != null ? jugador.getValorMercado() : 0;
        
        // Bonus por juventud
        double bonusEdad = 1.0;
        if (jugador.getEdad() != null && jugador.getEdad() < 23) {
            bonusEdad = 1.5; // 50% más de beneficio para jóvenes
        } else if (jugador.getEdad() != null && jugador.getEdad() < 28) {
            bonusEdad = 1.2; // 20% más para jugadores en su prime
        }
        
        // Bonus por posición (las posiciones más escasas tienen más valor)
        double bonusPosicion = 1.0;
        String posicion = jugador.getPosicion();
        if (posicion != null) {
            if (posicion.toLowerCase().contains("arquero") || posicion.toLowerCase().contains("portero")) {
                bonusPosicion = 1.3; // Arqueros buenos son escasos
            } else if (posicion.toLowerCase().contains("delantero")) {
                bonusPosicion = 1.2; // Delanteros anotadores son valiosos
            }
        }
        
        return base * bonusEdad * bonusPosicion;
    }

    private Map<String, List<JugadorEntity>> agruparJugadoresPorPosicion(List<JugadorEntity> jugadores) {
        Map<String, List<JugadorEntity>> grupos = new HashMap<>();
        
        for (JugadorEntity jugador : jugadores) {
            String categoria = determinarCategoriaPosicion(jugador.getPosicion());
            if (!grupos.containsKey(categoria)) {
                grupos.put(categoria, new ArrayList<>());
            }
            grupos.get(categoria).add(jugador);
        }
        
        return grupos;
    }

    private String determinarCategoriaPosicion(String posicion) {
        if (posicion == null) return "Mediocampo";
        String pos = posicion.toLowerCase().trim();
        
        if (pos.contains("arquero") || pos.contains("portero")) return "Arquero";
        if (pos.contains("defensa") || pos.contains("lateral") || pos.contains("central")) return "Defensa";
        if (pos.contains("delantero") || pos.contains("atacante") || pos.contains("extremo")) return "Delantero";
        return "Mediocampo";
    }

    private double calcularValorTotal(List<JugadorEntity> jugadores) {
        return jugadores.stream()
            .mapToDouble(j -> j.getValorMercado() != null ? j.getValorMercado() : 0.0)
            .sum();
    }

    private double calcularCostoTotal(List<JugadorEntity> jugadores) {
        return jugadores.stream()
            .mapToDouble(j -> j.getValorMercado() != null ? j.getValorMercado() : 0.0)
            .sum();
    }

    private Map<String, Integer> obtenerDesglosePorPosicion(List<JugadorEntity> jugadores) {
        Map<String, Integer> desglose = new HashMap<>();
        for (JugadorEntity jugador : jugadores) {
            String categoria = determinarCategoriaPosicion(jugador.getPosicion());
            desglose.put(categoria, desglose.getOrDefault(categoria, 0) + 1);
        }
        return desglose;
    }

    // =================================================================
    // ALGORITMOS COMPLEJOS - PRIM/KRUSKAL
    // =================================================================

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

    // =================================================================
    // ALGORITMOS COMPLEJOS - BRANCH & BOUND
    // =================================================================

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
}