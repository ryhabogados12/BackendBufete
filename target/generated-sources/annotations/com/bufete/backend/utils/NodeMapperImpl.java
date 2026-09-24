package com.bufete.backend.utils;

import com.bufete.backend.Dtos.folder.NodeBasicDTO;
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
    date = "2026-09-24T12:11:27-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
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
        nodeDTO.id( node.getId() );
        nodeDTO.type( node.getType() );
        nodeDTO.name( node.getName() );
        nodeDTO.description( node.getDescription() );
        nodeDTO.modulo( node.getModulo() );
        nodeDTO.createdAt( node.getCreatedAt() );
        nodeDTO.updatedAt( node.getUpdatedAt() );
        nodeDTO.sizeBytes( node.getSizeBytes() );
        nodeDTO.itemCount( node.getItemCount() );
        nodeDTO.lastAccessed( node.getLastAccessed() );
        nodeDTO.children( nodeSetToNodeDTOList( node.getChildren() ) );

        return nodeDTO.build();
    }

    @Override
    public NodeBasicDTO toBasicDTO(Node node) {
        if ( node == null ) {
            return null;
        }

        NodeBasicDTO.NodeBasicDTOBuilder nodeBasicDTO = NodeBasicDTO.builder();

        nodeBasicDTO.id( node.getId() );
        nodeBasicDTO.type( node.getType() );
        nodeBasicDTO.name( node.getName() );
        nodeBasicDTO.description( node.getDescription() );
        nodeBasicDTO.itemCount( node.getItemCount() );
        nodeBasicDTO.children( nodeSetToNodeDTOList( node.getChildren() ) );

        return nodeBasicDTO.build();
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
