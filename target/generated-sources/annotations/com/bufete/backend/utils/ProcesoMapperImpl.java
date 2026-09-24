package com.bufete.backend.utils;

import com.bufete.backend.Dtos.proceso.CreateProcesoRequest;
import com.bufete.backend.Dtos.proceso.EditProcesoRequest;
import com.bufete.backend.Dtos.proceso.ProcesoDTO;
import com.bufete.backend.model.Asunto;
import com.bufete.backend.model.Cliente;
import com.bufete.backend.model.EtapaProceso;
import com.bufete.backend.model.Jurisdiccion;
import com.bufete.backend.model.Proceso;
import com.bufete.backend.model.TipoProcedimiento;
import com.bufete.backend.model.Usuario;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-24T12:18:50-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class ProcesoMapperImpl implements ProcesoMapper {

    @Override
    public ProcesoDTO toDTO(Proceso proceso) {
        if ( proceso == null ) {
            return null;
        }

        ProcesoDTO.ProcesoDTOBuilder procesoDTO = ProcesoDTO.builder();

        procesoDTO.clienteId( procesoClienteIdentificacion( proceso ) );
        procesoDTO.clienteNombre( procesoClienteNombre( proceso ) );
        procesoDTO.abogadoResponsableId( procesoAbogadoResponsableId( proceso ) );
        procesoDTO.createdByNombre( procesoCreatedByNombre( proceso ) );
        procesoDTO.jurisdiccionId( procesoJurisdiccionId( proceso ) );
        procesoDTO.asuntoId( procesoAsuntoId( proceso ) );
        procesoDTO.etapaActualId( procesoEtapaActualId( proceso ) );
        procesoDTO.tipoProcedimientoId( procesoTipoProcedimientoId( proceso ) );
        procesoDTO.id( proceso.getId() );
        procesoDTO.numeroProceso( proceso.getNumeroProceso() );
        procesoDTO.nombre( proceso.getNombre() );
        procesoDTO.descripcion( proceso.getDescripcion() );
        procesoDTO.tipoProceso( proceso.getTipoProceso() );
        procesoDTO.estado( proceso.getEstado() );
        procesoDTO.fechaCreacion( proceso.getFechaCreacion() );
        procesoDTO.fechaInicio( proceso.getFechaInicio() );
        procesoDTO.fechaCierre( proceso.getFechaCierre() );
        procesoDTO.juzgado( proceso.getJuzgado() );
        procesoDTO.radicado( proceso.getRadicado() );
        procesoDTO.demandante( proceso.getDemandante() );
        procesoDTO.demandado( proceso.getDemandado() );
        procesoDTO.cuantia( proceso.getCuantia() );
        procesoDTO.observaciones( proceso.getObservaciones() );
        procesoDTO.activo( proceso.getActivo() );
        procesoDTO.createdAt( proceso.getCreatedAt() );
        procesoDTO.updatedAt( proceso.getUpdatedAt() );

        procesoDTO.abogadoResponsableNombre( proceso.getAbogadoResponsable().getNombre() + " " + proceso.getAbogadoResponsable().getApellido() );

        return procesoDTO.build();
    }

    @Override
    public List<ProcesoDTO> toDTOList(List<Proceso> procesos) {
        if ( procesos == null ) {
            return null;
        }

        List<ProcesoDTO> list = new ArrayList<ProcesoDTO>( procesos.size() );
        for ( Proceso proceso : procesos ) {
            list.add( toDTO( proceso ) );
        }

        return list;
    }

    @Override
    public Proceso toEntity(CreateProcesoRequest request) {
        if ( request == null ) {
            return null;
        }

        Proceso.ProcesoBuilder proceso = Proceso.builder();

        proceso.numeroProceso( request.getNumeroProceso() );
        proceso.nombre( request.getNombre() );
        proceso.descripcion( request.getDescripcion() );
        proceso.tipoProceso( request.getTipoProceso() );
        proceso.fechaInicio( request.getFechaInicio() );
        proceso.juzgado( request.getJuzgado() );
        proceso.radicado( request.getRadicado() );
        proceso.demandante( request.getDemandante() );
        proceso.demandado( request.getDemandado() );
        proceso.cuantia( request.getCuantia() );
        proceso.observaciones( request.getObservaciones() );

        return proceso.build();
    }

    @Override
    public void updateEntity(Proceso proceso, EditProcesoRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getNumeroProceso() != null ) {
            proceso.setNumeroProceso( request.getNumeroProceso() );
        }
        if ( request.getNombre() != null ) {
            proceso.setNombre( request.getNombre() );
        }
        if ( request.getDescripcion() != null ) {
            proceso.setDescripcion( request.getDescripcion() );
        }
        if ( request.getTipoProceso() != null ) {
            proceso.setTipoProceso( request.getTipoProceso() );
        }
    }

    private String procesoClienteIdentificacion(Proceso proceso) {
        if ( proceso == null ) {
            return null;
        }
        Cliente cliente = proceso.getCliente();
        if ( cliente == null ) {
            return null;
        }
        String identificacion = cliente.getIdentificacion();
        if ( identificacion == null ) {
            return null;
        }
        return identificacion;
    }

    private String procesoClienteNombre(Proceso proceso) {
        if ( proceso == null ) {
            return null;
        }
        Cliente cliente = proceso.getCliente();
        if ( cliente == null ) {
            return null;
        }
        String nombre = cliente.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    private Long procesoAbogadoResponsableId(Proceso proceso) {
        if ( proceso == null ) {
            return null;
        }
        Usuario abogadoResponsable = proceso.getAbogadoResponsable();
        if ( abogadoResponsable == null ) {
            return null;
        }
        Long id = abogadoResponsable.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String procesoCreatedByNombre(Proceso proceso) {
        if ( proceso == null ) {
            return null;
        }
        Usuario createdBy = proceso.getCreatedBy();
        if ( createdBy == null ) {
            return null;
        }
        String nombre = createdBy.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    private Integer procesoJurisdiccionId(Proceso proceso) {
        if ( proceso == null ) {
            return null;
        }
        Jurisdiccion jurisdiccion = proceso.getJurisdiccion();
        if ( jurisdiccion == null ) {
            return null;
        }
        Integer id = jurisdiccion.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Integer procesoAsuntoId(Proceso proceso) {
        if ( proceso == null ) {
            return null;
        }
        Asunto asunto = proceso.getAsunto();
        if ( asunto == null ) {
            return null;
        }
        Integer id = asunto.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Integer procesoEtapaActualId(Proceso proceso) {
        if ( proceso == null ) {
            return null;
        }
        EtapaProceso etapaActual = proceso.getEtapaActual();
        if ( etapaActual == null ) {
            return null;
        }
        Integer id = etapaActual.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Integer procesoTipoProcedimientoId(Proceso proceso) {
        if ( proceso == null ) {
            return null;
        }
        TipoProcedimiento tipoProcedimiento = proceso.getTipoProcedimiento();
        if ( tipoProcedimiento == null ) {
            return null;
        }
        Integer id = tipoProcedimiento.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
