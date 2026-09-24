package com.bufete.backend.Dtos.juridico;

import java.time.LocalDate;
import lombok.Data;

@Data
public class CrearActuacionRequest {
    private Long expedienteId;
    private Integer tipoActuacionId;
    private LocalDate fechaActuacion;
    private String contexto;
    private String observaciones;
}