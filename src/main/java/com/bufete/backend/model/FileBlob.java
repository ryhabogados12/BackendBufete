package com.bufete.backend.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_blob", indexes = {
    @Index(name = "idx_blob_storage_key", columnList = "storage_key"),
    @Index(name = "idx_blob_mime_type", columnList = "mime_type")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileBlob {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;
    
    @Column(name = "bucket_name", nullable = false, length = 100)
    private String bucketName;
    
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;
    
    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;
    
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;
    
    @Column(name = "original_name", length = 255)
    private String originalName;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    
    @Column(name = "is_image")
    @Builder.Default
    private Boolean isImage = false;
    
    @Column(name = "thumbnail_key", length = 500)
    private String thumbnailKey;
    
    @OneToMany(mappedBy = "blob")
    @Builder.Default
    private Set<FileVersion> versions = new HashSet<>();
    
    @OneToMany(mappedBy = "fileBlob")
    @Builder.Default
    private Set<Plantilla> plantillas = new HashSet<>();
    
    @OneToMany(mappedBy = "fileBlob")
    @Builder.Default
    private Set<Sentencia> sentencias = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public void setChecksumSha256(String checksumSha256) {
        this.checksumSha256 = checksumSha256;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsImage() {
        return isImage;
    }

    public void setIsImage(Boolean isImage) {
        this.isImage = isImage;
    }

    public String getThumbnailKey() {
        return thumbnailKey;
    }

    public void setThumbnailKey(String thumbnailKey) {
        this.thumbnailKey = thumbnailKey;
    }

    public Set<FileVersion> getVersions() {
        return versions;
    }

    public void setVersions(Set<FileVersion> versions) {
        this.versions = versions;
    }

    public Set<Plantilla> getPlantillas() {
        return plantillas;
    }

    public void setPlantillas(Set<Plantilla> plantillas) {
        this.plantillas = plantillas;
    }

    public Set<Sentencia> getSentencias() {
        return sentencias;
    }

    public void setSentencias(Set<Sentencia> sentencias) {
        this.sentencias = sentencias;
    }

    
}
