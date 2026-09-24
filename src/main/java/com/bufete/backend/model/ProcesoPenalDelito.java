package com.bufete.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "proceso_penal_delito") @IdClass(ProcesoPenalDelitoId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProcesoPenalDelito {
    @Id @Column(name = "proceso_id") private Long procesoId;
    @Id @Column(name = "delito_id") private Integer delitoId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "proceso_id", insertable = false, updatable = false) private ProcesoPenal procesoPenal;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "delito_id", insertable = false, updatable = false) private Delito delito;
    @Column(name = "descripcion") private String descripcion;
}