package com.bufete.backend.Dtos.migracion;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class S3ObjectSummary {
    private String key;
    private Long size;
    private Instant lastModified;
    private String eTag;
}