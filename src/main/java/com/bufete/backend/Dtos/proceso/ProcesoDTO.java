package com.bufete.backend.Dtos.proceso;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.bufete.backend.model.Proceso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcesoDTO {
    private Long id;
    
    @NotBlank(message = "El número de proceso es obligatorio")
    private String numeroProceso;
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String descripcion;
    private String tipoProceso;
    private Proceso.EstadoProceso estado;
    private Instant fechaCreacion;
    private LocalDate fechaInicio;
    private LocalDate fechaCierre;
    @NotNull(message = "El cliente es obligatorio")
    private String clienteId;
    private String clienteNombre;
    @NotNull(message = "El abogado responsable es obligatorio")
    private Long abogadoResponsableId;
    private String abogadoResponsableNombre;
    private String juzgado;
    private String radicado;
    private String demandante;
    private String demandado;
    private BigDecimal cuantia;
    private String observaciones;
    private Boolean activo;
    private String createdByNombre;
    private Instant createdAt;
    private Instant updatedAt;
    // Campos adicionales para vista
    private int totalExpedientes;
    private int totalEventos;
    
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
    public Proceso.EstadoProceso getEstado() {
        return estado;
    }
    public void setEstado(Proceso.EstadoProceso estado) {
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
    public String getClienteId() {
        return clienteId;
    }
    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }
    public String getClienteNombre() {
        return clienteNombre;
    }
    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }
    public Long getAbogadoResponsableId() {
        return abogadoResponsableId;
    }
    public void setAbogadoResponsableId(Long abogadoResponsableId) {
        this.abogadoResponsableId = abogadoResponsableId;
    }
    public String getAbogadoResponsableNombre() {
        return abogadoResponsableNombre;
    }
    public void setAbogadoResponsableNombre(String abogadoResponsableNombre) {
        this.abogadoResponsableNombre = abogadoResponsableNombre;
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
    public String getCreatedByNombre() {
        return createdByNombre;
    }
    public void setCreatedByNombre(String createdByNombre) {
        this.createdByNombre = createdByNombre;
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
    public int getTotalExpedientes() {
        return totalExpedientes;
    }
    public void setTotalExpedientes(int totalExpedientes) {
        this.totalExpedientes = totalExpedientes;
    }
    public int getTotalEventos() {
        return totalEventos;
    }
    public void setTotalEventos(int totalEventos) {
        this.totalEventos = totalEventos;
    }

    
}
