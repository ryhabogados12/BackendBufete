package com.bufete.backend.Dtos.proceso;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.bufete.backend.model.Proceso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditProcesoRequest {
    private Long id;
    
    @NotBlank(message = "El número de proceso es obligatorio.")
    private String numeroProceso;
    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    private String descripcion;

    private String tipoProceso;
    
    @NotNull(message = "El cliente es obligatorio")
    private String clienteId;
    
    private String clienteNombre;

}
