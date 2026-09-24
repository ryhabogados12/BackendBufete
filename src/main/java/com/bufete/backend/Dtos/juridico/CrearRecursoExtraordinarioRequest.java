package com.bufete.backend.Dtos.juridico;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class CrearRecursoExtraordinarioRequest {
    private Long expedienteId;
    private String contexto;
    private LocalDate fechaPresentacion;
    private UUID documentoNodeId;
}