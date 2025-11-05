package com.uade.transferencia_futbol.controller;

import com.uade.transferencia_futbol.entity.LigaEntity;
import com.uade.transferencia_futbol.service.LigaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ligas")
@CrossOrigin(origins = "*")
public class LigaController {
    
    @Autowired
    private LigaService ligaService;
    
    // ===== CRUD BÁSICO =====
    
    @PostMapping
    public ResponseEntity<?> crearLiga(@RequestBody LigaEntity liga) {
        try {
            LigaEntity nuevaLiga = ligaService.crearLiga(liga);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaLiga);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<LigaEntity>> obtenerTodasLigas() {
        return ResponseEntity.ok(ligaService.obtenerTodasLigas());
    }
    
    @GetMapping("/{nombre}")
    public ResponseEntity<?> obtenerLigaPorNombre(@PathVariable String nombre) {
        return ligaService.obtenerLigaPorNombre(nombre)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{nombre}")
    public ResponseEntity<?> actualizarLiga(
            @PathVariable String nombre,
            @RequestBody LigaEntity liga) {
        try {
            LigaEntity ligaActualizada = ligaService.actualizarLiga(nombre, liga);
            return ResponseEntity.ok(ligaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{nombre}")
    public ResponseEntity<?> eliminarLiga(@PathVariable String nombre) {
        try {
            ligaService.eliminarLiga(nombre);
            return ResponseEntity.ok(Map.of("mensaje", "Liga eliminada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== BÚSQUEDAS Y FILTROS =====
    
    @GetMapping("/pais/{pais}")
    public ResponseEntity<List<LigaEntity>> buscarPorPais(@PathVariable String pais) {
        return ResponseEntity.ok(ligaService.buscarPorPais(pais));
    }
    
    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<LigaEntity>> buscarPorNivel(@PathVariable Integer nivel) {
        return ResponseEntity.ok(ligaService.buscarPorNivel(nivel));
    }
    
    @GetMapping("/minimo-equipos/{cantidad}")
    public ResponseEntity<List<LigaEntity>> buscarConMinimoEquipos(@PathVariable Integer cantidad) {
        return ResponseEntity.ok(ligaService.buscarConMinimoEquipos(cantidad));
    }
    
    @GetMapping("/top-nivel/{limit}")
    public ResponseEntity<List<LigaEntity>> obtenerTopLigasPorNivel(@PathVariable Integer limit) {
        return ResponseEntity.ok(ligaService.obtenerTopLigasPorNivel(limit));
    }
    
    @GetMapping("/presupuesto-minimo/{presupuesto}")
    public ResponseEntity<List<LigaEntity>> ligasConPresupuestoMinimo(@PathVariable Double presupuesto) {
        return ResponseEntity.ok(ligaService.ligasConPresupuestoMinimo(presupuesto));
    }
    
    @GetMapping("/{nombre}/contar-jugadores")
    public ResponseEntity<?> contarJugadoresEnLiga(@PathVariable String nombre) {
        Integer total = ligaService.contarJugadoresEnLiga(nombre);
        return ResponseEntity.ok(Map.of(
            "liga", nombre,
            "totalJugadores", total
        ));
    }
    
    @GetMapping("/{nombre}/valor-mercado-total")
    public ResponseEntity<?> obtenerValorMercadoTotalLiga(@PathVariable String nombre) {
        Double valor = ligaService.obtenerValorMercadoTotalLiga(nombre);
        return ResponseEntity.ok(Map.of(
            "liga", nombre,
            "valorMercadoTotal", valor
        ));
    }
    
    @GetMapping("/ordenadas-por-equipos")
    public ResponseEntity<List<LigaEntity>> ligasOrdenadasPorEquipos() {
        return ResponseEntity.ok(ligaService.ligasOrdenadasPorEquipos());
    }
    
    // ===== ACTUALIZACIONES ESPECÍFICAS =====
    
    @PatchMapping("/{nombre}/nivel")
    public ResponseEntity<?> actualizarNivel(
            @PathVariable String nombre,
            @RequestParam Integer nuevoNivel) {
        try {
            LigaEntity liga = ligaService.actualizarNivel(nombre, nuevoNivel);
            return ResponseEntity.ok(liga);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PatchMapping("/{nombre}/cantidad-equipos")
    public ResponseEntity<?> actualizarCantidadEquipos(
            @PathVariable String nombre,
            @RequestParam Integer nuevaCantidad) {
        try {
            LigaEntity liga = ligaService.actualizarCantidadEquipos(nombre, nuevaCantidad);
            return ResponseEntity.ok(liga);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{nombre}/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasLiga(@PathVariable String nombre) {
        try {
            String estadisticas = ligaService.obtenerEstadisticasLiga(nombre);
            return ResponseEntity.ok(Map.of("estadisticas", estadisticas));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
}