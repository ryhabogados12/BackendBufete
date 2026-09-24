package com.bufete.backend.Dtos.juridico;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CatalogoDTO {
    private Integer id;
    private String codigo;
    private String nombre;
    private String descripcion;
}