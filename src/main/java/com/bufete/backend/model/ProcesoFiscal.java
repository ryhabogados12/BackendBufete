package com.bufete.backend.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "proceso_fiscal")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProcesoFiscal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "proceso_id", nullable = false, unique = true) private Proceso proceso;
    @Column(name = "nombre_fiscal", length = 255) private String nombreFiscal;
    @Column(name = "unidad", length = 30) private String unidad;
    @Column(name = "numero_fiscalia", length = 100) private String numeroFiscalia;
    @Column(name = "correo", length = 200) private String correo;
    @Column(name = "telefono", length = 30) private String telefono;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}