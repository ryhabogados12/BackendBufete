package com.bufete.backend.model;

import java.time.Instant;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "proceso_penal_imputacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProcesoPenalImputacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "proceso_id", nullable = false, unique = true) private ProcesoPenal procesoPenal;
    private String descripcion;
    @Column(name = "fecha_imputacion") private LocalDate fechaImputacion;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}