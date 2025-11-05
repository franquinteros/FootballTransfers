package com.uade.transferencia_futbol.repository;

import com.uade.transferencia_futbol.entity.AgenteEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgenteRepository extends Neo4jRepository<AgenteEntity, String> {
    
    // Búsquedas básicas
    List<AgenteEntity> findByAgencia(String agencia);
    
    List<AgenteEntity> findByNacionalidad(String nacionalidad);
    
    List<AgenteEntity> findByComisionGreaterThan(Double comisionMinima);
    
    List<AgenteEntity> findByComisionBetween(Double comisionMin, Double comisionMax);
    
    // Consultas personalizadas con Cypher
    
    @Query("MATCH (a:Agente)<-[:REPRESENTADO_POR]-(j:Jugador) " +
           "WITH a, count(j) as numJugadores " +
           "WHERE numJugadores >= $minJugadores " +
           "RETURN a")
    List<AgenteEntity> findAgentesConMinimoJugadores(@Param("minJugadores") Integer minJugadores);
    
    @Query("MATCH (a:Agente {nombre: $nombreAgente})<-[:REPRESENTADO_POR]-(j:Jugador) " +
           "RETURN sum(j.valorMercado)")
    Double getValorTotalCartera(@Param("nombreAgente") String nombreAgente);
    
    @Query("MATCH (a:Agente)<-[:REPRESENTADO_POR]-(j:Jugador) " +
           "RETURN a, count(j) as numJugadores " +
           "ORDER BY numJugadores DESC " +
           "LIMIT $limit")
    List<AgenteEntity> findTopAgentesByNumeroJugadores(@Param("limit") Integer limit);
    
    @Query("MATCH (a:Agente)<-[:REPRESENTADO_POR]-(j:Jugador)-[:JUEGA_EN]->(c:Club {nombre: $nombreClub}) " +
           "RETURN DISTINCT a")
    List<AgenteEntity> findAgentesConJugadoresEnClub(@Param("nombreClub") String nombreClub);
    
    @Query("MATCH (a:Agente)<-[:REPRESENTADO_POR]-(j:Jugador) " +
           "WITH a, sum(j.valorMercado) as valorTotal " +
           "RETURN a ORDER BY valorTotal DESC LIMIT $limit")
    List<AgenteEntity> findTopAgentesByValorCartera(@Param("limit") Integer limit);
    
    @Query("MATCH (a:Agente {agencia: $agencia})<-[:REPRESENTADO_POR]-(j:Jugador) " +
           "RETURN count(j)")
    Integer countJugadoresByAgencia(@Param("agencia") String agencia);
    
    @Query("MATCH (a:Agente) WHERE a.comision >= $comisionMin RETURN a ORDER BY a.comision DESC")
    List<AgenteEntity> findAllOrderByComisionDesc(@Param("comisionMin") Double comisionMin);
}