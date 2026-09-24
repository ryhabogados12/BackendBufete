package com.bufete.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_version", indexes = {
    @Index(name = "idx_version_node", columnList = "node_id"),
    @Index(name = "idx_version_uploaded_by", columnList = "uploaded_by")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"node_id", "version_num"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private Node node;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_blob_id", nullable = false)
    private FileBlob blob;
    
    @Column(name = "version_num", nullable = false)
    private Integer versionNum;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Usuario uploadedBy;
    
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant uploadedAt;
    
    @Column(name = "note")
    private String note;
    
    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = true;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public FileBlob getBlob() {
        return blob;
    }

    public void setBlob(FileBlob blob) {
        this.blob = blob;
    }

    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    public Usuario getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Usuario uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    
}