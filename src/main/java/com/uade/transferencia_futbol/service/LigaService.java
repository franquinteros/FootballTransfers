package com.uade.transferencia_futbol.service;

import com.uade.transferencia_futbol.entity.LigaEntity;
import com.uade.transferencia_futbol.repository.LigaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LigaService {
    
    @Autowired
    private LigaRepository ligaRepository;
    
    // Crear liga
    public LigaEntity crearLiga(LigaEntity liga) {
        if (ligaRepository.existsById(liga.getNombre())) {
            throw new RuntimeException("La liga ya existe: " + liga.getNombre());
        }
        return ligaRepository.save(liga);
    }
    
    // Obtener todas las ligas
    public List<LigaEntity> obtenerTodasLigas() {
        return ligaRepository.findAll();
    }
    
    // Obtener liga por nombre
    public Optional<LigaEntity> obtenerLigaPorNombre(String nombre) {
        return ligaRepository.findById(nombre);
    }
    
    // Actualizar liga
    public LigaEntity actualizarLiga(String nombre, LigaEntity ligaActualizada) {
        return ligaRepository.findById(nombre)
            .map(liga -> {
                liga.setPais(ligaActualizada.getPais());
                liga.setNivel(ligaActualizada.getNivel());
                liga.setCantidadEquipos(ligaActualizada.getCantidadEquipos());
                return ligaRepository.save(liga);
            })
            .orElseThrow(() -> new RuntimeException("Liga no encontrada: " + nombre));
    }
    
    // Eliminar liga
    public void eliminarLiga(String nombre) {
        if (!ligaRepository.existsById(nombre)) {
            throw new RuntimeException("Liga no encontrada: " + nombre);
        }
        ligaRepository.deleteById(nombre);
    }
    
    // Buscar ligas por país
    public List<LigaEntity> buscarPorPais(String pais) {
        return ligaRepository.findByPais(pais);
    }
    
    // Buscar ligas por nivel
    public List<LigaEntity> buscarPorNivel(Integer nivel) {
        return ligaRepository.findByNivel(nivel);
    }
    
    // Buscar ligas con más de N equipos
    public List<LigaEntity> buscarConMinimoEquipos(Integer cantidad) {
        return ligaRepository.findByCantidadEquiposGreaterThan(cantidad);
    }
    
    // Top ligas por nivel (mejores ligas)
    public List<LigaEntity> obtenerTopLigasPorNivel(Integer limit) {
        return ligaRepository.findTopLigasByNivel(limit);
    }
    
    // Ligas con presupuesto total mínimo
    public List<LigaEntity> ligasConPresupuestoMinimo(Double presupuestoMinimo) {
        return ligaRepository.findLigasConPresupuestoMinimo(presupuestoMinimo);
    }
    
    // Contar jugadores en una liga
    public Integer contarJugadoresEnLiga(String nombreLiga) {
        Integer total = ligaRepository.countJugadoresEnLiga(nombreLiga);
        return total != null ? total : 0;
    }
    
    // Obtener valor de mercado total de una liga
    public Double obtenerValorMercadoTotalLiga(String nombreLiga) {
        Double valor = ligaRepository.getValorMercadoTotalLiga(nombreLiga);
        return valor != null ? valor : 0.0;
    }
    
    // Ligas ordenadas por cantidad de equipos
    public List<LigaEntity> ligasOrdenadasPorEquipos() {
        return ligaRepository.findAllOrderByCantidadEquipos();
    }
    
    // Actualizar nivel de liga
    public LigaEntity actualizarNivel(String nombre, Integer nuevoNivel) {
        return ligaRepository.findById(nombre)
            .map(liga -> {
                if (nuevoNivel < 1 || nuevoNivel > 10) {
                    throw new RuntimeException("El nivel debe estar entre 1 y 10");
                }
                liga.setNivel(nuevoNivel);
                return ligaRepository.save(liga);
            })
            .orElseThrow(() -> new RuntimeException("Liga no encontrada: " + nombre));
    }
    
    // Actualizar cantidad de equipos
    public LigaEntity actualizarCantidadEquipos(String nombre, Integer nuevaCantidad) {
        return ligaRepository.findById(nombre)
            .map(liga -> {
                liga.setCantidadEquipos(nuevaCantidad);
                return ligaRepository.save(liga);
            })
            .orElseThrow(() -> new RuntimeException("Liga no encontrada: " + nombre));
    }
    
    // Obtener estadísticas de una liga
    public String obtenerEstadisticasLiga(String nombreLiga) {
        LigaEntity liga = ligaRepository.findById(nombreLiga)
            .orElseThrow(() -> new RuntimeException("Liga no encontrada: " + nombreLiga));
        
        Integer totalJugadores = contarJugadoresEnLiga(nombreLiga);
        Double valorTotal = obtenerValorMercadoTotalLiga(nombreLiga);
        
        return String.format(
            "Liga: %s\nPaís: %s\nNivel: %d\nEquipos: %d\nJugadores totales: %d\nValor total de mercado: %.2f M€",
            liga.getNombre(), liga.getPais(), liga.getNivel(), liga.getCantidadEquipos(), totalJugadores, valorTotal
        );
    }
}
