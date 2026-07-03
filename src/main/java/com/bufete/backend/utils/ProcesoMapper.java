package com.bufete.backend.utils;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.bufete.backend.Dtos.proceso.CreateProcesoRequest;
import com.bufete.backend.Dtos.proceso.EditProcesoRequest;
import com.bufete.backend.Dtos.proceso.ProcesoDTO;
import com.bufete.backend.model.Proceso;

@Mapper(componentModel = "spring")
public interface ProcesoMapper {
    
    @Mapping(target = "clienteId", source = "cliente.identificacion")
    @Mapping(target = "clienteNombre", source = "cliente.nombre")
    @Mapping(target = "abogadoResponsableId", source = "abogadoResponsable.id")
    @Mapping(target = "abogadoResponsableNombre", 
             expression = "java(proceso.getAbogadoResponsable().getNombre() + \" \" + proceso.getAbogadoResponsable().getApellido())")
    @Mapping(target = "createdByNombre", source = "createdBy.nombre")
    ProcesoDTO toDTO(Proceso proceso);
    
    @Mapping(target = "clienteId", source = "cliente.identificacion")
    @Mapping(target = "abogadoResponsableId", source = "abogadoResponsable.id")
    @Mapping(target = "abogadoResponsableNombre", 
             expression = "java(proceso.getAbogadoResponsable().getNombre() + \" \" + proceso.getAbogadoResponsable().getApellido())")
    List<ProcesoDTO> toDTOList(List<Proceso> procesos);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "abogadoResponsable", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "expedientes", ignore = true)
    @Mapping(target = "eventos", ignore = true)
    @Mapping(target = "documentosContables", ignore = true)
    @Mapping(target = "sentencias", ignore = true)
    Proceso toEntity(CreateProcesoRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "abogadoResponsable", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Proceso proceso, EditProcesoRequest request);
}