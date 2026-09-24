package com.bufete.backend.Dtos.juridico;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TerminoDTO {
    private Long id;
    private Long actuacionId;
    private Integer reglaTerminoId;
    private LocalDate fechaInicio;
    private Integer diasPlazo;
    private LocalDate fechaVencimiento;
    private Long eventoId;
}