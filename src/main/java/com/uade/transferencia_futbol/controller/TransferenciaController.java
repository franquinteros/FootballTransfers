package com.uade.transferencia_futbol.controller;

import com.uade.transferencia_futbol.entity.TransferenciaEntity;
import com.uade.transferencia_futbol.service.TransferenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/transferencias")
@CrossOrigin(origins = "*")
public class TransferenciaController {
    
    @Autowired
    private TransferenciaService transferenciaService;
    
    // ===== CRUD BÁSICO =====
    
    @PostMapping
    public ResponseEntity<?> crearTransferencia(@RequestBody TransferenciaEntity transferencia) {
        try {
            TransferenciaEntity nuevaTransferencia = transferenciaService.crearTransferencia(transferencia);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTransferencia);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/realizar")
    public ResponseEntity<?> realizarTransferencia(
            @RequestParam String nombreJugador,
            @RequestParam String nombreClubDestino,
            @RequestParam Double monto,
            @RequestParam String temporada,
            @RequestParam String tipoTransferencia) {
        try {
            TransferenciaEntity transferencia = transferenciaService.realizarTransferencia(
                nombreJugador, nombreClubDestino, monto, temporada, tipoTransferencia
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transferencia);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<TransferenciaEntity>> obtenerTodasTransferencias() {
        return ResponseEntity.ok(transferenciaService.obtenerTodasTransferencias());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerTransferenciaPorId(@PathVariable Long id) {
        return transferenciaService.obtenerTransferenciaPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarTransferencia(@PathVariable Long id) {
        try {
            transferenciaService.eliminarTransferencia(id);
            return ResponseEntity.ok(Map.of("mensaje", "Transferencia eliminada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== BÚSQUEDAS Y FILTROS =====
    
    @GetMapping("/temporada/{temporada}")
    public ResponseEntity<List<TransferenciaEntity>> buscarPorTemporada(@PathVariable String temporada) {
        return ResponseEntity.ok(transferenciaService.buscarPorTemporada(temporada));
    }
    
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<TransferenciaEntity>> buscarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(transferenciaService.buscarPorTipo(tipo));
    }
    
    @GetMapping("/monto-minimo/{monto}")
    public ResponseEntity<List<TransferenciaEntity>> buscarPorMontoMinimo(@PathVariable Double monto) {
        return ResponseEntity.ok(transferenciaService.buscarPorMontoMinimo(monto));
    }
    
    @GetMapping("/fechas")
    public ResponseEntity<List<TransferenciaEntity>> buscarEntreFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(transferenciaService.buscarEntreFechas(fechaInicio, fechaFin));
    }
    
    @GetMapping("/jugador/{nombreJugador}")
    public ResponseEntity<List<TransferenciaEntity>> obtenerTransferenciasDeJugador(@PathVariable String nombreJugador) {
        return ResponseEntity.ok(transferenciaService.obtenerTransferenciasDeJugador(nombreJugador));
    }
    
    @GetMapping("/club-destino/{nombreClub}")
    public ResponseEntity<List<TransferenciaEntity>> obtenerTransferenciasHaciaClub(@PathVariable String nombreClub) {
        return ResponseEntity.ok(transferenciaService.obtenerTransferenciasHaciaClub(nombreClub));
    }
    
    @GetMapping("/club-origen/{nombreClub}")
    public ResponseEntity<List<TransferenciaEntity>> obtenerTransferenciasDesdeClub(@PathVariable String nombreClub) {
        return ResponseEntity.ok(transferenciaService.obtenerTransferenciasDesdeClub(nombreClub));
    }
    
    @GetMapping("/top-monto/{limit}")
    public ResponseEntity<List<TransferenciaEntity>> obtenerTopTransferenciasPorMonto(@PathVariable Integer limit) {
        return ResponseEntity.ok(transferenciaService.obtenerTopTransferenciasPorMonto(limit));
    }
    
    @GetMapping("/temporada-tipo")
    public ResponseEntity<List<TransferenciaEntity>> buscarPorTemporadaYTipo(
            @RequestParam String temporada,
            @RequestParam String tipo) {
        return ResponseEntity.ok(transferenciaService.buscarPorTemporadaYTipo(temporada, tipo));
    }
    
    // ===== ESTADÍSTICAS =====
    
    @GetMapping("/club/{nombreClub}/total-invertido")
    public ResponseEntity<?> obtenerTotalInvertidoPorClub(@PathVariable String nombreClub) {
        Double total = transferenciaService.obtenerTotalInvertidoPorClub(nombreClub);
        return ResponseEntity.ok(Map.of(
            "club", nombreClub,
            "totalInvertido", total
        ));
    }
    
    @GetMapping("/temporada-actual/{temporada}")
    public ResponseEntity<List<TransferenciaEntity>> obtenerTransferenciasTemporadaActual(@PathVariable String temporada) {
        return ResponseEntity.ok(transferenciaService.obtenerTransferenciasTemporadaActual(temporada));
    }
    
    @GetMapping("/puede-realizar")
    public ResponseEntity<?> puedeRealizarTransferencia(
            @RequestParam String nombreClub,
            @RequestParam Double monto) {
        boolean puede = transferenciaService.puedeRealizarTransferencia(nombreClub, monto);
        return ResponseEntity.ok(Map.of(
            "club", nombreClub,
            "monto", monto,
            "puedeRealizarTransferencia", puede
        ));
    }
    
    @GetMapping("/estadisticas/{temporada}")
    public ResponseEntity<?> obtenerEstadisticasTemporada(@PathVariable String temporada) {
        String estadisticas = transferenciaService.obtenerEstadisticasTemporada(temporada);
        return ResponseEntity.ok(Map.of("estadisticas", estadisticas));
    }
    
    // ===== ALGORITMOS COMPLEJOS =====
    
    /**
     * DIJKSTRA - Ruta de transferencia más barata
     */
    @GetMapping("/ruta-mas-barata")
    public ResponseEntity<?> obtenerRutaTransferenciaMasBarata(
            @RequestParam String clubOrigen,
            @RequestParam String clubDestino) {
        try {
            List<String> ruta = transferenciaService.obtenerRutaTransferenciaMasBarata(clubOrigen, clubDestino);
            return ResponseEntity.ok(Map.of(
                "algoritmo", "Dijkstra",
                "clubOrigen", clubOrigen,
                "clubDestino", clubDestino,
                "ruta", ruta,
                "numeroSaltos", ruta.size() - 1
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * PROGRAMACIÓN DINÁMICA - Optimización de presupuesto
     */
    @GetMapping("/optimizar-presupuesto")
    public ResponseEntity<?> optimizarPresupuesto(
            @RequestParam String nombreClub,
            @RequestParam Double presupuestoMaximo) {
        try {
            Map<String, Object> resultado = transferenciaService.optimizarPresupuestoTransferencias(
                nombreClub, presupuestoMaximo);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * PROGRAMACIÓN DINÁMICA - Optimización balanceada
     */
    @GetMapping("/optimizar-presupuesto-balanceado")
    public ResponseEntity<?> optimizarPresupuestoBalanceado(
            @RequestParam String nombreClub,
            @RequestParam Double presupuestoMaximo,
            @RequestParam(defaultValue = "1") Integer arqueros,
            @RequestParam(defaultValue = "4") Integer defensas,
            @RequestParam(defaultValue = "4") Integer mediocampistas,
            @RequestParam(defaultValue = "2") Integer delanteros) {
        try {
            Map<String, Integer> posicionesRequeridas = new HashMap<>();
            posicionesRequeridas.put("Arquero", arqueros);
            posicionesRequeridas.put("Defensa", defensas);
            posicionesRequeridas.put("Mediocampo", mediocampistas);
            posicionesRequeridas.put("Delantero", delanteros);
            
            Map<String, Object> resultado = transferenciaService.optimizarPresupuestoBalanceado(
                nombreClub, presupuestoMaximo, posicionesRequeridas);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * PRIM - Red mínima de transferencias
     */
    @GetMapping("/red-minima")
    public ResponseEntity<?> calcularRedTransferenciaMinima() {
        try {
            Double costo = transferenciaService.calcularCostoRedTransferenciaMinima();
            return ResponseEntity.ok(Map.of(
                "algoritmo", "Prim",
                "costoTotalRedMinima", costo
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * BRANCH & BOUND - Mejores ofertas
     */
    @GetMapping("/mejores-ofertas")
    public ResponseEntity<?> buscarMejoresOfertas(
            @RequestParam String clubOrigen,
            @RequestParam Double presupuestoMaximo) {
        try {
            List<TransferenciaEntity> ofertas = transferenciaService.buscarMejoresOfertas(clubOrigen, presupuestoMaximo);
            return ResponseEntity.ok(Map.of(
                "algoritmo", "Branch & Bound",
                "clubOrigen", clubOrigen,
                "presupuestoMaximo", presupuestoMaximo,
                "ofertas", ofertas,
                "totalOfertas", ofertas.size()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}