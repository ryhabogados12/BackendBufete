package com.bufete.backend.Dtos.cliente;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteConsultarDTO {    
    private String nombre;
    private String apellido;
    private String identificacion;
    private String tipoDocumento;
    
}