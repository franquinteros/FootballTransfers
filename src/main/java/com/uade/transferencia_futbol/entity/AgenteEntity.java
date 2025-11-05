package com.uade.transferencia_futbol.entity;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.INCOMING;

@Node("Agente")
public class AgenteEntity {
    
    @Id
    private String nombre;
    
    @Property("agencia")
    private String agencia;
    
    @Property("comision")
    private Double comision;
    
    @Property("nacionalidad")
    private String nacionalidad;
    
    @Relationship(type = "REPRESENTADO_POR", direction = INCOMING)
    private Set<JugadorEntity> jugadores = new HashSet<>();
    
    public AgenteEntity() {}
    
    public AgenteEntity(String nombre, String agencia, Double comision, String nacionalidad) {
        this.nombre = nombre;
        this.agencia = agencia;
        this.comision = comision;
        this.nacionalidad = nacionalidad;
    }
    
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getAgencia() {
        return agencia;
    }
    
    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }
    
    public Double getComision() {
        return comision;
    }
    
    public void setComision(Double comision) {
        this.comision = comision;
    }
    
    public String getNacionalidad() {
        return nacionalidad;
    }
    
    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }
    
    public Set<JugadorEntity> getJugadores() {
        return jugadores;
    }
    
    public void setJugadores(Set<JugadorEntity> jugadores) {
        this.jugadores = jugadores;
    }
}