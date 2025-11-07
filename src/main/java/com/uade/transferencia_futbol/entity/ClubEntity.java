package com.uade.transferencia_futbol.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.INCOMING;
import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Node("Club")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nombre")
public class ClubEntity {
    
    @Id
    private String nombre;
    
    @Property("pais")
    private String pais;
    
    @Property("presupuesto")
    private Double presupuesto;
    
    @Property("liga")
    private String liga;
    
    @Property("fundacion")
    private Integer fundacion;
    
    @Relationship(type = "JUEGA_EN", direction = INCOMING)
    private Set<JugadorEntity> jugadores = new HashSet<>();
    
    @Relationship(type = "COMPITE_EN", direction = OUTGOING)
    private LigaEntity ligaEntity;
    
    public ClubEntity() {}
    
    public ClubEntity(String nombre, String pais, Double presupuesto, String liga, Integer fundacion) {
        this.nombre = nombre;
        this.pais = pais;
        this.presupuesto = presupuesto;
        this.liga = liga;
        this.fundacion = fundacion;
    }
    
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getPais() {
        return pais;
    }
    
    public void setPais(String pais) {
        this.pais = pais;
    }
    
    public Double getPresupuesto() {
        return presupuesto;
    }
    
    public void setPresupuesto(Double presupuesto) {
        this.presupuesto = presupuesto;
    }
    
    public String getLiga() {
        return liga;
    }
    
    public void setLiga(String liga) {
        this.liga = liga;
    }
    
    public Integer getFundacion() {
        return fundacion;
    }
    
    public void setFundacion(Integer fundacion) {
        this.fundacion = fundacion;
    }
    
    public Set<JugadorEntity> getJugadores() {
        return jugadores;
    }
    
    public void setJugadores(Set<JugadorEntity> jugadores) {
        this.jugadores = jugadores;
    }
    
    public LigaEntity getLigaEntity() {
        return ligaEntity;
    }
    
    public void setLigaEntity(LigaEntity ligaEntity) {
        this.ligaEntity = ligaEntity;
    }
}