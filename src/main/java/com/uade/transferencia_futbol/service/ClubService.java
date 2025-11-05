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
    
    // Crear club
    public ClubEntity crearClub(ClubEntity club) {
        if (clubRepository.existsById(club.getNombre())) {
            throw new RuntimeException("El club ya existe: " + club.getNombre());
        }
        return clubRepository.save(club);
    }
    
    // Obtener todos los clubes
    public List<ClubEntity> obtenerTodosClubes() {
        return clubRepository.findAll();
    }
    
    // Obtener club por nombre
    public Optional<ClubEntity> obtenerClubPorNombre(String nombre) {
        return clubRepository.findById(nombre);
    }
    
    // Actualizar club
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
    
    // Eliminar club
    public void eliminarClub(String nombre) {
        if (!clubRepository.existsById(nombre)) {
            throw new RuntimeException("Club no encontrado: " + nombre);
        }
        clubRepository.deleteById(nombre);
    }
    
    // Asignar club a una liga
    public ClubEntity asignarLiga(String nombreClub, String nombreLiga) {
        ClubEntity club = clubRepository.findById(nombreClub)
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClub));
        
        LigaEntity liga = ligaRepository.findById(nombreLiga)
            .orElseThrow(() -> new RuntimeException("Liga no encontrada: " + nombreLiga));
        
        club.setLigaEntity(liga);
        club.setLiga(nombreLiga);
        return clubRepository.save(club);
    }
    
    // Buscar clubes por país
    public List<ClubEntity> buscarPorPais(String pais) {
        return clubRepository.findByPais(pais);
    }
    
    // Buscar clubes por liga
    public List<ClubEntity> buscarPorLiga(String liga) {
        return clubRepository.findByLiga(liga);
    }
    
    // Buscar clubes con presupuesto mínimo
    public List<ClubEntity> buscarPorPresupuestoMinimo(Double presupuestoMinimo) {
        return clubRepository.findByPresupuestoGreaterThan(presupuestoMinimo);
    }
    
    // Buscar clubes fundados después de un año
    public List<ClubEntity> buscarFundadosDespuesDe(Integer anio) {
        return clubRepository.findByFundacionGreaterThan(anio);
    }
    
    // Obtener clubes de una liga
    public List<ClubEntity> obtenerClubesDeUnaLiga(String nombreLiga) {
        return clubRepository.findClubesByLiga(nombreLiga);
    }
    
    // Clubes que pueden pagar por un jugador
    public List<ClubEntity> clubesQuePuedenPagar(Double valorJugador) {
        return clubRepository.findClubesQuePuedenPagar(valorJugador);
    }
    
    // Obtener clubes con jugadores de una nacionalidad
    public List<ClubEntity> clubesConJugadoresDeNacionalidad(String nacionalidad) {
        return clubRepository.findClubesConJugadoresDeNacionalidad(nacionalidad);
    }
    
    // Top clubes por presupuesto
    public List<ClubEntity> obtenerTopClubesPorPresupuesto(Integer limit) {
        return clubRepository.findTopClubesByPresupuesto(limit);
    }
    
    // Actualizar presupuesto
    public ClubEntity actualizarPresupuesto(String nombre, Double nuevoPresupuesto) {
        return clubRepository.findById(nombre)
            .map(club -> {
                club.setPresupuesto(nuevoPresupuesto);
                return clubRepository.save(club);
            })
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombre));
    }
    
    // Reducir presupuesto (para transferencias)
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
    
    // Aumentar presupuesto (para ventas)
    public ClubEntity aumentarPresupuesto(String nombre, Double monto) {
        return clubRepository.findById(nombre)
            .map(club -> {
                club.setPresupuesto(club.getPresupuesto() + monto);
                return clubRepository.save(club);
            })
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombre));
    }

    // =================================================================
    // ALGORITMOS COMPLEJOS
    // =================================================================

    // /api/clubs/optimal-squad - Backtracking para formación
    /**
     * Implementación de Backtracking para seleccionar la mejor combinación de jugadores
     * (obtenidos de JugadorRepository) para una formación dada,
     * maximizando algún valor (ej. rating total) y respetando restricciones (posiciones, presupuesto).
     */
   public List<JugadorEntity> obtenerEscuadraOptima(String nombreClub, String formacion) {
        ClubEntity club = clubRepository.findById(nombreClub)
            .orElseThrow(() -> new RuntimeException("Club no encontrado: " + nombreClub));
        
        List<JugadorEntity> jugadoresClub = jugadorRepository.findJugadoresByClub(nombreClub);
        Map<String, Integer> formacionRequerida = parseFormacion(formacion);
        
        List<JugadorEntity> mejorEscuadra = new ArrayList<>();
        List<JugadorEntity> escuadraActual = new ArrayList<>();
        double[] mejorRating = {0.0};
        
        backtrackEscuadra(jugadoresClub, formacionRequerida, escuadraActual, mejorEscuadra, mejorRating, 0);
        
        return mejorEscuadra;
    }
    
    private void backtrackEscuadra(List<JugadorEntity> jugadores, 
                                  Map<String, Integer> formacionRequerida,
                                  List<JugadorEntity> escuadraActual,
                                  List<JugadorEntity> mejorEscuadra,
                                  double[] mejorRating,
                                  int index) {
        
        // Verificar si tenemos una formación válida
        if (esFormacionValida(escuadraActual, formacionRequerida)) {
            double ratingTotal = calcularRatingTotal(escuadraActual);
            if (ratingTotal > mejorRating[0]) {
                mejorRating[0] = ratingTotal;
                mejorEscuadra.clear();
                mejorEscuadra.addAll(new ArrayList<>(escuadraActual));
            }
            return;
        }
        
        if (index >= jugadores.size()) {
            return;
        }
        
        // Incluir el jugador actual
        escuadraActual.add(jugadores.get(index));
        backtrackEscuadra(jugadores, formacionRequerida, escuadraActual, mejorEscuadra, mejorRating, index + 1);
        escuadraActual.remove(escuadraActual.size() - 1);
        
        // Excluir el jugador actual
        backtrackEscuadra(jugadores, formacionRequerida, escuadraActual, mejorEscuadra, mejorRating, index + 1);
    }
    
    private Map<String, Integer> parseFormacion(String formacion) {
        Map<String, Integer> formacionMap = new HashMap<>();
        // Ejemplo: "4-3-3" -> 4 defensores, 3 mediocampistas, 3 delanteros
        String[] partes = formacion.split("-");
        formacionMap.put("Defensa", Integer.parseInt(partes[0]));
        formacionMap.put("Mediocampo", Integer.parseInt(partes[1]));
        formacionMap.put("Delantero", Integer.parseInt(partes[2]));
        return formacionMap;
    }
    
    private boolean esFormacionValida(List<JugadorEntity> escuadra, Map<String, Integer> formacionRequerida) {
        Map<String, Integer> conteoActual = new HashMap<>();
        conteoActual.put("Defensa", 0);
        conteoActual.put("Mediocampo", 0);
        conteoActual.put("Delantero", 0);
        
        for (JugadorEntity jugador : escuadra) {
            String posicion = determinarCategoria(jugador.getPosicion());
            conteoActual.put(posicion, conteoActual.get(posicion) + 1);
        }
        
        return conteoActual.equals(formacionRequerida);
    }
    
    private String determinarCategoria(String posicion) {
        if (posicion.contains("defensa") || posicion.equals("Defensa")) return "Defensa";
        if (posicion.contains("mediocampo") || posicion.equals("Mediocampista")) return "Mediocampo";
        if (posicion.contains("delantero") || posicion.equals("Delantero")) return "Delantero";
        return "Mediocampo"; // default
    }
    
    private double calcularRatingTotal(List<JugadorEntity> escuadra) {
        return escuadra.stream()
            .mapToDouble(j -> j.getValorMercado() != null ? j.getValorMercado() / 10000000 : 0.0)
            .sum();
    }
}