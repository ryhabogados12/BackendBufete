package com.bufete.backend.model;

import java.time.Instant;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "recurso_extraordinario", uniqueConstraints = @UniqueConstraint(columnNames = "expediente_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RecursoExtraordinario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "proceso_id", nullable = false) private Proceso proceso;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "expediente_id", nullable = false, unique = true) private Expediente expediente;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "abogado_id", nullable = false) private Usuario abogado;
    private String contexto;
    @Column(name = "fecha_presentacion") private LocalDate fechaPresentacion;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "documento_node_id") private Node documentoNode;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private Usuario createdBy;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}