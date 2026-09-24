package com.bufete.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "tipo_actuacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TipoActuacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "asunto_id") private Asunto asunto;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "etapa_id", nullable = false) private EtapaProceso etapa;
    @Column(name = "codigo", nullable = false, unique = true, length = 120) private String codigo;
    @Column(name = "nombre", nullable = false, length = 300) private String nombre;
    @Column(name = "grupo", nullable = false, length = 40) private String grupo;
    @Column(name = "requiere_fecha", nullable = false) private Boolean requiereFecha = true;
    @Column(name = "permite_archivo", nullable = false) private Boolean permiteArchivo = true;
    @Column(name = "es_repetible", nullable = false) private Boolean esRepetible = false;
    @Column(name = "genera_termino", nullable = false) private Boolean generaTermino = false;
    @Column(name = "orden", nullable = false) private Integer orden = 1;
    @Column(name = "activo", nullable = false) private Boolean activo = true;
}