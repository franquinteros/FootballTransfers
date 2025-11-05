package com.uade.transferencia_futbol.service;

import com.uade.transferencia_futbol.entity.JugadorEntity;
import com.uade.transferencia_futbol.entity.ClubEntity;
import com.uade.transferencia_futbol.entity.AgenteEntity;
import com.uade.transferencia_futbol.entity.TransferenciaEntity;
import com.uade.transferencia_futbol.repository.JugadorRepository;
import com.uade.transferencia_futbol.repository.ClubRepository;
import com.uade.transferencia_futbol.repository.AgenteRepository;
import com.uade.transferencia_futbol.repository.TransferenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class JugadorService {
    
    @Autowired
    private JugadorRepository jugadorRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private AgenteRepository agenteRepository;
    
    @Autowired
    private TransferenciaRepository transferenciaRepository;
    
    // Crear jugador
    public JugadorEntity crearJugador(JugadorEntity jugador) {
        if (jugadorRepository.existsById(jugador.getNombre())) {
            throw new RuntimeException("El jugador ya existe: " + jugador.getNombre());
        }
        return jugadorRepository.save(jugador);
    }
    
    // Obtener todos los jugadores
    public List<JugadorEntity> obtenerTodosJugadores() {
        return jugadorRepository.findAll();
    }
    
    // Obtener jugador por nombre
    public Optional<JugadorEntity> obtenerJugadorPorNombre(String nombre) {
        return jugadorRepository.findById(nombre);
    }
    
    // Actualizar jugador
    public JugadorEntity actualizarJugador(String nombre, JugadorEntity jugadorActualizado) {
        return jugadorRepository.findById(nombre)
            .map(jugador -> {
                jugador.setEdad(jugadorActualizado.getEdad());
                jugador.setPosicion(jugadorActualizado.getPosicion());
                jugador.setValorMercado(jugadorActualizado.getValorMercado());
                jugador.setNacionalidad(jugadorActualizado.getNacionalidad());
                return jugadorRepository.save(jugador);
            })
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + nombre));
    }
    
    // Eliminar jugador
    public void eliminarJugador(String nombre) {
        if (!jugadorRepository.existsById(nombre)) {
            throw new RuntimeException("Jugador no encontrado: " + nombre);
        }
        jugadorRepository.deleteById(nombre);
    }
    
    // Asignar jugador a un club
    public JugadorEntity asignarClub(String nombreJugador, String nombreClub) {
        JugadorEntity jugador = jugadorRepository.findById(nombreJugador)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + nombreJugador));
        
        ClubEntity club = clubRepository.findById(nombreClub)
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClub));
        
        jugador.setClubActual(club);
        return jugadorRepository.save(jugador);
    }
    
    // Asignar agente a jugador
    public JugadorEntity asignarAgente(String nombreJugador, String nombreAgente) {
        JugadorEntity jugador = jugadorRepository.findById(nombreJugador)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + nombreJugador));
        
        AgenteEntity agente = agenteRepository.findById(nombreAgente)
            .orElseThrow(() -> new RuntimeException("Agente no encontrado: " + nombreAgente));
        
        jugador.setAgente(agente);
        return jugadorRepository.save(jugador);
    }
    
    // Buscar jugadores por posición
    public List<JugadorEntity> buscarPorPosicion(String posicion) {
        return jugadorRepository.findByPosicion(posicion);
    }
    
    // Buscar jugadores por nacionalidad
    public List<JugadorEntity> buscarPorNacionalidad(String nacionalidad) {
        return jugadorRepository.findByNacionalidad(nacionalidad);
    }
    
    // Buscar jugadores por rango de edad
    public List<JugadorEntity> buscarPorRangoEdad(Integer edadMin, Integer edadMax) {
        return jugadorRepository.findByEdadBetween(edadMin, edadMax);
    }
    
    // Buscar jugadores por valor de mercado mínimo
    public List<JugadorEntity> buscarPorValorMinimo(Double valorMinimo) {
        return jugadorRepository.findByValorMercadoGreaterThan(valorMinimo);
    }
    
    // Obtener jugadores de un club
    public List<JugadorEntity> obtenerJugadoresDeClub(String nombreClub) {
        return jugadorRepository.findJugadoresByClub(nombreClub);
    }
    
    // Obtener jugadores de un agente
    public List<JugadorEntity> obtenerJugadoresDeAgente(String nombreAgente) {
        return jugadorRepository.findJugadoresByAgente(nombreAgente);
    }
    
    // Obtener jugadores libres (sin club)
    public List<JugadorEntity> obtenerJugadoresLibres() {
        return jugadorRepository.findJugadoresLibres();
    }
    
    // Obtener top jugadores por valor
    public List<JugadorEntity> obtenerTopJugadores(Integer limit) {
        return jugadorRepository.findTopJugadoresByValor(limit);
    }
    
    // Actualizar valor de mercado
    public JugadorEntity actualizarValorMercado(String nombre, Double nuevoValor) {
        return jugadorRepository.findById(nombre)
            .map(jugador -> {
                jugador.setValorMercado(nuevoValor);
                return jugadorRepository.save(jugador);
            })
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + nombre));
    }

    // =================================================================
    // ALGORITMOS COMPLEJOS
    // =================================================================

    // /api/players/career-path - BFS/DFS del historial
    /**
     * Implementación de BFS o DFS para recorrer el historial de clubes y temporadas
     * (deberá interactuar con TransferenciaService o TransferenciaRepository para obtener el historial).
     */
public List<String> obtenerRutaCarreraJugador(String nombreJugador) {
        List<String> rutaCarrera = new ArrayList<>();
        
        // Obtener transferencias del jugador - CORREGIDO: usa TransferenciaEntity
        List<TransferenciaEntity> transferencias = transferenciaRepository.findTransferenciasByJugador(nombreJugador);
        
        // Construir grafo de clubes basado en transferencias reales
        Map<String, List<String>> grafoClubes = construirGrafoCarrera(transferencias);
        
        // BFS desde el club actual hacia atrás en el tiempo
        JugadorEntity jugador = jugadorRepository.findById(nombreJugador)
            .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + nombreJugador));
        
        if (jugador.getClubActual() != null) {
            String clubActual = jugador.getClubActual().getNombre();
            bfsCarrera(clubActual, grafoClubes, rutaCarrera);
        }
        
        return rutaCarrera;
    }
    
    private Map<String, List<String>> construirGrafoCarrera(List<TransferenciaEntity> transferencias) {
        Map<String, List<String>> grafo = new HashMap<>();
        
        // Ordenar transferencias por fecha (más reciente primero)
        transferencias.sort((t1, t2) -> t2.getFecha().compareTo(t1.getFecha()));
        
        // Construir grafo de movimientos entre clubes
        for (int i = 0; i < transferencias.size() - 1; i++) {
            TransferenciaEntity actual = transferencias.get(i);
            // Aquí necesitaríamos información del club origen (no disponible en TransferenciaEntity actual)
            // Por ahora, usamos una implementación simplificada
        }
        
        return grafo;
    }
    
    private void bfsCarrera(String clubActual, Map<String, List<String>> grafoClubes, List<String> rutaCarrera) {
        Queue<String> cola = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        
        cola.offer(clubActual);
        visitados.add(clubActual);
        
        while (!cola.isEmpty()) {
            String club = cola.poll();
            rutaCarrera.add(club);
            
            // Agregar clubes conectados (anteriores en la carrera)
            List<String> clubesConectados = grafoClubes.getOrDefault(club, new ArrayList<>());
            for (String clubConectado : clubesConectados) {
                if (!visitados.contains(clubConectado)) {
                    visitados.add(clubConectado);
                    cola.offer(clubConectado);
                }
            }
        }
    }

    // /api/players/sort-by-value - QuickSort personalizado
    /**
     * Implementación de QuickSort para ordenar jugadores por valor de mercado
     */
    public List<JugadorEntity> obtenerJugadoresOrdenadosPorValor() {
        List<JugadorEntity> jugadores = jugadorRepository.findAll();
        
        // Filtrar jugadores con valor de mercado nulo
        jugadores.removeIf(j -> j.getValorMercado() == null);
        
        if (!jugadores.isEmpty()) {
            quickSort(jugadores, 0, jugadores.size() - 1);
        }
        
        return jugadores;
    }
    
    private void quickSort(List<JugadorEntity> jugadores, int low, int high) {
        if (low < high) {
            int pi = partition(jugadores, low, high);
            quickSort(jugadores, low, pi - 1);
            quickSort(jugadores, pi + 1, high);
        }
    }
    
    private int partition(List<JugadorEntity> jugadores, int low, int high) {
        double pivot = jugadores.get(high).getValorMercado();
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            // Orden descendente (mayor valor primero)
            if (jugadores.get(j).getValorMercado() >= pivot) {
                i++;
                Collections.swap(jugadores, i, j);
            }
        }
        
        Collections.swap(jugadores, i + 1, high);
        return i + 1;
    }

    // REEMPLAZAR EL MÉTODO obtenerRutaCarreraJugador EN JugadorService

// =================================================================
// BFS - Recorrido de carrera del jugador MEJORADO
// =================================================================

/**
 * Implementación de BFS para recorrer el historial de clubes del jugador
 * usando el historial de transferencias real
 */
public Map<String, Object> obtenerRutaCarreraJugadorBFS(String nombreJugador) {
    JugadorEntity jugador = jugadorRepository.findById(nombreJugador)
        .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + nombreJugador));
    
    // Obtener historial de clubes desde el repository
    List<String> historialClubes = transferenciaRepository.findHistorialClubesJugador(nombreJugador);
    
    // Si tiene club actual, agregarlo al final
    if (jugador.getClubActual() != null && !historialClubes.isEmpty()) {
        historialClubes.add(jugador.getClubActual().getNombre());
    }
    
    // Construir grafo de conexiones entre clubes
    Map<String, List<String>> grafo = construirGrafoCarreraMejorado(historialClubes);
    
    // BFS desde el primer club
    List<String> rutaBFS = new ArrayList<>();
    Set<String> visitados = new HashSet<>();
    
    if (!historialClubes.isEmpty()) {
        String clubInicial = historialClubes.get(0);
        bfsRecorrido(clubInicial, grafo, rutaBFS, visitados);
    }
    
    return Map.of(
        "jugador", nombreJugador,
        "algoritmo", "BFS",
        "historialCompleto", historialClubes,
        "recorridoBFS", rutaBFS,
        "totalClubes", historialClubes.size(),
        "clubActual", jugador.getClubActual() != null ? jugador.getClubActual().getNombre() : "Sin club"
    );
}

/**
 * Implementación de DFS para recorrer el historial de clubes del jugador
 */
public Map<String, Object> obtenerRutaCarreraJugadorDFS(String nombreJugador) {
    JugadorEntity jugador = jugadorRepository.findById(nombreJugador)
        .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + nombreJugador));
    
    // Obtener historial de clubes
    List<String> historialClubes = transferenciaRepository.findHistorialClubesJugador(nombreJugador);
    
    if (jugador.getClubActual() != null && !historialClubes.isEmpty()) {
        historialClubes.add(jugador.getClubActual().getNombre());
    }
    
    // Construir grafo de conexiones
    Map<String, List<String>> grafo = construirGrafoCarreraMejorado(historialClubes);
    
    // DFS desde el primer club
    List<String> rutaDFS = new ArrayList<>();
    Set<String> visitados = new HashSet<>();
    
    if (!historialClubes.isEmpty()) {
        String clubInicial = historialClubes.get(0);
        dfsRecorrido(clubInicial, grafo, rutaDFS, visitados);
    }
    
    return Map.of(
        "jugador", nombreJugador,
        "algoritmo", "DFS",
        "historialCompleto", historialClubes,
        "recorridoDFS", rutaDFS,
        "totalClubes", historialClubes.size(),
        "clubActual", jugador.getClubActual() != null ? jugador.getClubActual().getNombre() : "Sin club"
    );
}

private Map<String, List<String>> construirGrafoCarreraMejorado(List<String> historialClubes) {
    Map<String, List<String>> grafo = new HashMap<>();
    
    // Crear conexiones secuenciales entre clubes
    for (int i = 0; i < historialClubes.size() - 1; i++) {
        String clubActual = historialClubes.get(i);
        String clubSiguiente = historialClubes.get(i + 1);
        
        grafo.putIfAbsent(clubActual, new ArrayList<>());
        grafo.get(clubActual).add(clubSiguiente);
        
        // Conexión bidireccional (opcional, para explorar en ambas direcciones)
        grafo.putIfAbsent(clubSiguiente, new ArrayList<>());
    }
    
    return grafo;
}

private void bfsRecorrido(String clubInicial, Map<String, List<String>> grafo, 
                         List<String> rutaBFS, Set<String> visitados) {
    Queue<String> cola = new LinkedList<>();
    cola.offer(clubInicial);
    visitados.add(clubInicial);
    
    while (!cola.isEmpty()) {
        String club = cola.poll();
        rutaBFS.add(club);
        
        List<String> clubesConectados = grafo.getOrDefault(club, new ArrayList<>());
        for (String clubConectado : clubesConectados) {
            if (!visitados.contains(clubConectado)) {
                visitados.add(clubConectado);
                cola.offer(clubConectado);
            }
        }
    }
}

private void dfsRecorrido(String club, Map<String, List<String>> grafo, 
                         List<String> rutaDFS, Set<String> visitados) {
    visitados.add(club);
    rutaDFS.add(club);
    
    List<String> clubesConectados = grafo.getOrDefault(club, new ArrayList<>());
    for (String clubConectado : clubesConectados) {
        if (!visitados.contains(clubConectado)) {
            dfsRecorrido(clubConectado, grafo, rutaDFS, visitados);
        }
    }
}
}
