package com.uade.transferencia_futbol.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class HomeController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("aplicacion", "Sistema de Transferencias de Fútbol");
        response.put("version", "1.0.0");
        response.put("estado", "Activo");
        response.put("descripcion", "API REST para gestión de transferencias de fútbol con Neo4j");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("jugadores", "/api/jugadores");
        endpoints.put("clubes", "/api/clubes");
        endpoints.put("agentes", "/api/agentes");
        endpoints.put("ligas", "/api/ligas");
        endpoints.put("transferencias", "/api/transferencias");
        endpoints.put("health", "/api/health");
        
        Map<String, String> algoritmosEndpoints = new HashMap<>();
        algoritmosEndpoints.put("Dijkstra - Ruta más barata", "GET /api/transferencias/ruta-mas-barata?clubOrigen={club1}&clubDestino={club2}");
        algoritmosEndpoints.put("BFS - Carrera jugador", "GET /api/jugadores/{nombre}/carrera-bfs");
        algoritmosEndpoints.put("DFS - Carrera jugador", "GET /api/jugadores/{nombre}/carrera-dfs");
        algoritmosEndpoints.put("Backtracking - Formación óptima", "GET /api/clubes/{club}/formacion-optima?formacion=4-3-3");
        algoritmosEndpoints.put("Prog. Dinámica - Optimizar presupuesto", "GET /api/transferencias/optimizar-presupuesto?nombreClub={club}&presupuestoMaximo={monto}");
        algoritmosEndpoints.put("Prim - Red mínima", "GET /api/transferencias/red-minima-prim");
        algoritmosEndpoints.put("Kruskal - Red mínima", "GET /api/transferencias/red-minima-kruskal");
        algoritmosEndpoints.put("QuickSort - Ordenar jugadores", "GET /api/jugadores/ordenados-por-valor");
        algoritmosEndpoints.put("Greedy - Asignar jugadores", "GET /api/agentes/asignacion-greedy");
        algoritmosEndpoints.put("Branch & Bound - Mejores ofertas", "GET /api/transferencias/mejores-ofertas?clubOrigen={club}&presupuestoMaximo={monto}");
        
        response.put("endpoints", endpoints);
        response.put("algoritmos", algoritmosEndpoints);
        response.put("documentacion", "https://github.com/tu-repo/transferencia-futbol");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "Transferencias Fútbol API");
        health.put("database", "Neo4j - Connected");
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("nombre", "Transferencias Fútbol API");
        info.put("version", "1.0.0");
        info.put("descripcion", "API REST para gestión de transferencias de fútbol");
        
        Map<String, Object> recursos = new HashMap<>();
        recursos.put("Jugadores", Map.of(
            "endpoint", "/api/jugadores",
            "metodos", "GET, POST, PUT, DELETE",
            "descripcion", "Gestión de jugadores de fútbol"
        ));
        recursos.put("Clubes", Map.of(
            "endpoint", "/api/clubes",
            "metodos", "GET, POST, PUT, DELETE",
            "descripcion", "Gestión de clubes de fútbol"
        ));
        recursos.put("Agentes", Map.of(
            "endpoint", "/api/agentes",
            "metodos", "GET, POST, PUT, DELETE",
            "descripcion", "Gestión de agentes de jugadores"
        ));
        recursos.put("Ligas", Map.of(
            "endpoint", "/api/ligas",
            "metodos", "GET, POST, PUT, DELETE",
            "descripcion", "Gestión de ligas de fútbol"
        ));
        recursos.put("Transferencias", Map.of(
            "endpoint", "/api/transferencias",
            "metodos", "GET, POST, DELETE",
            "descripcion", "Gestión de transferencias de jugadores"
        ));
        
        info.put("recursos", recursos);
        info.put("baseUrl", "/api");
        
        return ResponseEntity.ok(info);
    }
}
