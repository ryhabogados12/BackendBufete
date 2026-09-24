package com.bufete.backend.model;

import java.io.Serializable;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ProcesoPenalDelitoId implements Serializable {
    private Long procesoId;
    private Integer delitoId;
}