package com.bufete.backend.Dtos.juridico;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcesoPenalResponse {
    private Long procesoId;
    private Long expedienteId;
    private Integer jurisdiccionId;
    private Integer asuntoId;
    private Integer etapaId;
    private Integer tipoProcedimientoId;
    private String nunc;
    private Boolean huboVictimas;
    private List<Long> participantesIds;
    private List<Integer> delitosIds;
    private List<Integer> bienesJuridicosIds;
    private String rootNodeId;
}