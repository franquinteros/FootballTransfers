package com.uade.transferencia_futbol.repository;

import com.uade.transferencia_futbol.entity.ClubEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends Neo4jRepository<ClubEntity, String> {
    
    // ==================== BÚSQUEDAS BÁSICAS ====================
    
    List<ClubEntity> findByPais(String pais);
    
    List<ClubEntity> findByLiga(String liga);
    
    List<ClubEntity> findByPresupuestoGreaterThan(Double presupuestoMinimo);
    
    List<ClubEntity> findByFundacionGreaterThan(Integer anio);
    
    // ==================== CONSULTAS PERSONALIZADAS CON CYPHER ====================
    
    @Query("MATCH (c:Club)-[:COMPITE_EN]->(l:Liga {nombre: $nombreLiga}) RETURN c")
    List<ClubEntity> findClubesByLiga(@Param("nombreLiga") String nombreLiga);
    
    @Query("MATCH (c:Club) WHERE c.presupuesto >= $valorJugador RETURN c")
    List<ClubEntity> findClubesQuePuedenPagar(@Param("valorJugador") Double valorJugador);
    
    @Query("MATCH (c:Club)<-[:JUEGA_EN]-(j:Jugador {nacionalidad: $nacionalidad}) RETURN DISTINCT c")
    List<ClubEntity> findClubesConJugadoresDeNacionalidad(@Param("nacionalidad") String nacionalidad);
    
    @Query("MATCH (c:Club) RETURN c ORDER BY c.presupuesto DESC LIMIT $limit")
    List<ClubEntity> findTopClubesByPresupuesto(@Param("limit") Integer limit);
    
    @Query("MATCH (c:Club)<-[:JUEGA_EN]-(j:Jugador) WITH c, count(j) as jugadoresCount RETURN c ORDER BY jugadoresCount DESC LIMIT $limit")
    List<ClubEntity> findTopClubesByNumeroJugadores(@Param("limit") Integer limit);
    
    @Query("MATCH (c:Club {nombre: $nombreClub})<-[:JUEGA_EN]-(j:Jugador) RETURN sum(j.valorMercado)")
    Double getValorTotalPlantilla(@Param("nombreClub") String nombreClub);
    
    @Query("MATCH (c:Club) WHERE c.presupuesto >= $presupuestoMin AND c.presupuesto <= $presupuestoMax RETURN c")
    List<ClubEntity> findByPresupuestoBetween(
        @Param("presupuestoMin") Double presupuestoMin, 
        @Param("presupuestoMax") Double presupuestoMax
    );
    
    @Query("MATCH (c:Club)-[:COMPITE_EN]->(l:Liga) WHERE l.nivel >= $nivelMinimo RETURN c")
    List<ClubEntity> findClubesEnLigasTopNivel(@Param("nivelMinimo") Integer nivelMinimo);
    
    @Query("MATCH (c:Club) RETURN c ORDER BY c.fundacion ASC")
    List<ClubEntity> findAllOrderByFundacionAsc();
    
    // ==================== CONSULTAS ADICIONALES ====================
    
    @Query("MATCH (c:Club)<-[:JUEGA_EN]-(j:Jugador) WITH c, avg(j.edad) as edadPromedio RETURN c ORDER BY edadPromedio ASC")
    List<ClubEntity> findClubesOrdenadosPorEdadPromedioAsc();
    
    @Query("MATCH (c:Club)<-[:JUEGA_EN]-(j:Jugador) WITH c, avg(j.edad) as edadPromedio RETURN c ORDER BY edadPromedio DESC")
    List<ClubEntity> findClubesOrdenadosPorEdadPromedioDesc();
    
    @Query("MATCH (c:Club)<-[:JUEGA_EN]-(j:Jugador) WHERE j.valorMercado IS NOT NULL WITH c, max(j.valorMercado) as maxValor RETURN c ORDER BY maxValor DESC LIMIT $limit")
    List<ClubEntity> findTopClubesPorJugadorMasValioso(@Param("limit") Integer limit);
    
    @Query("MATCH (c:Club) WHERE c.pais = $pais AND c.presupuesto >= $presupuestoMin RETURN c")
    List<ClubEntity> findClubesPorPaisYPresupuestoMin(
        @Param("pais") String pais, 
        @Param("presupuestoMin") Double presupuestoMin
    );
    
    @Query("MATCH (c:Club) WHERE toLower(c.nombre) CONTAINS toLower($nombre) RETURN c")
    List<ClubEntity> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    @Query("MATCH (c:Club) WHERE c.fundacion BETWEEN $anioInicio AND $anioFin RETURN c ORDER BY c.fundacion")
    List<ClubEntity> findByFundacionBetween(
        @Param("anioInicio") Integer anioInicio, 
        @Param("anioFin") Integer anioFin
    );
    
    @Query("MATCH (c:Club)<-[:JUEGA_EN]-(j:Jugador) WITH c, count(j) as totalJugadores, sum(j.valorMercado) as valorTotal RETURN c, totalJugadores, valorTotal ORDER BY valorTotal DESC LIMIT $limit")
    List<Object[]> findTopClubesPorValorTotalPlantilla(@Param("limit") Integer limit);
}