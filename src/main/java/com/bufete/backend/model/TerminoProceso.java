package com.bufete.backend.model;

import java.time.Instant;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "termino_proceso", uniqueConstraints = @UniqueConstraint(columnNames = {"actuacion_id", "regla_termino_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TerminoProceso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "actuacion_id", nullable = false) private ProcesoActuacion actuacion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "regla_termino_id", nullable = false) private ReglaTermino reglaTermino;
    @Column(name = "fecha_inicio", nullable = false) private LocalDate fechaInicio;
    @Column(name = "dias_plazo", nullable = false) private Integer diasPlazo;
    @Column(name = "fecha_vencimiento", nullable = false) private LocalDate fechaVencimiento;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "evento_id") private Evento evento;
    @Column(name = "calculado_automaticamente", nullable = false) private Boolean calculadoAutomaticamente = true;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}