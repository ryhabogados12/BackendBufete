package com.bufete.backend.Dtos.cliente;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
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
public class CreateClienteRequest {

    public CreateClienteRequest(String identificacion) {
        this.tipoCliente = ClienteDTO.TipoCliente.NATURAL;
        this.nombre = "N/A";
        this.identificacion = identificacion;
        this.tipoDocumento = "CC";
    }
    @NotNull(message = "El tipo de cliente es obligatorio")
    private ClienteDTO.TipoCliente tipoCliente;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    private String apellido;
    private String razonSocial;
    
    @NotBlank(message = "La identificación es obligatoria")
    private String identificacion;
    
    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;
    
    private String email;
    
    private String telefono;
    private String direccion;
    private String ciudad;
    private String departamento;
    private String pais;
    private LocalDate fechaNacimiento;
    private String representanteLegal;
    private String observaciones;
}
