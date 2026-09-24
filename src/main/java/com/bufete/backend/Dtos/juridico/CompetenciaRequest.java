package com.bufete.backend.Dtos.juridico;

import lombok.Data;

@Data
public class CompetenciaRequest {
    private String municipal;
    private String circuito;
    private String circuitoEspecializado;
}