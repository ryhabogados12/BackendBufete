package com.bufete.backend.Dtos.juridico;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecursoExtraordinarioDTO {
    private Long id;
    private Long procesoId;
    private Long expedienteId;
    private Long abogadoId;
    private String contexto;
    private LocalDate fechaPresentacion;
    private UUID documentoNodeId;
}