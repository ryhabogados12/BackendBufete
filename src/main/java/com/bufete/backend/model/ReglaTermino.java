package com.bufete.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "regla_termino", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo_procedimiento_id", "tipo_actuacion_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReglaTermino {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "tipo_procedimiento_id", nullable = false) private TipoProcedimiento tipoProcedimiento;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "tipo_actuacion_id", nullable = false) private TipoActuacion tipoActuacion;
    @Column(name = "dias", nullable = false) private Integer dias;
    @Column(name = "unidad", nullable = false, length = 20) private String unidad = "DIAS";
    @Column(name = "titulo_evento", nullable = false, length = 255) private String tituloEvento;
    @Column(name = "descripcion_evento") private String descripcionEvento;
    @Column(name = "activo", nullable = false) private Boolean activo = true;
}