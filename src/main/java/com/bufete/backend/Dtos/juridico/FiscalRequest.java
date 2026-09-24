package com.bufete.backend.Dtos.juridico;

import lombok.Data;

@Data
public class FiscalRequest {
    private String nombreFiscal;
    private String unidad;
    private String numeroFiscalia;
    private String correo;
    private String telefono;
}