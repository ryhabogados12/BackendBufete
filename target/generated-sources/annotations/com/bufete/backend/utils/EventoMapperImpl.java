package com.bufete.backend.utils;

import com.bufete.backend.Dtos.evento.CreateEventoRequest;
import com.bufete.backend.Dtos.evento.EventoDTO;
import com.bufete.backend.model.Evento;
import com.bufete.backend.model.Expediente;
import com.bufete.backend.model.Proceso;
import com.bufete.backend.model.TipoEvento;
import com.bufete.backend.model.Usuario;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-07T12:39:13-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class EventoMapperImpl implements EventoMapper {

    @Override
    public EventoDTO toDTO(Evento evento) {
        if ( evento == null ) {
            return null;
        }

        EventoDTO.EventoDTOBuilder eventoDTO = EventoDTO.builder();

        eventoDTO.tipoEventoId( eventoTipoEventoId( evento ) );
        eventoDTO.tipoEventoNombre( eventoTipoEventoNombre( evento ) );
        eventoDTO.tipoEventoColor( eventoTipoEventoColor( evento ) );
        eventoDTO.procesoId( eventoProcesoId( evento ) );
        eventoDTO.procesoNombre( eventoProcesoNombre( evento ) );
        eventoDTO.expedienteId( eventoExpedienteId( evento ) );
        eventoDTO.expedienteNombre( eventoExpedienteNombre( evento ) );
        eventoDTO.responsableId( eventoResponsableId( evento ) );
        eventoDTO.allDay( evento.getAllDay() );
        eventoDTO.createdAt( evento.getCreatedAt() );
        eventoDTO.descripcion( evento.getDescripcion() );
        eventoDTO.fechaFin( evento.getFechaFin() );
        eventoDTO.fechaInicio( evento.getFechaInicio() );
        eventoDTO.id( evento.getId() );
        eventoDTO.titulo( evento.getTitulo() );
        eventoDTO.updatedAt( evento.getUpdatedAt() );

        eventoDTO.responsableNombre( getFullName(evento.getResponsable().getNombre(), evento.getResponsable().getApellido()) );

        return eventoDTO.build();
    }

    @Override
    public List<EventoDTO> toDTOList(List<Evento> eventos) {
        if ( eventos == null ) {
            return null;
        }

        List<EventoDTO> list = new ArrayList<EventoDTO>( eventos.size() );
        for ( Evento evento : eventos ) {
            list.add( toDTO( evento ) );
        }

        return list;
    }

    @Override
    public Evento toEntity(CreateEventoRequest request) {
        if ( request == null ) {
            return null;
        }

        Evento.EventoBuilder evento = Evento.builder();

        evento.allDay( request.isAllDay() );
        evento.descripcion( request.getDescripcion() );
        evento.fechaFin( request.getFechaFin() );
        evento.fechaInicio( request.getFechaInicio() );
        evento.titulo( request.getTitulo() );

        return evento.build();
    }

    @Override
    public void updateEntity(Evento evento, CreateEventoRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getTitulo() != null ) {
            evento.setTitulo( request.getTitulo() );
        }
        if ( request.getDescripcion() != null ) {
            evento.setDescripcion( request.getDescripcion() );
        }
        if ( request.getFechaInicio() != null ) {
            evento.setFechaInicio( request.getFechaInicio() );
        }
        if ( request.getFechaFin() != null ) {
            evento.setFechaFin( request.getFechaFin() );
        }
        evento.setAllDay( request.isAllDay() );
    }

    @Override
    public Evento toEntity(EventoDTO eventoDTO) {
        if ( eventoDTO == null ) {
            return null;
        }

        Evento.EventoBuilder evento = Evento.builder();

        evento.allDay( eventoDTO.getAllDay() );
        evento.descripcion( eventoDTO.getDescripcion() );
        evento.fechaFin( eventoDTO.getFechaFin() );
        evento.fechaInicio( eventoDTO.getFechaInicio() );
        evento.id( eventoDTO.getId() );
        evento.titulo( eventoDTO.getTitulo() );

        return evento.build();
    }

    private Long eventoTipoEventoId(Evento evento) {
        if ( evento == null ) {
            return null;
        }
        TipoEvento tipoEvento = evento.getTipoEvento();
        if ( tipoEvento == null ) {
            return null;
        }
        Long id = tipoEvento.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String eventoTipoEventoNombre(Evento evento) {
        if ( evento == null ) {
            return null;
        }
        TipoEvento tipoEvento = evento.getTipoEvento();
        if ( tipoEvento == null ) {
            return null;
        }
        String nombre = tipoEvento.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    private String eventoTipoEventoColor(Evento evento) {
        if ( evento == null ) {
            return null;
        }
        TipoEvento tipoEvento = evento.getTipoEvento();
        if ( tipoEvento == null ) {
            return null;
        }
        String color = tipoEvento.getColor();
        if ( color == null ) {
            return null;
        }
        return color;
    }

    private Long eventoProcesoId(Evento evento) {
        if ( evento == null ) {
            return null;
        }
        Proceso proceso = evento.getProceso();
        if ( proceso == null ) {
            return null;
        }
        Long id = proceso.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String eventoProcesoNombre(Evento evento) {
        if ( evento == null ) {
            return null;
        }
        Proceso proceso = evento.getProceso();
        if ( proceso == null ) {
            return null;
        }
        String nombre = proceso.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    private Long eventoExpedienteId(Evento evento) {
        if ( evento == null ) {
            return null;
        }
        Expediente expediente = evento.getExpediente();
        if ( expediente == null ) {
            return null;
        }
        Long id = expediente.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String eventoExpedienteNombre(Evento evento) {
        if ( evento == null ) {
            return null;
        }
        Expediente expediente = evento.getExpediente();
        if ( expediente == null ) {
            return null;
        }
        String nombre = expediente.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    private Long eventoResponsableId(Evento evento) {
        if ( evento == null ) {
            return null;
        }
        Usuario responsable = evento.getResponsable();
        if ( responsable == null ) {
            return null;
        }
        Long id = responsable.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
