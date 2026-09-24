package com.bufete.backend.utils;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.bufete.backend.Dtos.folder.NodeBasicDTO;
import com.bufete.backend.Dtos.folder.NodeDTO;
import com.bufete.backend.model.Node;

@Mapper(componentModel = "spring")
public interface NodeMapper {
    
    @Mapping(target = "expedienteNombre", source = "expediente.nombre")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "createdByNombre", source = "createdBy.nombre")
    NodeDTO toDTO(Node node);
    
    NodeBasicDTO toBasicDTO(Node node);
    
    List<NodeDTO> toDTOList(List<Node> nodes);
}