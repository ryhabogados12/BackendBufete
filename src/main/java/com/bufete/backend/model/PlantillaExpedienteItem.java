package com.bufete.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "plantilla_expediente_item", uniqueConstraints = @UniqueConstraint(columnNames = {"plantilla_id", "codigo"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PlantillaExpedienteItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "plantilla_id", nullable = false) private PlantillaExpediente plantilla;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_item_id") private PlantillaExpedienteItem parentItem;
    @Column(name = "codigo", nullable = false, length = 120) private String codigo;
    @Column(name = "nombre", nullable = false, length = 255) private String nombre;
    @Column(name = "orden", nullable = false) private Integer orden = 1;
    @Column(name = "activo", nullable = false) private Boolean activo = true;
}