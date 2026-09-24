package com.bufete.backend.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "actuacion_documento", uniqueConstraints = @UniqueConstraint(columnNames = {"actuacion_id", "node_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ActuacionDocumento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "actuacion_id", nullable = false) private ProcesoActuacion actuacion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "node_id", nullable = false) private Node node;
    @Column(name = "es_principal", nullable = false) private Boolean esPrincipal = false;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}