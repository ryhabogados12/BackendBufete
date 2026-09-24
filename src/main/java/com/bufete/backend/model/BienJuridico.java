package com.bufete.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "bien_juridico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BienJuridico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(name = "codigo", unique = true, length = 100) private String codigo;
    @Column(name = "nombre", nullable = false, length = 300) private String nombre;
    @Column(name = "descripcion", nullable = false, length = 300) private String descripcion;
    @Column(name = "activo", nullable = false) private Boolean activo = true;
}