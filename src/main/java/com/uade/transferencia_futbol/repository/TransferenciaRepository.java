package com.uade.transferencia_futbol.repository;

import com.uade.transferencia_futbol.entity.TransferenciaEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransferenciaRepository extends Neo4jRepository<TransferenciaEntity, Long> {
    
    // Búsquedas básicas
    List<TransferenciaEntity> findByTemporada(String temporada);
    
    List<TransferenciaEntity> findByTipoTransferencia(String tipoTransferencia);
    
    List<TransferenciaEntity> findByMontoGreaterThan(Double montoMinimo);
    
    List<TransferenciaEntity> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    List<TransferenciaEntity> findByTemporadaAndTipoTransferencia(String temporada, String tipoTransferencia);
    
    // Consultas personalizadas con Cypher
    
    @Query("MATCH (j:Jugador {nombre: $nombreJugador})-[t:TRANSFERIDO]->(c:Club) " +
           "RETURN t ORDER BY t.fecha DESC")
    List<TransferenciaEntity> findTransferenciasByJugador(@Param("nombreJugador") String nombreJugador);
    
    @Query("MATCH ()-[t:TRANSFERIDO]->(c:Club {nombre: $nombreClub}) " +
           "RETURN t ORDER BY t.fecha DESC")
    List<TransferenciaEntity> findTransferenciasHaciaClub(@Param("nombreClub") String nombreClub);
    
    @Query("MATCH (c:Club {nombre: $nombreClub})-[t:TRANSFERIDO]->() " +
           "RETURN t ORDER BY t.fecha DESC")
    List<TransferenciaEntity> findTransferenciasDesdeClub(@Param("nombreClub") String nombreClub);
    
    @Query("MATCH ()-[t:TRANSFERIDO]->() " +
           "WHERE t.monto IS NOT NULL " +
           "RETURN t ORDER BY t.monto DESC LIMIT $limit")
    List<TransferenciaEntity> findTopTransferenciasByMonto(@Param("limit") Integer limit);
    
    @Query("MATCH ()-[t:TRANSFERIDO]->(c:Club {nombre: $nombreClub}) " +
           "WHERE t.monto IS NOT NULL " +
           "RETURN sum(t.monto)")
    Double getTotalInvertidoByClub(@Param("nombreClub") String nombreClub);
    
    @Query("MATCH ()-[t:TRANSFERIDO]->() " +
           "WHERE t.temporada = $temporada " +
           "RETURN t ORDER BY t.fecha DESC")
    List<TransferenciaEntity> findTransferenciasTemporadaActual(@Param("temporada") String temporada);
    
    @Query("MATCH (j:Jugador)-[t:TRANSFERIDO]->(c:Club) " +
           "RETURN j.nombre as jugador, c.nombre as club, t.monto as monto, t.fecha as fecha " +
           "ORDER BY t.monto DESC LIMIT $limit")
    List<Object> findTopTransferenciasDetalladas(@Param("limit") Integer limit);
    
    @Query("MATCH ()-[t:TRANSFERIDO]->() " +
           "WHERE t.fecha >= $fechaInicio AND t.fecha <= $fechaFin " +
           "RETURN count(t)")
    Integer countTransferenciasBetweenDates(
        @Param("fechaInicio") LocalDate fechaInicio, 
        @Param("fechaFin") LocalDate fechaFin
    );
    
    @Query("MATCH (j:Jugador {nombre: $nombreJugador})-[t:TRANSFERIDO]->(c:Club) " +
           "RETURN c.nombre ORDER BY t.fecha ASC")
    List<String> findHistorialClubesJugador(@Param("nombreJugador") String nombreJugador);
}