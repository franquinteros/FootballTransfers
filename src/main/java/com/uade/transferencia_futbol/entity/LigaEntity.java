package com.uade.transferencia_futbol.entity;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.INCOMING;

@Node("Liga")
public class LigaEntity {
    
    @Id
    private String nombre;
    
    @Property("pais")
    private String pais;
    
    @Property("nivel")
    private Integer nivel;
    
    @Property("cantidadEquipos")
    private Integer cantidadEquipos;
    
    @Relationship(type = "COMPITE_EN", direction = INCOMING)
    private Set<ClubEntity> clubes = new HashSet<>();
    
    public LigaEntity() {}
    
    public LigaEntity(String nombre, String pais, Integer nivel, Integer cantidadEquipos) {
        this.nombre = nombre;
        this.pais = pais;
        this.nivel = nivel;
        this.cantidadEquipos = cantidadEquipos;
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
    
    public Integer getNivel() {
        return nivel;
    }
    
    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }
    
    public Integer getCantidadEquipos() {
        return cantidadEquipos;
    }
    
    public void setCantidadEquipos(Integer cantidadEquipos) {
        this.cantidadEquipos = cantidadEquipos;
    }
    
    public Set<ClubEntity> getClubes() {
        return clubes;
    }
    
    public void setClubes(Set<ClubEntity> clubes) {
        this.clubes = clubes;
    }
}