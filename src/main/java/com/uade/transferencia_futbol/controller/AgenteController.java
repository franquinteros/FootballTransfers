package com.uade.transferencia_futbol.controller;

import com.uade.transferencia_futbol.entity.AgenteEntity;
import com.uade.transferencia_futbol.entity.JugadorEntity;
import com.uade.transferencia_futbol.service.AgenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agentes")
@CrossOrigin(origins = "*")
public class AgenteController {
    
    @Autowired
    private AgenteService agenteService;
    
    // ===== CRUD BÁSICO =====
    
    @PostMapping
    public ResponseEntity<?> crearAgente(@RequestBody AgenteEntity agente) {
        try {
            AgenteEntity nuevoAgente = agenteService.crearAgente(agente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAgente);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<AgenteEntity>> obtenerTodosAgentes() {
        return ResponseEntity.ok(agenteService.obtenerTodosAgentes());
    }
    
    @GetMapping("/{nombre}")
    public ResponseEntity<?> obtenerAgentePorNombre(@PathVariable String nombre) {
        return agenteService.obtenerAgentePorNombre(nombre)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{nombre}")
    public ResponseEntity<?> actualizarAgente(
            @PathVariable String nombre,
            @RequestBody AgenteEntity agente) {
        try {
            AgenteEntity agenteActualizado = agenteService.actualizarAgente(nombre, agente);
            return ResponseEntity.ok(agenteActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{nombre}")
    public ResponseEntity<?> eliminarAgente(@PathVariable String nombre) {
        try {
            agenteService.eliminarAgente(nombre);
            return ResponseEntity.ok(Map.of("mensaje", "Agente eliminado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== BÚSQUEDAS Y FILTROS =====
    
    @GetMapping("/agencia/{agencia}")
    public ResponseEntity<List<AgenteEntity>> buscarPorAgencia(@PathVariable String agencia) {
        return ResponseEntity.ok(agenteService.buscarPorAgencia(agencia));
    }
    
    @GetMapping("/nacionalidad/{nacionalidad}")
    public ResponseEntity<List<AgenteEntity>> buscarPorNacionalidad(@PathVariable String nacionalidad) {
        return ResponseEntity.ok(agenteService.buscarPorNacionalidad(nacionalidad));
    }
    
    @GetMapping("/comision-minima/{comision}")
    public ResponseEntity<List<AgenteEntity>> buscarPorComisionMinima(@PathVariable Double comision) {
        return ResponseEntity.ok(agenteService.buscarPorComisionMinima(comision));
    }
    
    @GetMapping("/comision-rango")
    public ResponseEntity<List<AgenteEntity>> buscarPorRangoComision(
            @RequestParam Double comisionMin,
            @RequestParam Double comisionMax) {
        return ResponseEntity.ok(agenteService.buscarPorRangoComision(comisionMin, comisionMax));
    }
    
    @GetMapping("/minimo-jugadores/{cantidad}")
    public ResponseEntity<List<AgenteEntity>> agentesConMinimoJugadores(@PathVariable Integer cantidad) {
        return ResponseEntity.ok(agenteService.agentesConMinimoJugadores(cantidad));
    }
    
    @GetMapping("/{nombre}/valor-cartera")
    public ResponseEntity<?> obtenerValorTotalCartera(@PathVariable String nombre) {
        Double valor = agenteService.obtenerValorTotalCartera(nombre);
        return ResponseEntity.ok(Map.of(
            "agente", nombre,
            "valorTotalCartera", valor
        ));
    }
    
    @GetMapping("/top-jugadores/{limit}")
    public ResponseEntity<List<AgenteEntity>> obtenerTopAgentesPorJugadores(@PathVariable Integer limit) {
        return ResponseEntity.ok(agenteService.obtenerTopAgentesPorJugadores(limit));
    }
    
    @GetMapping("/club/{nombreClub}")
    public ResponseEntity<List<AgenteEntity>> agentesConJugadoresEnClub(@PathVariable String nombreClub) {
        return ResponseEntity.ok(agenteService.agentesConJugadoresEnClub(nombreClub));
    }
    
    // ===== ACTUALIZACIONES ESPECÍFICAS =====
    
    @PatchMapping("/{nombre}/comision")
    public ResponseEntity<?> actualizarComision(
            @PathVariable String nombre,
            @RequestParam Double nuevaComision) {
        try {
            AgenteEntity agente = agenteService.actualizarComision(nombre, nuevaComision);
            return ResponseEntity.ok(agente);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{nombre}/calcular-comision")
    public ResponseEntity<?> calcularComisionTransferencia(
            @PathVariable String nombre,
            @RequestParam Double montoTransferencia) {
        try {
            Double comision = agenteService.calcularComisionTransferencia(nombre, montoTransferencia);
            return ResponseEntity.ok(Map.of(
                "agente", nombre,
                "montoTransferencia", montoTransferencia,
                "comisionCalculada", comision
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== ALGORITMOS COMPLEJOS =====
    
    /**
     * GREEDY - Asignación óptima de jugadores a agentes
     * Endpoint: GET /api/agentes/asignacion-greedy
     */
    @GetMapping("/asignacion-greedy")
    public ResponseEntity<?> asignarJugadoresAgentesGreedy() {
        Map<AgenteEntity, List<JugadorEntity>> asignacion = agenteService.asignarJugadoresAgentesGreedy();
        
        // Formatear respuesta
        Map<String, Object> respuesta = Map.of(
            "algoritmo", "Greedy",
            "descripcion", "Asignación de jugadores libres a agentes balanceando la carga",
            "totalAgentes", asignacion.size(),
            "asignaciones", asignacion.entrySet().stream()
                .map(entry -> Map.of(
                    "agente", entry.getKey().getNombre(),
                    "jugadoresAsignados", entry.getValue().size(),
                    "jugadores", entry.getValue().stream()
                        .map(JugadorEntity::getNombre)
                        .toList()
                ))
                .toList()
        );
        
        return ResponseEntity.ok(respuesta);
    }
}