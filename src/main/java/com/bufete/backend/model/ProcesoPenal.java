package com.bufete.backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "proceso_penal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoPenal {
    @Id
    private Long procesoId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @Column(name = "nunc", unique = true, length = 100)
    private String nunc;

    @Column(name = "etapa_proceso", length = 255)
    private String etapaProceso;

    @Column(name = "competencia_asunto", length = 255)
    private String competenciaAsunto;

    @Column(name = "tipo_juzgado", length = 255)
    private String tipoJuzgado;

    @Column(name = "hubo_victimas", nullable = false)
    private Boolean huboVictimas = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}