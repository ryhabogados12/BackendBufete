package com.bufete.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "proceso", indexes = {
    @Index(name = "idx_proceso_cliente", columnList = "cliente_id"),
    @Index(name = "idx_proceso_abogado", columnList = "abogado_responsable_id"),
    @Index(name = "idx_proceso_estado", columnList = "estado"),
    @Index(name = "idx_proceso_numero", columnList = "numero_proceso")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proceso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_proceso", unique = true, length = 100)
    private String numeroProceso;
    
    @Column(name = "nombre", length = 255)
    private String nombre;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "tipo_proceso", length = 100)
    private String tipoProceso;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    private EstadoProceso estado = EstadoProceso.ACTIVO;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant fechaCreacion;
    
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;
    
    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jurisdiccion_id")
    private Jurisdiccion jurisdiccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asunto_id")
    private Asunto asunto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_actual_id")
    private EtapaProceso etapaActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_procedimiento_id")
    private TipoProcedimiento tipoProcedimiento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abogado_responsable_id")
    private Usuario abogadoResponsable;
    
    @Column(name = "juzgado", length = 200)
    private String juzgado;
    
    @Column(name = "radicado", length = 100)
    private String radicado;
    
    @Column(name = "demandante", length = 300)
    private String demandante;
    
    @Column(name = "demandado", length = 300)
    private String demandado;
    
    @Column(name = "cuantia", precision = 15, scale = 2)
    private BigDecimal cuantia;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Usuario createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Expediente> expedientes = new HashSet<>();
    
    @OneToMany(mappedBy = "proceso")
    @Builder.Default
    private Set<Evento> eventos = new HashSet<>();
    
    @OneToMany(mappedBy = "proceso")
    @Builder.Default
    private Set<DocumentoContable> documentosContables = new HashSet<>();
    
    @OneToMany(mappedBy = "proceso")
    @Builder.Default
    private Set<Sentencia> sentencias = new HashSet<>();

    @OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<ProcesoEtapa> etapas = new HashSet<>();
    
    public enum EstadoProceso {
        ACTIVO, SUSPENDIDO, CERRADO, ARCHIVADO
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroProceso() {
        return numeroProceso;
    }

    public void setNumeroProceso(String numeroProceso) {
        this.numeroProceso = numeroProceso;
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

    public String getTipoProceso() {
        return tipoProceso;
    }

    public void setTipoProceso(String tipoProceso) {
        this.tipoProceso = tipoProceso;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Jurisdiccion getJurisdiccion() {
        return jurisdiccion;
    }

    public void setJurisdiccion(Jurisdiccion jurisdiccion) {
        this.jurisdiccion = jurisdiccion;
    }

    public Asunto getAsunto() {
        return asunto;
    }

    public void setAsunto(Asunto asunto) {
        this.asunto = asunto;
    }

    public EtapaProceso getEtapaActual() {
        return etapaActual;
    }

    public void setEtapaActual(EtapaProceso etapaActual) {
        this.etapaActual = etapaActual;
    }

    public TipoProcedimiento getTipoProcedimiento() {
        return tipoProcedimiento;
    }

    public void setTipoProcedimiento(TipoProcedimiento tipoProcedimiento) {
        this.tipoProcedimiento = tipoProcedimiento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getAbogadoResponsable() {
        return abogadoResponsable;
    }

    public void setAbogadoResponsable(Usuario abogadoResponsable) {
        this.abogadoResponsable = abogadoResponsable;
    }

    public String getJuzgado() {
        return juzgado;
    }

    public void setJuzgado(String juzgado) {
        this.juzgado = juzgado;
    }

    public String getRadicado() {
        return radicado;
    }

    public void setRadicado(String radicado) {
        this.radicado = radicado;
    }

    public String getDemandante() {
        return demandante;
    }

    public void setDemandante(String demandante) {
        this.demandante = demandante;
    }

    public String getDemandado() {
        return demandado;
    }

    public void setDemandado(String demandado) {
        this.demandado = demandado;
    }

    public BigDecimal getCuantia() {
        return cuantia;
    }

    public void setCuantia(BigDecimal cuantia) {
        this.cuantia = cuantia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Usuario getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Usuario createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Expediente> getExpedientes() {
        return expedientes;
    }

    public void setExpedientes(Set<Expediente> expedientes) {
        this.expedientes = expedientes;
    }

    public Set<Evento> getEventos() {
        return eventos;
    }

    public void setEventos(Set<Evento> eventos) {
        this.eventos = eventos;
    }

    public Set<DocumentoContable> getDocumentosContables() {
        return documentosContables;
    }

    public void setDocumentosContables(Set<DocumentoContable> documentosContables) {
        this.documentosContables = documentosContables;
    }

    public Set<Sentencia> getSentencias() {
        return sentencias;
    }

    public void setSentencias(Set<Sentencia> sentencias) {
        this.sentencias = sentencias;
    }

    public Set<ProcesoEtapa> getEtapas() {
        return etapas;
    }

    public void setEtapas(Set<ProcesoEtapa> etapas) {
        this.etapas = etapas;
    }

    
}