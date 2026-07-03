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
public class ClienteDTO {
    private Long id;
    private TipoCliente tipoCliente;

    
    
    public ClienteDTO(String identificacion) {
        this.nombre = "N/A";
        this.identificacion = identificacion;
        this.tipoDocumento = "CC";
        this.email = "N@A";
    }

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    private String apellido;
    private String razonSocial;
    
    @NotBlank(message = "La identificación es obligatoria")
    private String identificacion;
    
    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;
    
    @Email(message = "El formato del email no es válido")
    private String email;
    
    private String telefono;
    private String direccion;
    private String ciudad;
    private String departamento;
    private String pais;
    private LocalDate fechaNacimiento;
    private String representanteLegal;
    private Boolean activo;
    private String observaciones;
    private String createdByNombre;
    private Instant createdAt;
    private Instant updatedAt;
    
    public enum TipoCliente {
        NATURAL, JURIDICA
    }
}