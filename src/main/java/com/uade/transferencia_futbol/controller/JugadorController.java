package com.uade.transferencia_futbol.controller;

import com.uade.transferencia_futbol.entity.JugadorEntity;
import com.uade.transferencia_futbol.service.JugadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jugadores")
@CrossOrigin(origins = "*")
public class JugadorController {
    
    @Autowired
    private JugadorService jugadorService;
    
    // ===== CRUD BÁSICO =====
    
    @PostMapping
    public ResponseEntity<?> crearJugador(@RequestBody JugadorEntity jugador) {
        try {
            JugadorEntity nuevoJugador = jugadorService.crearJugador(jugador);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoJugador);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<JugadorEntity>> obtenerTodosJugadores() {
        return ResponseEntity.ok(jugadorService.obtenerTodosJugadores());
    }
    
    @GetMapping("/{nombre}")
    public ResponseEntity<?> obtenerJugadorPorNombre(@PathVariable String nombre) {
        return jugadorService.obtenerJugadorPorNombre(nombre)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{nombre}")
    public ResponseEntity<?> actualizarJugador(
            @PathVariable String nombre,
            @RequestBody JugadorEntity jugador) {
        try {
            JugadorEntity jugadorActualizado = jugadorService.actualizarJugador(nombre, jugador);
            return ResponseEntity.ok(jugadorActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{nombre}")
    public ResponseEntity<?> eliminarJugador(@PathVariable String nombre) {
        try {
            jugadorService.eliminarJugador(nombre);
            return ResponseEntity.ok(Map.of("mensaje", "Jugador eliminado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== ASIGNACIONES =====
    
    @PutMapping("/{nombreJugador}/club/{nombreClub}")
    public ResponseEntity<?> asignarClub(
            @PathVariable String nombreJugador,
            @PathVariable String nombreClub) {
        try {
            JugadorEntity jugador = jugadorService.asignarClub(nombreJugador, nombreClub);
            return ResponseEntity.ok(jugador);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{nombreJugador}/agente/{nombreAgente}")
    public ResponseEntity<?> asignarAgente(
            @PathVariable String nombreJugador,
            @PathVariable String nombreAgente) {
        try {
            JugadorEntity jugador = jugadorService.asignarAgente(nombreJugador, nombreAgente);
            return ResponseEntity.ok(jugador);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== BÚSQUEDAS Y FILTROS =====
    
    @GetMapping("/posicion/{posicion}")
    public ResponseEntity<List<JugadorEntity>> buscarPorPosicion(@PathVariable String posicion) {
        return ResponseEntity.ok(jugadorService.buscarPorPosicion(posicion));
    }
    
    @GetMapping("/nacionalidad/{nacionalidad}")
    public ResponseEntity<List<JugadorEntity>> buscarPorNacionalidad(@PathVariable String nacionalidad) {
        return ResponseEntity.ok(jugadorService.buscarPorNacionalidad(nacionalidad));
    }
    
    @GetMapping("/edad")
    public ResponseEntity<List<JugadorEntity>> buscarPorRangoEdad(
            @RequestParam Integer edadMin,
            @RequestParam Integer edadMax) {
        return ResponseEntity.ok(jugadorService.buscarPorRangoEdad(edadMin, edadMax));
    }
    
    @GetMapping("/valor-minimo/{valor}")
    public ResponseEntity<List<JugadorEntity>> buscarPorValorMinimo(@PathVariable Double valor) {
        return ResponseEntity.ok(jugadorService.buscarPorValorMinimo(valor));
    }
    
    @GetMapping("/club/{nombreClub}")
    public ResponseEntity<List<JugadorEntity>> obtenerJugadoresDeClub(@PathVariable String nombreClub) {
        return ResponseEntity.ok(jugadorService.obtenerJugadoresDeClub(nombreClub));
    }
    
    @GetMapping("/agente/{nombreAgente}")
    public ResponseEntity<List<JugadorEntity>> obtenerJugadoresDeAgente(@PathVariable String nombreAgente) {
        return ResponseEntity.ok(jugadorService.obtenerJugadoresDeAgente(nombreAgente));
    }
    
    @GetMapping("/libres")
    public ResponseEntity<List<JugadorEntity>> obtenerJugadoresLibres() {
        return ResponseEntity.ok(jugadorService.obtenerJugadoresLibres());
    }
    
    @GetMapping("/top/{limit}")
    public ResponseEntity<List<JugadorEntity>> obtenerTopJugadores(@PathVariable Integer limit) {
        return ResponseEntity.ok(jugadorService.obtenerTopJugadores(limit));
    }
    
    // ===== ACTUALIZACIONES ESPECÍFICAS =====
    
    @PatchMapping("/{nombre}/valor-mercado")
    public ResponseEntity<?> actualizarValorMercado(
            @PathVariable String nombre,
            @RequestParam Double nuevoValor) {
        try {
            JugadorEntity jugador = jugadorService.actualizarValorMercado(nombre, nuevoValor);
            return ResponseEntity.ok(jugador);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== ALGORITMOS COMPLEJOS =====
    
    /**
     * BFS - Recorrido de carrera del jugador (Breadth-First Search)
     * Endpoint: GET /api/jugadores/{nombre}/carrera-bfs
     */
    @GetMapping("/{nombre}/carrera-bfs")
    public ResponseEntity<?> obtenerRutaCarreraBFS(@PathVariable String nombre) {
        try {
            Map<String, Object> resultado = jugadorService.obtenerRutaCarreraJugadorBFS(nombre);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * DFS - Recorrido de carrera del jugador (Depth-First Search)
     * Endpoint: GET /api/jugadores/{nombre}/carrera-dfs
     */
    @GetMapping("/{nombre}/carrera-dfs")
    public ResponseEntity<?> obtenerRutaCarreraDFS(@PathVariable String nombre) {
        try {
            Map<String, Object> resultado = jugadorService.obtenerRutaCarreraJugadorDFS(nombre);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * QUICKSORT - Ordenar jugadores por valor de mercado
     * Endpoint: GET /api/jugadores/ordenados-por-valor
     */
    @GetMapping("/ordenados-por-valor")
    public ResponseEntity<?> obtenerJugadoresOrdenados() {
        List<JugadorEntity> jugadores = jugadorService.obtenerJugadoresOrdenadosPorValor();
        return ResponseEntity.ok(Map.of(
            "totalJugadores", jugadores.size(),
            "algoritmo", "QuickSort",
            "jugadores", jugadores
        ));
    }
}