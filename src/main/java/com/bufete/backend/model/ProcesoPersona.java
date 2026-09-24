package com.bufete.backend.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "proceso_persona")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProcesoPersona {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "proceso_id", nullable = false) private Proceso proceso;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "persona_id", nullable = false) private Persona persona;
    @Column(name = "rol", nullable = false, length = 30) private String rol;
    @Column(name = "orden", nullable = false) private Integer orden = 1;
    @Column(name = "privado_libertad") private Boolean privadoLibertad;
    @Column(name = "tipo_detencion", length = 80) private String tipoDetencion;
    @Column(name = "establecimiento_o_residencia") private String establecimientoOResidencia;
    private String observaciones;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}