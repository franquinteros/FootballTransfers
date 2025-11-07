package com.uade.transferencia_futbol.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Node("Jugador")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nombre")
public class JugadorEntity {
    
    @Id
    private String nombre;
    
    @Property("edad")
    private Integer edad;
    
    @Property("posicion")
    private String posicion;
    
    @Property("valorMercado")
    private Double valorMercado;
    
    @Property("nacionalidad")
    private String nacionalidad;
    
    @Relationship(type = "JUEGA_EN", direction = OUTGOING)
    private ClubEntity clubActual;
    
    @Relationship(type = "REPRESENTADO_POR", direction = OUTGOING)
    private AgenteEntity agente;
    
    public JugadorEntity() {}
    
    public JugadorEntity(String nombre, Integer edad, String posicion, Double valorMercado, String nacionalidad) {
        this.nombre = nombre;
        this.edad = edad;
        this.posicion = posicion;
        this.valorMercado = valorMercado;
        this.nacionalidad = nacionalidad;
    }
    
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Integer getEdad() {
        return edad;
    }
    
    public void setEdad(Integer edad) {
        this.edad = edad;
    }
    
    public String getPosicion() {
        return posicion;
    }
    
    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }
    
    public Double getValorMercado() {
        return valorMercado;
    }
    
    public void setValorMercado(Double valorMercado) {
        this.valorMercado = valorMercado;
    }
    
    public String getNacionalidad() {
        return nacionalidad;
    }
    
    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }
    
    public ClubEntity getClubActual() {
        return clubActual;
    }
    
    public void setClubActual(ClubEntity clubActual) {
        this.clubActual = clubActual;
    }
    
    public AgenteEntity getAgente() {
        return agente;
    }
    
    public void setAgente(AgenteEntity agente) {
        this.agente = agente;
    }
}