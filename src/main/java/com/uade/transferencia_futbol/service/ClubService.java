package com.uade.transferencia_futbol.service;

import com.uade.transferencia_futbol.entity.ClubEntity;
import com.uade.transferencia_futbol.entity.LigaEntity;
import com.uade.transferencia_futbol.entity.JugadorEntity;
import com.uade.transferencia_futbol.repository.ClubRepository;
import com.uade.transferencia_futbol.repository.LigaRepository;
import com.uade.transferencia_futbol.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ClubService {
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private LigaRepository ligaRepository;
    
    @Autowired
    private JugadorRepository jugadorRepository;

    // ==================== CRUD ====================

    public ClubEntity crearClub(ClubEntity club) {
        if (clubRepository.existsById(club.getNombre())) {
            throw new RuntimeException("El club ya existe: " + club.getNombre());
        }
        return clubRepository.save(club);
    }

    public List<ClubEntity> obtenerTodosClubes() {
        return clubRepository.findAll();
    }

    public Optional<ClubEntity> obtenerClubPorNombre(String nombre) {
        return clubRepository.findById(nombre);
    }

    public ClubEntity actualizarClub(String nombre, ClubEntity clubActualizado) {
        return clubRepository.findById(nombre)
            .map(club -> {
                club.setPais(clubActualizado.getPais());
                club.setPresupuesto(clubActualizado.getPresupuesto());
                club.setLiga(clubActualizado.getLiga());
                club.setFundacion(clubActualizado.getFundacion());
                return clubRepository.save(club);
            })
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombre));
    }

    public void eliminarClub(String nombre) {
        if (!clubRepository.existsById(nombre)) {
            throw new RuntimeException("Club no encontrado: " + nombre);
        }
        clubRepository.deleteById(nombre);
    }

    public ClubEntity asignarLiga(String nombreClub, String nombreLiga) {
        ClubEntity club = clubRepository.findById(nombreClub)
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClub));
        
        LigaEntity liga = ligaRepository.findById(nombreLiga)
            .orElseThrow(() -> new RuntimeException("Liga no encontrada: " + nombreLiga));
        
        club.setLigaEntity(liga);
        club.setLiga(nombreLiga);
        return clubRepository.save(club);
    }

    // ==================== MÉTODOS ADICIONALES ====================

    public List<ClubEntity> buscarPorPais(String pais) {
        return clubRepository.findByPais(pais);
    }
    
    public List<ClubEntity> buscarPorLiga(String liga) {
        return clubRepository.findByLiga(liga);
    }
    
    public List<ClubEntity> buscarPorPresupuestoMinimo(Double presupuestoMinimo) {
        return clubRepository.findByPresupuestoGreaterThan(presupuestoMinimo);
    }
    
    public List<ClubEntity> buscarFundadosDespuesDe(Integer anio) {
        return clubRepository.findByFundacionGreaterThan(anio);
    }
    
    public List<ClubEntity> obtenerClubesDeUnaLiga(String nombreLiga) {
        return clubRepository.findClubesByLiga(nombreLiga);
    }
    
    public List<ClubEntity> clubesQuePuedenPagar(Double valorJugador) {
        return clubRepository.findClubesQuePuedenPagar(valorJugador);
    }
    
    public List<ClubEntity> clubesConJugadoresDeNacionalidad(String nacionalidad) {
        return clubRepository.findClubesConJugadoresDeNacionalidad(nacionalidad);
    }
    
    public List<ClubEntity> obtenerTopClubesPorPresupuesto(Integer limit) {
        return clubRepository.findTopClubesByPresupuesto(limit);
    }
    
    public List<ClubEntity> obtenerTopClubesPorNumeroJugadores(Integer limit) {
        return clubRepository.findTopClubesByNumeroJugadores(limit);
    }
    
    public List<ClubEntity> buscarPorRangoPresupuesto(Double presupuestoMin, Double presupuestoMax) {
        return clubRepository.findByPresupuestoBetween(presupuestoMin, presupuestoMax);
    }
    
    public List<ClubEntity> buscarClubesEnLigasTopNivel(Integer nivelMinimo) {
        return clubRepository.findClubesEnLigasTopNivel(nivelMinimo);
    }
    
    public List<ClubEntity> obtenerClubesOrdenadosPorFundacion() {
        return clubRepository.findAllOrderByFundacionAsc();
    }
    
    public Double obtenerValorTotalPlantilla(String nombreClub) {
        Double valor = clubRepository.getValorTotalPlantilla(nombreClub);
        return valor != null ? valor : 0.0;
    }
    
    public ClubEntity actualizarPresupuesto(String nombre, Double nuevoPresupuesto) {
        return clubRepository.findById(nombre)
            .map(club -> {
                club.setPresupuesto(nuevoPresupuesto);
                return clubRepository.save(club);
            })
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombre));
    }
    
    public ClubEntity reducirPresupuesto(String nombre, Double monto) {
        return clubRepository.findById(nombre)
            .map(club -> {
                if (club.getPresupuesto() < monto) {
                    throw new RuntimeException("Presupuesto insuficiente para el club: " + nombre);
                }
                club.setPresupuesto(club.getPresupuesto() - monto);
                return clubRepository.save(club);
            })
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombre));
    }
    
    public ClubEntity aumentarPresupuesto(String nombre, Double monto) {
        return clubRepository.findById(nombre)
            .map(club -> {
                club.setPresupuesto(club.getPresupuesto() + monto);
                return clubRepository.save(club);
            })
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombre));
    }

    // =================================================================
    // ALGORITMO BACKTRACKING: ESCUADRA ÓPTIMA
    // =================================================================

    /**
     * Backtracking para encontrar la mejor alineación según formación
     */
    public Map<String, Object> obtenerEscuadraOptima(String nombreClub, String formacion) {
        ClubEntity club = clubRepository.findById(nombreClub)
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClub));
        
        List<JugadorEntity> jugadoresClub = jugadorRepository.findJugadoresByClub(nombreClub);
        
        if (jugadoresClub.isEmpty()) {
            throw new RuntimeException("El club no tiene jugadores disponibles");
        }
        
        Map<String, Integer> formacionRequerida = parseFormacion(formacion);
        Map<String, List<JugadorEntity>> jugadoresPorCategoria = agruparPorCategoria(jugadoresClub);
        
        List<JugadorEntity> mejorEscuadra = new ArrayList<>();
        List<JugadorEntity> escuadraActual = new ArrayList<>();
        double[] mejorValor = {0.0};
        
        if (!esPosibleFormarEquipo(jugadoresPorCategoria, formacionRequerida)) {
            throw new RuntimeException("No hay suficientes jugadores para la formación " + formacion);
        }
        
        backtrackEscuadra(
            jugadoresPorCategoria, 
            formacionRequerida, 
            escuadraActual, 
            mejorEscuadra, 
            mejorValor,
            new HashMap<>()
        );
        
        // Preparar respuesta completa
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("club", club.getNombre());
        resultado.put("formacion", formacion);
        resultado.put("escuadra", mejorEscuadra);
        resultado.put("valorTotal", mejorValor[0]);
        resultado.put("cantidadJugadores", mejorEscuadra.size());
        resultado.put("desglose", obtenerDesglosePorPosicion(mejorEscuadra));
        resultado.put("algoritmo", "Backtracking");
        
        return resultado;
    }

    /**
     * Backtracking con restricción de presupuesto
     */
    public Map<String, Object> obtenerEscuadraOptimaConPresupuesto(String nombreClub, String formacion, Double presupuestoMaximo) {
        ClubEntity club = clubRepository.findById(nombreClub)
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClub));
        
        // Combinar jugadores del club + jugadores libres que se pueden comprar
        List<JugadorEntity> jugadoresClub = jugadorRepository.findJugadoresByClub(nombreClub);
        List<JugadorEntity> jugadoresLibres = jugadorRepository.findJugadoresLibres();
        
        // Filtrar jugadores libres por presupuesto
        List<JugadorEntity> jugadoresDisponibles = new ArrayList<>(jugadoresClub);
        for (JugadorEntity libre : jugadoresLibres) {
            if (libre.getValorMercado() != null && libre.getValorMercado() <= presupuestoMaximo) {
                jugadoresDisponibles.add(libre);
            }
        }
        
        Map<String, Integer> formacionRequerida = parseFormacion(formacion);
        Map<String, List<JugadorEntity>> jugadoresPorCategoria = agruparPorCategoria(jugadoresDisponibles);
        
        List<JugadorEntity> mejorEscuadra = new ArrayList<>();
        List<JugadorEntity> escuadraActual = new ArrayList<>();
        double[] mejorValor = {0.0};
        double[] mejorRating = {0.0};
        
        backtrackEscuadraConPresupuesto(
            jugadoresPorCategoria, 
            formacionRequerida, 
            escuadraActual, 
            mejorEscuadra, 
            mejorValor,
            mejorRating,
            new HashMap<>(),
            0.0,
            presupuestoMaximo,
            jugadoresClub
        );
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("club", club.getNombre());
        resultado.put("formacion", formacion);
        resultado.put("presupuestoMaximo", presupuestoMaximo);
        resultado.put("escuadra", mejorEscuadra);
        resultado.put("valorTotal", mejorValor[0]);
        resultado.put("costoTotal", calcularCostoTotal(mejorEscuadra, jugadoresClub));
        resultado.put("ratingTotal", mejorRating[0]);
        resultado.put("jugadoresNuevos", contarJugadoresNuevos(mejorEscuadra, jugadoresClub));
        resultado.put("algoritmo", "Backtracking con Presupuesto");
        
        return resultado;
    }

    private void backtrackEscuadra(
            Map<String, List<JugadorEntity>> jugadoresPorCategoria,
            Map<String, Integer> formacionRequerida,
            List<JugadorEntity> escuadraActual,
            List<JugadorEntity> mejorEscuadra,
            double[] mejorValor,
            Map<String, Integer> contadorActual) {
        
        if (esFormacionCompleta(contadorActual, formacionRequerida)) {
            double valorTotal = calcularRatingTotal(escuadraActual);
            if (valorTotal > mejorValor[0]) {
                mejorValor[0] = valorTotal;
                mejorEscuadra.clear();
                mejorEscuadra.addAll(new ArrayList<>(escuadraActual));
            }
            return;
        }
        
        String categoriaActual = obtenerSiguienteCategoria(contadorActual, formacionRequerida);
        if (categoriaActual == null) return;
        
        List<JugadorEntity> candidatos = jugadoresPorCategoria.get(categoriaActual);
        if (candidatos == null || candidatos.isEmpty()) return;
        
        int actuales = contadorActual.getOrDefault(categoriaActual, 0);
        
        for (JugadorEntity jugador : candidatos) {
            if (!escuadraActual.contains(jugador)) {
                escuadraActual.add(jugador);
                contadorActual.put(categoriaActual, actuales + 1);
                
                backtrackEscuadra(
                    jugadoresPorCategoria,
                    formacionRequerida,
                    escuadraActual,
                    mejorEscuadra,
                    mejorValor,
                    contadorActual
                );
                
                escuadraActual.remove(escuadraActual.size() - 1);
                contadorActual.put(categoriaActual, actuales);
            }
        }
    }

    private void backtrackEscuadraConPresupuesto(
            Map<String, List<JugadorEntity>> jugadoresPorCategoria,
            Map<String, Integer> formacionRequerida,
            List<JugadorEntity> escuadraActual,
            List<JugadorEntity> mejorEscuadra,
            double[] mejorValor,
            double[] mejorRating,
            Map<String, Integer> contadorActual,
            double costoActual,
            double presupuestoMaximo,
            List<JugadorEntity> jugadoresClub) {
        
        if (esFormacionCompleta(contadorActual, formacionRequerida)) {
            double ratingTotal = calcularRatingTotal(escuadraActual);
            if (ratingTotal > mejorRating[0] && costoActual <= presupuestoMaximo) {
                mejorRating[0] = ratingTotal;
                mejorValor[0] = costoActual;
                mejorEscuadra.clear();
                mejorEscuadra.addAll(new ArrayList<>(escuadraActual));
            }
            return;
        }
        
        if (costoActual > presupuestoMaximo) {
            return; // Podar rama si excede presupuesto
        }
        
        String categoriaActual = obtenerSiguienteCategoria(contadorActual, formacionRequerida);
        if (categoriaActual == null) return;
        
        List<JugadorEntity> candidatos = jugadoresPorCategoria.get(categoriaActual);
        if (candidatos == null || candidatos.isEmpty()) return;
        
        int actuales = contadorActual.getOrDefault(categoriaActual, 0);
        
        for (JugadorEntity jugador : candidatos) {
            if (!escuadraActual.contains(jugador)) {
                double costoJugador = !jugadoresClub.contains(jugador) ? 
                    (jugador.getValorMercado() != null ? jugador.getValorMercado() : 0.0) : 0.0;
                
                escuadraActual.add(jugador);
                contadorActual.put(categoriaActual, actuales + 1);
                
                backtrackEscuadraConPresupuesto(
                    jugadoresPorCategoria,
                    formacionRequerida,
                    escuadraActual,
                    mejorEscuadra,
                    mejorValor,
                    mejorRating,
                    contadorActual,
                    costoActual + costoJugador,
                    presupuestoMaximo,
                    jugadoresClub
                );
                
                escuadraActual.remove(escuadraActual.size() - 1);
                contadorActual.put(categoriaActual, actuales);
            }
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private boolean esPosibleFormarEquipo(
            Map<String, List<JugadorEntity>> jugadoresPorCategoria,
            Map<String, Integer> formacionRequerida) {
        
        for (Map.Entry<String, Integer> entry : formacionRequerida.entrySet()) {
            String categoria = entry.getKey();
            int necesarios = entry.getValue();
            int disponibles = jugadoresPorCategoria.getOrDefault(categoria, new ArrayList<>()).size();
            
            if (disponibles < necesarios) {
                return false;
            }
        }
        return true;
    }

    private Map<String, List<JugadorEntity>> agruparPorCategoria(List<JugadorEntity> jugadores) {
        Map<String, List<JugadorEntity>> grupos = new HashMap<>();
        grupos.put("Arquero", new ArrayList<>());
        grupos.put("Defensa", new ArrayList<>());
        grupos.put("Mediocampo", new ArrayList<>());
        grupos.put("Delantero", new ArrayList<>());
        
        for (JugadorEntity jugador : jugadores) {
            String categoria = determinarCategoria(jugador.getPosicion());
            if (!grupos.containsKey(categoria)) {
                grupos.put(categoria, new ArrayList<>());
            }
            grupos.get(categoria).add(jugador);
        }
        
        // Ordenar por valor de mercado (mejores primero)
        for (List<JugadorEntity> grupo : grupos.values()) {
            grupo.sort((j1, j2) -> {
                double v1 = j1.getValorMercado() != null ? j1.getValorMercado() : 0.0;
                double v2 = j2.getValorMercado() != null ? j2.getValorMercado() : 0.0;
                return Double.compare(v2, v1); // Descendente
            });
        }
        return grupos;
    }

    private Map<String, Integer> parseFormacion(String formacion) {
        Map<String, Integer> formacionMap = new HashMap<>();
        try {
            String[] partes = formacion.split("-");
            if (partes.length != 3) {
                throw new RuntimeException("Formato inválido. Use #-#-# (ej: 4-3-3)");
            }
            formacionMap.put("Arquero", 1); // Siempre un arquero
            formacionMap.put("Defensa", Integer.parseInt(partes[0]));
            formacionMap.put("Mediocampo", Integer.parseInt(partes[1]));
            formacionMap.put("Delantero", Integer.parseInt(partes[2]));
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear formación: " + formacion);
        }
        return formacionMap;
    }

    private boolean esFormacionCompleta(
            Map<String, Integer> contadorActual, 
            Map<String, Integer> formacionRequerida) {
        
        for (Map.Entry<String, Integer> entry : formacionRequerida.entrySet()) {
            int necesarios = entry.getValue();
            int actuales = contadorActual.getOrDefault(entry.getKey(), 0);
            if (actuales != necesarios) {
                return false;
            }
        }
        return true;
    }

    private String obtenerSiguienteCategoria(
            Map<String, Integer> contadorActual,
            Map<String, Integer> formacionRequerida) {
        
        String[] orden = {"Arquero", "Defensa", "Mediocampo", "Delantero"};
        for (String categoria : orden) {
            int necesarios = formacionRequerida.getOrDefault(categoria, 0);
            int actuales = contadorActual.getOrDefault(categoria, 0);
            if (actuales < necesarios) {
                return categoria;
            }
        }
        return null;
    }

    private String determinarCategoria(String posicion) {
        if (posicion == null) return "Mediocampo";
        String pos = posicion.toLowerCase().trim();

        // Arqueros
        if (pos.contains("arquero") || pos.contains("portero") || pos.equals("gk")) {
            return "Arquero";
        }

        // Defensas
        if (pos.contains("defens") || pos.equals("defensa") ||
            pos.contains("lateral") || pos.contains("central") ||
            pos.equals("df") || pos.equals("lb") || pos.equals("rb") ||
            pos.equals("cb") || pos.equals("rwb") || pos.equals("lwb")) {
            return "Defensa";
        }

        // Delanteros
        if (pos.contains("delantero") || pos.equals("delantero") ||
            pos.contains("atacante") || pos.contains("ariete") ||
            pos.equals("dc") || pos.equals("st") || pos.equals("cf") ||
            pos.equals("lw") || pos.equals("rw") || pos.contains("extremo")) {
            return "Delantero";
        }

        // Por defecto: mediocampista
        return "Mediocampo";
    }

    private double calcularRatingTotal(List<JugadorEntity> escuadra) {
        return escuadra.stream()
            .mapToDouble(j -> j.getValorMercado() != null ? j.getValorMercado() : 0.0)
            .sum();
    }

    private double calcularCostoTotal(List<JugadorEntity> escuadra, List<JugadorEntity> jugadoresClub) {
        double costo = 0.0;
        for (JugadorEntity jugador : escuadra) {
            // Solo contar costo de jugadores que no están en el club
            if (!jugadoresClub.contains(jugador)) {
                costo += jugador.getValorMercado() != null ? jugador.getValorMercado() : 0.0;
            }
        }
        return costo;
    }

    private int contarJugadoresNuevos(List<JugadorEntity> escuadra, List<JugadorEntity> jugadoresClub) {
        int count = 0;
        for (JugadorEntity jugador : escuadra) {
            if (!jugadoresClub.contains(jugador)) {
                count++;
            }
        }
        return count;
    }

    private Map<String, Integer> obtenerDesglosePorPosicion(List<JugadorEntity> escuadra) {
        Map<String, Integer> desglose = new HashMap<>();
        for (JugadorEntity jugador : escuadra) {
            String categoria = determinarCategoria(jugador.getPosicion());
            desglose.put(categoria, desglose.getOrDefault(categoria, 0) + 1);
        }
        return desglose;
    }
}