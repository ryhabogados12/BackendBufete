package com.bufete.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "node", indexes = {
    @Index(name = "idx_node_parent", columnList = "parent_id"),
    @Index(name = "idx_node_expediente", columnList = "expediente_id"),
    @Index(name = "idx_node_modulo", columnList = "modulo"),
    @Index(name = "idx_node_type", columnList = "type"),
    @Index(name = "idx_node_name", columnList = "name")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_sibling_name", 
        columnNames = {"parent_id", "name", "expediente_id", "contable_id", "modulo", "is_deleted"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id")
    private Expediente expediente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contable_id")
    private DocumentoContable contable;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Node parent;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private NodeType type;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "modulo", nullable = false, length = 20)
    @Builder.Default
    private Modulo modulo = Modulo.DOCUMENTAL;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Usuario createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private FileVersion currentVersion;

    @Column(name = "plantilla_item_id")
    private Long plantillaItemId;
    
    @Column(name = "size_bytes")
    @Builder.Default
    private Long sizeBytes = 0L;
    
    @Column(name = "item_count")
    @Builder.Default
    private Integer itemCount = 0;
    
    @Column(name = "last_accessed")
    private Instant lastAccessed;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Node> children = new HashSet<>();
    
    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<FileVersion> versions = new HashSet<>();
    
    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<NodePermission> permissions = new HashSet<>();
    
    public enum NodeType {
        FOLDER, FILE
    }
    
    public enum Modulo {
        DOCUMENTAL, CONTABLE, PLANTILLAS, SENTENCIA, CONSTANCIA
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Expediente getExpediente() {
        return expediente;
    }

    public void setExpediente(Expediente expediente) {
        this.expediente = expediente;
    }

    public DocumentoContable getContable() {
        return contable;
    }

    public void setContable(DocumentoContable contable) {
        this.contable = contable;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
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

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }

    public Usuario getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Usuario createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public FileVersion getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(FileVersion currentVersion) {
        this.currentVersion = currentVersion;
    }

    public Long getPlantillaItemId() {
        return plantillaItemId;
    }

    public void setPlantillaItemId(Long plantillaItemId) {
        this.plantillaItemId = plantillaItemId;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public Instant getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Instant lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public Set<Node> getChildren() {
        return children;
    }

    public void setChildren(Set<Node> children) {
        this.children = children;
    }

    public Set<FileVersion> getVersions() {
        return versions;
    }

    public void setVersions(Set<FileVersion> versions) {
        this.versions = versions;
    }

    public Set<NodePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<NodePermission> permissions) {
        this.permissions = permissions;
    }
}
