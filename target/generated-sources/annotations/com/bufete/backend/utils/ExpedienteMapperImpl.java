package com.bufete.backend.utils;

import com.bufete.backend.Dtos.expediente.CreateExpedienteRequest;
import com.bufete.backend.Dtos.expediente.ExpedienteDTO;
import com.bufete.backend.model.Expediente;
import com.bufete.backend.model.Proceso;
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
public class ExpedienteMapperImpl implements ExpedienteMapper {

    @Override
    public ExpedienteDTO toDTO(Expediente expediente) {
        if ( expediente == null ) {
            return null;
        }

        ExpedienteDTO.ExpedienteDTOBuilder expedienteDTO = ExpedienteDTO.builder();

        expedienteDTO.procesoNombre( expedienteProcesoNombre( expediente ) );
        expedienteDTO.procesoNumero( expedienteProcesoNumeroProceso( expediente ) );
        expedienteDTO.procesoId( expedienteProcesoId( expediente ) );
        expedienteDTO.createdByNombre( expedienteCreatedByNombre( expediente ) );
        expedienteDTO.id( expediente.getId() );
        expedienteDTO.nombre( expediente.getNombre() );
        expedienteDTO.descripcion( expediente.getDescripcion() );
        expedienteDTO.estado( expediente.getEstado() );
        expedienteDTO.fechaCreacion( expediente.getFechaCreacion() );
        expedienteDTO.fechaCierre( expediente.getFechaCierre() );
        expedienteDTO.rootNodeId( expediente.getRootNodeId() );
        expedienteDTO.orden( expediente.getOrden() );
        expedienteDTO.updatedAt( expediente.getUpdatedAt() );

        return expedienteDTO.build();
    }

    @Override
    public List<ExpedienteDTO> toDTOList(List<Expediente> expedientes) {
        if ( expedientes == null ) {
            return null;
        }

        List<ExpedienteDTO> list = new ArrayList<ExpedienteDTO>( expedientes.size() );
        for ( Expediente expediente : expedientes ) {
            list.add( toDTO( expediente ) );
        }

        return list;
    }

    @Override
    public Expediente toEntity(CreateExpedienteRequest request) {
        if ( request == null ) {
            return null;
        }

        Expediente.ExpedienteBuilder expediente = Expediente.builder();

        expediente.nombre( request.getNombre() );
        expediente.descripcion( request.getDescripcion() );
        expediente.orden( request.getOrden() );

        return expediente.build();
    }

    @Override
    public void updateEntity(Expediente expediente, CreateExpedienteRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getNombre() != null ) {
            expediente.setNombre( request.getNombre() );
        }
        if ( request.getDescripcion() != null ) {
            expediente.setDescripcion( request.getDescripcion() );
        }
        if ( request.getOrden() != null ) {
            expediente.setOrden( request.getOrden() );
        }
    }

    private String expedienteProcesoNombre(Expediente expediente) {
        if ( expediente == null ) {
            return null;
        }
        Proceso proceso = expediente.getProceso();
        if ( proceso == null ) {
            return null;
        }
        String nombre = proceso.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    private String expedienteProcesoNumeroProceso(Expediente expediente) {
        if ( expediente == null ) {
            return null;
        }
        Proceso proceso = expediente.getProceso();
        if ( proceso == null ) {
            return null;
        }
        String numeroProceso = proceso.getNumeroProceso();
        if ( numeroProceso == null ) {
            return null;
        }
        return numeroProceso;
    }

    private Long expedienteProcesoId(Expediente expediente) {
        if ( expediente == null ) {
            return null;
        }
        Proceso proceso = expediente.getProceso();
        if ( proceso == null ) {
            return null;
        }
        Long id = proceso.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String expedienteCreatedByNombre(Expediente expediente) {
        if ( expediente == null ) {
            return null;
        }
        Usuario createdBy = expediente.getCreatedBy();
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
