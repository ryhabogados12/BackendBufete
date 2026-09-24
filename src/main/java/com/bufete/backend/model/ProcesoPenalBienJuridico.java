package com.bufete.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "proceso_penal_bien_juridico") @IdClass(ProcesoPenalBienJuridicoId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProcesoPenalBienJuridico {
    @Id @Column(name = "proceso_id") private Long procesoId;
    @Id @Column(name = "bien_juridico_id") private Integer bienJuridicoId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "proceso_id", insertable = false, updatable = false) private ProcesoPenal procesoPenal;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "bien_juridico_id", insertable = false, updatable = false) private BienJuridico bienJuridico;
}