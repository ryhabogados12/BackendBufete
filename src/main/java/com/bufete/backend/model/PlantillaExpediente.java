package com.bufete.backend.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "plantilla_expediente", uniqueConstraints = @UniqueConstraint(columnNames = {"asunto_id", "nombre"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PlantillaExpediente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "asunto_id", nullable = false) private Asunto asunto;
    @Column(name = "nombre", nullable = false, length = 255) private String nombre;
    private String descripcion;
    @Column(name = "activo", nullable = false) private Boolean activo = true;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}