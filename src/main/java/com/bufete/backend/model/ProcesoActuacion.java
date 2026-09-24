package com.bufete.backend.model;

import java.time.Instant;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "proceso_actuacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProcesoActuacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "proceso_id", nullable = false) private Proceso proceso;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "expediente_id", nullable = false) private Expediente expediente;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "tipo_actuacion_id", nullable = false) private TipoActuacion tipoActuacion;
    @Column(name = "fecha_actuacion") private LocalDate fechaActuacion;
    private String contexto;
    private String observaciones;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private Usuario createdBy;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}