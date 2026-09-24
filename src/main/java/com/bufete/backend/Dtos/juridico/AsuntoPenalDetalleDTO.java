package com.bufete.backend.Dtos.juridico;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AsuntoPenalDetalleDTO {
    private ProcesoDetalle proceso;
    private ProcesoPenalDetalle procesoPenal;
    private ImputacionDetalle imputacion;
    private List<ParticipanteDetalle> participantes;
    private List<CatalogoRelacionDetalle> delitos;
    private List<CatalogoRelacionDetalle> bienesJuridicos;
    private FiscalDetalle fiscal;
    private List<JuzgadoDetalle> juzgados;
    private List<ExpedienteDetalle> expedientes;
    private List<ActuacionDTO> actuaciones;
    private List<TerminoDTO> terminos;
    private List<EventoDetalle> eventos;

    @Data
    @Builder
    public static class ProcesoDetalle {
        private Long id;
        private String numeroProceso;
        private String estado;

        private LocalDate fechaInicio;
        private LocalDate fechaCierre;

        /* private Integer jurisdiccionId; */
        private String jurisdiccionNombre;
        /* private Integer asuntoId; */
        private String asuntoNombre;
        /* private Integer etapaActualId; */
        private String etapaActualNombre;
        /* private Integer tipoProcedimientoId; */
        private String tipoProcedimientoNorma;
        /* private Long clienteId;
        private Long abogadoResponsableId; */
        private String abogadoResponsableNombre;
    }

    @Data
    @Builder
    public static class ProcesoPenalDetalle {
        private Long procesoId;
        private String nunc;
        private String competenciaMunicipal;
        private Boolean huboVictimas;
    }

    @Data
    @Builder
    public static class ImputacionDetalle {
        private String descripcion;
    }

    @Data
    @Builder
    public static class ParticipanteDetalle {
        private Long id;
        private String rol;
        private Boolean privadoLibertad;
        private String tipoDetencion;
        private String establecimientoOResidencia;
        private Long personaId;
        private String tipoDocumento;
        private String numeroDocumento;
        private String nombres;
        private String apellidos;
        private String lugarResidencia;
        private String email;
        private String telefono;
    }

    @Data
    @Builder
    public static class CatalogoRelacionDetalle {
        private Integer id;
        private String codigo;
        private String nombre;
        private String descripcion;
    }

    @Data
    @Builder
    public static class FiscalDetalle {
        private Long id;
        private String nombreFiscal;
        private String unidad;
        private String correo;
        private String telefono;
    }

    @Data
    @Builder
    public static class JuzgadoDetalle {
        private Long id;
        private String tipoJuzgado;
        private String nombreJuzgado;
    }

    @Data
    @Builder
    public static class ExpedienteDetalle {
        private Long id;
        private String nombre;
        private String estado;
        private UUID rootNodeId;
        private Integer orden;
        private Instant fechaCreacion;
        private Instant fechaCierre;
    }

    @Data
    @Builder
    public static class EventoDetalle {
        private Long id;
        private String titulo;
        private String descripcion;
        private Long tipoEventoId;
        private String tipoEventoNombre;
        private Instant fechaInicio;
        private Instant fechaFin;
        private Boolean allDay;
        private Long expedienteId;
        private Long terminoProcesoId;
    }
}