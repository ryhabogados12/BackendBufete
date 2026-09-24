package com.bufete.backend.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "proceso_juzgado", uniqueConstraints = @UniqueConstraint(columnNames = {"proceso_id", "tipo_juzgado"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProcesoJuzgado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "proceso_id", nullable = false) private Proceso proceso;
    @Column(name = "tipo_juzgado", length = 30) private String tipoJuzgado;
    @Column(name = "nombre_juzgado", length = 300) private String nombreJuzgado;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}