package com.bufete.backend.Dtos.juridico;

import java.util.List;
import lombok.Data;

@Data
public class CrearAsuntoPenalRequest {
    private Integer jurisdiccionId;
    private Integer asuntoId;
    private Integer etapaId;
    private Integer tipoProcedimientoId;
    private boolean huboVictimas;


    private String etapaProceso;
    private String competenciaAsunto;
    private String tipoJuzgado;

    private String nunc;
    private List<PersonaRequest> imputados;
    private List<PersonaRequest> victimas;
    private List<Integer> delitosIds;
    private List<Integer> bienesJuridicosIds;
    private String imputacion;
    private FiscalRequest fiscal;
    private List<JuzgadoRequest> juzgados;
}