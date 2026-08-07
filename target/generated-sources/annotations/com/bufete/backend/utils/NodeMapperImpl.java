package com.bufete.backend.utils;

import com.bufete.backend.Dtos.folder.NodeDTO;
import com.bufete.backend.model.Expediente;
import com.bufete.backend.model.Node;
import com.bufete.backend.model.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-07T12:39:13-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class NodeMapperImpl implements NodeMapper {

    @Override
    public NodeDTO toDTO(Node node) {
        if ( node == null ) {
            return null;
        }

        NodeDTO.NodeDTOBuilder nodeDTO = NodeDTO.builder();

        nodeDTO.expedienteNombre( nodeExpedienteNombre( node ) );
        nodeDTO.parentName( nodeParentName( node ) );
        nodeDTO.createdByNombre( nodeCreatedByNombre( node ) );
        nodeDTO.children( nodeSetToNodeDTOList( node.getChildren() ) );
        nodeDTO.createdAt( node.getCreatedAt() );
        nodeDTO.description( node.getDescription() );
        nodeDTO.id( node.getId() );
        nodeDTO.itemCount( node.getItemCount() );
        nodeDTO.lastAccessed( node.getLastAccessed() );
        nodeDTO.modulo( node.getModulo() );
        nodeDTO.name( node.getName() );
        nodeDTO.sizeBytes( node.getSizeBytes() );
        nodeDTO.type( node.getType() );
        nodeDTO.updatedAt( node.getUpdatedAt() );

        return nodeDTO.build();
    }

    @Override
    public List<NodeDTO> toDTOList(List<Node> nodes) {
        if ( nodes == null ) {
            return null;
        }

        List<NodeDTO> list = new ArrayList<NodeDTO>( nodes.size() );
        for ( Node node : nodes ) {
            list.add( toDTO( node ) );
        }

        return list;
    }

    private String nodeExpedienteNombre(Node node) {
        if ( node == null ) {
            return null;
        }
        Expediente expediente = node.getExpediente();
        if ( expediente == null ) {
            return null;
        }
        String nombre = expediente.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    private String nodeParentName(Node node) {
        if ( node == null ) {
            return null;
        }
        Node parent = node.getParent();
        if ( parent == null ) {
            return null;
        }
        String name = parent.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String nodeCreatedByNombre(Node node) {
        if ( node == null ) {
            return null;
        }
        Usuario createdBy = node.getCreatedBy();
        if ( createdBy == null ) {
            return null;
        }
        String nombre = createdBy.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    protected List<NodeDTO> nodeSetToNodeDTOList(Set<Node> set) {
        if ( set == null ) {
            return null;
        }

        List<NodeDTO> list = new ArrayList<NodeDTO>( set.size() );
        for ( Node node : set ) {
            list.add( toDTO( node ) );
        }

        return list;
    }
}
