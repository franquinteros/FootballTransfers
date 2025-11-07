package com.uade.transferencia_futbol.service;

import com.uade.transferencia_futbol.entity.AgenteEntity;
import com.uade.transferencia_futbol.entity.JugadorEntity;
import com.uade.transferencia_futbol.repository.AgenteRepository;
import com.uade.transferencia_futbol.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class AgenteService {
    
    @Autowired
    private AgenteRepository agenteRepository;
    
    @Autowired
    private JugadorRepository jugadorRepository;
    
    // Crear agente
    public AgenteEntity crearAgente(AgenteEntity agente) {
        if (agenteRepository.existsById(agente.getNombre())) {
            throw new RuntimeException("El agente ya existe: " + agente.getNombre());
        }
        return agenteRepository.save(agente);
    }
    
    // Obtener todos los agentes
    public List<AgenteEntity> obtenerTodosAgentes() {
        return agenteRepository.findAll();
    }
    
    // Obtener agente por nombre
    public Optional<AgenteEntity> obtenerAgentePorNombre(String nombre) {
        return agenteRepository.findById(nombre);
    }
    
    // Actualizar agente
    public AgenteEntity actualizarAgente(String nombre, AgenteEntity agenteActualizado) {
        return agenteRepository.findById(nombre)
            .map(agente -> {
                agente.setAgencia(agenteActualizado.getAgencia());
                agente.setComision(agenteActualizado.getComision());
                agente.setNacionalidad(agenteActualizado.getNacionalidad());
                return agenteRepository.save(agente);
            })
            .orElseThrow(() -> new RuntimeException("Agente no encontrado: " + nombre));
    }
    
    // Eliminar agente
    public void eliminarAgente(String nombre) {
        if (!agenteRepository.existsById(nombre)) {
            throw new RuntimeException("Agente no encontrado: " + nombre);
        }
        agenteRepository.deleteById(nombre);
    }
    
    // Buscar agentes por agencia
    public List<AgenteEntity> buscarPorAgencia(String agencia) {
        return agenteRepository.findByAgencia(agencia);
    }
    
    // Buscar agentes por nacionalidad
    public List<AgenteEntity> buscarPorNacionalidad(String nacionalidad) {
        return agenteRepository.findByNacionalidad(nacionalidad);
    }
    
    // Buscar agentes con comisión mayor a un porcentaje
    public List<AgenteEntity> buscarPorComisionMinima(Double comisionMinima) {
        return agenteRepository.findByComisionGreaterThan(comisionMinima);
    }
    
    // Buscar agentes con comisión en un rango
    public List<AgenteEntity> buscarPorRangoComision(Double comisionMin, Double comisionMax) {
        return agenteRepository.findByComisionBetween(comisionMin, comisionMax);
    }
    
    // Agentes con mínimo número de jugadores
    public List<AgenteEntity> agentesConMinimoJugadores(Integer minJugadores) {
        return agenteRepository.findAgentesConMinimoJugadores(minJugadores);
    }
    
    // Obtener valor total de la cartera de un agente
    public Double obtenerValorTotalCartera(String nombreAgente) {
        Double valor = agenteRepository.getValorTotalCartera(nombreAgente);
        return valor != null ? valor : 0.0;
    }
    
    // Top agentes por número de jugadores
    public List<AgenteEntity> obtenerTopAgentesPorJugadores(Integer limit) {
        return agenteRepository.findTopAgentesByNumeroJugadores(limit);
    }
    
    // Agentes con jugadores en un club específico
    public List<AgenteEntity> agentesConJugadoresEnClub(String nombreClub) {
        return agenteRepository.findAgentesConJugadoresEnClub(nombreClub);
    }
    
    // Actualizar comisión
    public AgenteEntity actualizarComision(String nombre, Double nuevaComision) {
        return agenteRepository.findById(nombre)
            .map(agente -> {
                if (nuevaComision < 0 || nuevaComision > 100) {
                    throw new RuntimeException("La comisión debe estar entre 0 y 100");
                }
                agente.setComision(nuevaComision);
                return agenteRepository.save(agente);
            })
            .orElseThrow(() -> new RuntimeException("Agente no encontrado: " + nombre));
    }
    
    // Calcular comisión por una transferencia
    public Double calcularComisionTransferencia(String nombreAgente, Double montoTransferencia) {
        return agenteRepository.findById(nombreAgente)
            .map(agente -> montoTransferencia * (agente.getComision() / 100))
            .orElseThrow(() -> new RuntimeException("Agente no encontrado: " + nombreAgente));
    }

    // =================================================================
    // ALGORITMOS COMPLEJOS
    // =================================================================

    // /api/agents/assignment - Greedy
    /**
     * Implementación de un algoritmo Greedy para asignar jugadores a agentes
     * según alguna métrica (ej. maximizar comisión o balancear carga).
     */
    public Map<AgenteEntity, List<JugadorEntity>> asignarJugadoresAgentesGreedy() {
    List<JugadorEntity> jugadoresLibres = jugadorRepository.findJugadoresLibres();
    List<AgenteEntity> agentes = agenteRepository.findAll();
    
    if (agentes.isEmpty()) {
        return new HashMap<>();
    }
    
    // Ordenar jugadores por valor de mercado (mayor valor primero)
    jugadoresLibres.sort(Comparator.comparing(
        j -> j.getValorMercado() != null ? j.getValorMercado() : 0.0, 
        Comparator.reverseOrder()
    ));
    
    // Inicializar el mapa de asignación
    Map<AgenteEntity, List<JugadorEntity>> asignacion = new HashMap<>();
    for (AgenteEntity agente : agentes) {
        asignacion.put(agente, new ArrayList<>());
    }
    
    // Algoritmo Greedy: asignar cada jugador al agente con menos carga actual
    for (JugadorEntity jugador : jugadoresLibres) {
        // Encontrar el agente con menor carga (incluyendo asignaciones nuevas)
        AgenteEntity agenteMenosCargado = null;
        int minCarga = Integer.MAX_VALUE;
        
        for (AgenteEntity agente : agentes) {
            // Carga actual = jugadores existentes + jugadores ya asignados en esta ejecución
            int cargaActual = agente.getJugadores().size() + asignacion.get(agente).size();
            
            if (cargaActual < minCarga) {
                minCarga = cargaActual;
                agenteMenosCargado = agente;
            }
        }
        
        // Asignar el jugador al agente menos cargado
        if (agenteMenosCargado != null) {
            asignacion.get(agenteMenosCargado).add(jugador);
        }
    }
    
    return asignacion;
    }
}