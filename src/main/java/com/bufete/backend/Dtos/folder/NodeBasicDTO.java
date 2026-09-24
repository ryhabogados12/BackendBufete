package com.bufete.backend.Dtos.folder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.bufete.backend.model.Node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeBasicDTO {
    private UUID id;
    private String parentName;
    private Node.NodeType type;
    private String name;
    private String description;
    private Integer itemCount;
    
    // Para carpetas
    private List<NodeDTO> children;
    private String path;
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getParentName() {
        return parentName;
    }
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    public Node.NodeType getType() {
        return type;
    }
    public void setType(Node.NodeType type) {
        this.type = type;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }    
    public Integer getItemCount() {
        return itemCount;
    }
    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
    public List<NodeDTO> getChildren() {
        return children;
    }
    public void setChildren(List<NodeDTO> children) {
        this.children = children;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    
}
