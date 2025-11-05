package com.uade.transferencia_futbol.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDate;

@RelationshipProperties
public class TransferenciaEntity {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("monto")
    private Double monto;
    
    @Property("fecha")
    private LocalDate fecha;
    
    @Property("temporada")
    private String temporada;
    
    @Property("tipoTransferencia")
    private String tipoTransferencia; // "Compra", "Préstamo", "Libre"
    
    @TargetNode
    private ClubEntity clubDestino;
    
    public TransferenciaEntity() {}
    
    public TransferenciaEntity(Double monto, LocalDate fecha, String temporada, String tipoTransferencia, ClubEntity clubDestino) {
        this.monto = monto;
        this.fecha = fecha;
        this.temporada = temporada;
        this.tipoTransferencia = tipoTransferencia;
        this.clubDestino = clubDestino;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Double getMonto() {
        return monto;
    }
    
    public void setMonto(Double monto) {
        this.monto = monto;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public String getTemporada() {
        return temporada;
    }
    
    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }
    
    public String getTipoTransferencia() {
        return tipoTransferencia;
    }
    
    public void setTipoTransferencia(String tipoTransferencia) {
        this.tipoTransferencia = tipoTransferencia;
    }
    
    public ClubEntity getClubDestino() {
        return clubDestino;
    }
    
    public void setClubDestino(ClubEntity clubDestino) {
        this.clubDestino = clubDestino;
    }
}