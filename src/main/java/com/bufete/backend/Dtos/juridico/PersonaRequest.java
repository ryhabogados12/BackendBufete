package com.bufete.backend.Dtos.juridico;

import java.time.LocalDate;
import lombok.Data;

@Data
public class PersonaRequest {
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String lugarResidencia;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private Boolean privadoLibertad;
    private String tipoDetencion;
    private String establecimientoOResidencia;
}