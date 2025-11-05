package com.uade.transferencia_futbol.repository;

import com.uade.transferencia_futbol.entity.JugadorEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JugadorRepository extends Neo4jRepository<JugadorEntity, String> {
    
    // Búsquedas básicas
    List<JugadorEntity> findByPosicion(String posicion);
    
    List<JugadorEntity> findByNacionalidad(String nacionalidad);
    
    List<JugadorEntity> findByEdadBetween(Integer edadMin, Integer edadMax);
    
    List<JugadorEntity> findByValorMercadoGreaterThan(Double valorMinimo);
    
    // Consultas personalizadas con Cypher
    
    @Query("MATCH (j:Jugador)-[:JUEGA_EN]->(c:Club {nombre: $nombreClub}) RETURN j")
    List<JugadorEntity> findJugadoresByClub(@Param("nombreClub") String nombreClub);
    
    @Query("MATCH (j:Jugador)-[:REPRESENTADO_POR]->(a:Agente {nombre: $nombreAgente}) RETURN j")
    List<JugadorEntity> findJugadoresByAgente(@Param("nombreAgente") String nombreAgente);
    
    @Query("MATCH (j:Jugador) WHERE NOT (j)-[:JUEGA_EN]->() RETURN j")
    List<JugadorEntity> findJugadoresLibres();
    
    @Query("MATCH (j:Jugador) WHERE j.valorMercado IS NOT NULL RETURN j ORDER BY j.valorMercado DESC LIMIT $limit")
    List<JugadorEntity> findTopJugadoresByValor(@Param("limit") Integer limit);
    
    @Query("MATCH (j:Jugador)-[:JUEGA_EN]->(c:Club) WHERE c.nombre = $nombreClub RETURN count(j)")
    Integer countJugadoresByClub(@Param("nombreClub") String nombreClub);
    
    @Query("MATCH (j:Jugador) WHERE j.edad >= $edadMin AND j.edad <= $edadMax AND j.posicion = $posicion RETURN j")
    List<JugadorEntity> findByEdadRangoAndPosicion(
        @Param("edadMin") Integer edadMin, 
        @Param("edadMax") Integer edadMax, 
        @Param("posicion") String posicion
    );
    
    @Query("MATCH (j:Jugador)-[:JUEGA_EN]->(c:Club)-[:COMPITE_EN]->(l:Liga {nombre: $nombreLiga}) RETURN j")
    List<JugadorEntity> findJugadoresByLiga(@Param("nombreLiga") String nombreLiga);
    
    @Query("MATCH (j:Jugador) RETURN j ORDER BY j.valorMercado DESC")
    List<JugadorEntity> findAllOrderByValorMercadoDesc();
    
    @Query("MATCH (j:Jugador)-[:JUEGA_EN]->(c:Club {pais: $pais}) RETURN j")
    List<JugadorEntity> findJugadoresByPaisClub(@Param("pais") String pais);
}