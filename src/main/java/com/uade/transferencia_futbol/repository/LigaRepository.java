package com.uade.transferencia_futbol.repository;

import com.uade.transferencia_futbol.entity.LigaEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigaRepository extends Neo4jRepository<LigaEntity, String> {
    
    // Búsquedas básicas
    List<LigaEntity> findByPais(String pais);
    
    List<LigaEntity> findByNivel(Integer nivel);
    
    List<LigaEntity> findByCantidadEquiposGreaterThan(Integer cantidad);
    
    // Consultas personalizadas con Cypher
    
    @Query("MATCH (l:Liga) RETURN l ORDER BY l.nivel ASC LIMIT $limit")
    List<LigaEntity> findTopLigasByNivel(@Param("limit") Integer limit);
    
    @Query("MATCH (l:Liga)<-[:COMPITE_EN]-(c:Club) " +
           "WITH l, sum(c.presupuesto) as presupuestoTotal " +
           "WHERE presupuestoTotal >= $presupuestoMinimo " +
           "RETURN l")
    List<LigaEntity> findLigasConPresupuestoMinimo(@Param("presupuestoMinimo") Double presupuestoMinimo);
    
    @Query("MATCH (l:Liga {nombre: $nombreLiga})<-[:COMPITE_EN]-(c:Club)<-[:JUEGA_EN]-(j:Jugador) " +
           "RETURN count(j)")
    Integer countJugadoresEnLiga(@Param("nombreLiga") String nombreLiga);
    
    @Query("MATCH (l:Liga {nombre: $nombreLiga})<-[:COMPITE_EN]-(c:Club)<-[:JUEGA_EN]-(j:Jugador) " +
           "RETURN sum(j.valorMercado)")
    Double getValorMercadoTotalLiga(@Param("nombreLiga") String nombreLiga);
    
    @Query("MATCH (l:Liga) RETURN l ORDER BY l.cantidadEquipos DESC")
    List<LigaEntity> findAllOrderByCantidadEquipos();
    
    @Query("MATCH (l:Liga)<-[:COMPITE_EN]-(c:Club) " +
           "RETURN l, count(c) as numClubes " +
           "ORDER BY numClubes DESC")
    List<LigaEntity> findLigasOrderByClubesActivos();
    
    @Query("MATCH (l:Liga {pais: $pais}) RETURN l ORDER BY l.nivel ASC")
    List<LigaEntity> findLigasByPaisOrderByNivel(@Param("pais") String pais);
    
    @Query("MATCH (l:Liga)<-[:COMPITE_EN]-(c:Club)<-[:JUEGA_EN]-(j:Jugador) " +
           "WITH l, avg(j.edad) as edadPromedio " +
           "RETURN l ORDER BY edadPromedio ASC LIMIT $limit")
    List<LigaEntity> findLigasConJugadoresMasJovenes(@Param("limit") Integer limit);
}