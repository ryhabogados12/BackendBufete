package com.bufete.backend.Dtos.juridico;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActuacionDTO {
    private Long id;
    private Long procesoId;
    private Long expedienteId;
    private Integer tipoActuacionId;
    private String tipoActuacionCodigo;
    private String tipoActuacionNombre;
    private LocalDate fechaActuacion;
    private String contexto;
    private String observaciones;
}