package com.bufete.backend.utils;

import com.bufete.backend.Dtos.proceso.CreateProcesoRequest;
import com.bufete.backend.Dtos.proceso.ProcesoDTO;
import com.bufete.backend.model.Cliente;
import com.bufete.backend.model.Proceso;
import com.bufete.backend.model.Usuario;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-14T16:57:52-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251023-0518, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class ProcesoMapperImpl implements ProcesoMapper {

    @Override
    public ProcesoDTO toDTO(Proceso proceso) {
        if ( proceso == null ) {
            return null;
        }

        ProcesoDTO.ProcesoDTOBuilder procesoDTO = ProcesoDTO.builder();

        procesoDTO.clienteNombre( procesoClienteNombre( proceso ) );
        procesoDTO.createdByNombre( procesoCreatedByNombre( proceso ) );
        procesoDTO.activo( proceso.getActivo() );
        procesoDTO.createdAt( proceso.getCreatedAt() );
        procesoDTO.cuantia( proceso.getCuantia() );
        procesoDTO.demandado( proceso.getDemandado() );
        procesoDTO.demandante( proceso.getDemandante() );
        procesoDTO.descripcion( proceso.getDescripcion() );
        procesoDTO.estado( proceso.getEstado() );
        procesoDTO.fechaCierre( proceso.getFechaCierre() );
        procesoDTO.fechaCreacion( proceso.getFechaCreacion() );
        procesoDTO.fechaInicio( proceso.getFechaInicio() );
        procesoDTO.id( proceso.getId() );
        procesoDTO.juzgado( proceso.getJuzgado() );
        procesoDTO.nombre( proceso.getNombre() );
        procesoDTO.numeroProceso( proceso.getNumeroProceso() );
        procesoDTO.observaciones( proceso.getObservaciones() );
        procesoDTO.radicado( proceso.getRadicado() );
        procesoDTO.tipoProceso( proceso.getTipoProceso() );
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

        proceso.cuantia( request.getCuantia() );
        proceso.demandado( request.getDemandado() );
        proceso.demandante( request.getDemandante() );
        proceso.descripcion( request.getDescripcion() );
        proceso.fechaInicio( request.getFechaInicio() );
        proceso.juzgado( request.getJuzgado() );
        proceso.nombre( request.getNombre() );
        proceso.numeroProceso( request.getNumeroProceso() );
        proceso.observaciones( request.getObservaciones() );
        proceso.radicado( request.getRadicado() );
        proceso.tipoProceso( request.getTipoProceso() );

        return proceso.build();
    }

    @Override
    public void updateEntity(Proceso proceso, CreateProcesoRequest request) {
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
        if ( request.getFechaInicio() != null ) {
            proceso.setFechaInicio( request.getFechaInicio() );
        }
        if ( request.getJuzgado() != null ) {
            proceso.setJuzgado( request.getJuzgado() );
        }
        if ( request.getRadicado() != null ) {
            proceso.setRadicado( request.getRadicado() );
        }
        if ( request.getDemandante() != null ) {
            proceso.setDemandante( request.getDemandante() );
        }
        if ( request.getDemandado() != null ) {
            proceso.setDemandado( request.getDemandado() );
        }
        if ( request.getCuantia() != null ) {
            proceso.setCuantia( request.getCuantia() );
        }
        if ( request.getObservaciones() != null ) {
            proceso.setObservaciones( request.getObservaciones() );
        }
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
}
