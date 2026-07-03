package com.bufete.backend.utils;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.bufete.backend.Dtos.expediente.CreateExpedienteRequest;
import com.bufete.backend.Dtos.expediente.ExpedienteDTO;
import com.bufete.backend.model.Expediente;

@Mapper(componentModel = "spring")
public interface ExpedienteMapper {
    
    @Mapping(target = "procesoNombre", source = "proceso.nombre")
    @Mapping(target = "procesoNumero", source = "proceso.numeroProceso")
    @Mapping(target = "procesoId", source = "proceso.id")
    @Mapping(target = "createdByNombre", source = "createdBy.nombre")
    ExpedienteDTO toDTO(Expediente expediente);
    
    List<ExpedienteDTO> toDTOList(List<Expediente> expedientes);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "proceso", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaCierre", ignore = true)
    @Mapping(target = "rootNodeId", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "nodes", ignore = true)
    @Mapping(target = "eventos", ignore = true)
    @Mapping(target = "sentencias", ignore = true)
    Expediente toEntity(CreateExpedienteRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "proceso", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "rootNodeId", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget Expediente expediente, CreateExpedienteRequest request);
}
