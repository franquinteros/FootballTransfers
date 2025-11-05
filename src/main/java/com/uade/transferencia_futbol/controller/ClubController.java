package com.uade.transferencia_futbol.controller;

import com.uade.transferencia_futbol.entity.ClubEntity;
import com.uade.transferencia_futbol.entity.JugadorEntity;
import com.uade.transferencia_futbol.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubes")
@CrossOrigin(origins = "*")
public class ClubController {
    
    @Autowired
    private ClubService clubService;
    
    // ===== CRUD BÁSICO =====
    
    @PostMapping
    public ResponseEntity<?> crearClub(@RequestBody ClubEntity club) {
        try {
            ClubEntity nuevoClub = clubService.crearClub(club);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoClub);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ClubEntity>> obtenerTodosClubes() {
        return ResponseEntity.ok(clubService.obtenerTodosClubes());
    }
    
    @GetMapping("/{nombre}")
    public ResponseEntity<?> obtenerClubPorNombre(@PathVariable String nombre) {
        return clubService.obtenerClubPorNombre(nombre)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{nombre}")
    public ResponseEntity<?> actualizarClub(
            @PathVariable String nombre,
            @RequestBody ClubEntity club) {
        try {
            ClubEntity clubActualizado = clubService.actualizarClub(nombre, club);
            return ResponseEntity.ok(clubActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{nombre}")
    public ResponseEntity<?> eliminarClub(@PathVariable String nombre) {
        try {
            clubService.eliminarClub(nombre);
            return ResponseEntity.ok(Map.of("mensaje", "Club eliminado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== ASIGNACIONES =====
    
    @PutMapping("/{nombreClub}/liga/{nombreLiga}")
    public ResponseEntity<?> asignarLiga(
            @PathVariable String nombreClub,
            @PathVariable String nombreLiga) {
        try {
            ClubEntity club = clubService.asignarLiga(nombreClub, nombreLiga);
            return ResponseEntity.ok(club);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== BÚSQUEDAS Y FILTROS =====
    
    @GetMapping("/pais/{pais}")
    public ResponseEntity<List<ClubEntity>> buscarPorPais(@PathVariable String pais) {
        return ResponseEntity.ok(clubService.buscarPorPais(pais));
    }
    
    @GetMapping("/liga/{liga}")
    public ResponseEntity<List<ClubEntity>> buscarPorLiga(@PathVariable String liga) {
        return ResponseEntity.ok(clubService.buscarPorLiga(liga));
    }
    
    @GetMapping("/presupuesto-minimo/{presupuesto}")
    public ResponseEntity<List<ClubEntity>> buscarPorPresupuestoMinimo(@PathVariable Double presupuesto) {
        return ResponseEntity.ok(clubService.buscarPorPresupuestoMinimo(presupuesto));
    }
    
    @GetMapping("/fundados-despues/{anio}")
    public ResponseEntity<List<ClubEntity>> buscarFundadosDespuesDe(@PathVariable Integer anio) {
        return ResponseEntity.ok(clubService.buscarFundadosDespuesDe(anio));
    }
    
    @GetMapping("/liga-detalle/{nombreLiga}")
    public ResponseEntity<List<ClubEntity>> obtenerClubesDeUnaLiga(@PathVariable String nombreLiga) {
        return ResponseEntity.ok(clubService.obtenerClubesDeUnaLiga(nombreLiga));
    }
    
    @GetMapping("/pueden-pagar/{valor}")
    public ResponseEntity<List<ClubEntity>> clubesQuePuedenPagar(@PathVariable Double valor) {
        return ResponseEntity.ok(clubService.clubesQuePuedenPagar(valor));
    }
    
    @GetMapping("/nacionalidad-jugadores/{nacionalidad}")
    public ResponseEntity<List<ClubEntity>> clubesConJugadoresDeNacionalidad(@PathVariable String nacionalidad) {
        return ResponseEntity.ok(clubService.clubesConJugadoresDeNacionalidad(nacionalidad));
    }
    
    @GetMapping("/top-presupuesto/{limit}")
    public ResponseEntity<List<ClubEntity>> obtenerTopClubesPorPresupuesto(@PathVariable Integer limit) {
        return ResponseEntity.ok(clubService.obtenerTopClubesPorPresupuesto(limit));
    }
    
    // ===== ACTUALIZACIONES DE PRESUPUESTO =====
    
    @PatchMapping("/{nombre}/presupuesto")
    public ResponseEntity<?> actualizarPresupuesto(
            @PathVariable String nombre,
            @RequestParam Double nuevoPresupuesto) {
        try {
            ClubEntity club = clubService.actualizarPresupuesto(nombre, nuevoPresupuesto);
            return ResponseEntity.ok(club);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PatchMapping("/{nombre}/reducir-presupuesto")
    public ResponseEntity<?> reducirPresupuesto(
            @PathVariable String nombre,
            @RequestParam Double monto) {
        try {
            ClubEntity club = clubService.reducirPresupuesto(nombre, monto);
            return ResponseEntity.ok(club);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PatchMapping("/{nombre}/aumentar-presupuesto")
    public ResponseEntity<?> aumentarPresupuesto(
            @PathVariable String nombre,
            @RequestParam Double monto) {
        try {
            ClubEntity club = clubService.aumentarPresupuesto(nombre, monto);
            return ResponseEntity.ok(club);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== ALGORITMOS COMPLEJOS =====
    
    /**
     * BACKTRACKING - Formación óptima
     * Endpoint: GET /api/clubes/{nombre}/formacion-optima
     */
    @GetMapping("/{nombreClub}/formacion-optima")
    public ResponseEntity<?> obtenerEscuadraOptima(
            @PathVariable String nombreClub,
            @RequestParam(defaultValue = "4-3-3") String formacion) {
        try {
            List<JugadorEntity> escuadra = clubService.obtenerEscuadraOptima(nombreClub, formacion);
            
            double valorTotal = escuadra.stream()
                .mapToDouble(j -> j.getValorMercado() != null ? j.getValorMercado() : 0.0)
                .sum();
            
            return ResponseEntity.ok(Map.of(
                "club", nombreClub,
                "formacion", formacion,
                "algoritmo", "Backtracking",
                "jugadores", escuadra,
                "totalJugadores", escuadra.size(),
                "valorTotalMercado", valorTotal
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
}