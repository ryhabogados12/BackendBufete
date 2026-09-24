package com.bufete.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "expediente", indexes = {
    @Index(name = "idx_expediente_proceso", columnList = "proceso_id"),
    @Index(name = "idx_expediente_estado", columnList = "estado"),
    @Index(name = "idx_expediente_created_by", columnList = "created_by")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expediente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    private EstadoExpediente estado = EstadoExpediente.ACTIVO;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant fechaCreacion;
    
    @Column(name = "fecha_cierre")
    private Instant fechaCierre;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;
    
    @Column(name = "root_node_id", unique = true)
    private UUID rootNodeId;

    @Column(name = "plantilla_expediente_id")
    private Integer plantillaExpedienteId;
    
    @Column(name = "orden")
    @Builder.Default
    private Integer orden = 1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Usuario createdBy;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    
    @OneToMany(mappedBy = "expediente", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Node> nodes = new HashSet<>();
    
    @OneToMany(mappedBy = "expediente")
    @Builder.Default
    private Set<Evento> eventos = new HashSet<>();
    
    @OneToMany(mappedBy = "expediente")
    @Builder.Default
    private Set<Sentencia> sentencias = new HashSet<>();
    
    public enum EstadoExpediente {
        ACTIVO, ARCHIVADO, CERRADO
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoExpediente getEstado() {
        return estado;
    }

    public void setEstado(EstadoExpediente estado) {
        this.estado = estado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Instant getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Instant fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Proceso getProceso() {
        return proceso;
    }

    public void setProceso(Proceso proceso) {
        this.proceso = proceso;
    }

    public UUID getRootNodeId() {
        return rootNodeId;
    }

    public void setRootNodeId(UUID rootNodeId) {
        this.rootNodeId = rootNodeId;
    }

    public Integer getPlantillaExpedienteId() {
        return plantillaExpedienteId;
    }

    public void setPlantillaExpedienteId(Integer plantillaExpedienteId) {
        this.plantillaExpedienteId = plantillaExpedienteId;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Usuario getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Usuario createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    public Set<Evento> getEventos() {
        return eventos;
    }

    public void setEventos(Set<Evento> eventos) {
        this.eventos = eventos;
    }

    public Set<Sentencia> getSentencias() {
        return sentencias;
    }

    public void setSentencias(Set<Sentencia> sentencias) {
        this.sentencias = sentencias;
    }

    
}