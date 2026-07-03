package com.bufete.backend.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class MigrationReport {
    private int totalGroups;
    private int processedGroups;
    private int totalFiles;
    private int migratedFiles;
    private List<MigrationError> errors = new ArrayList<>();
    private Instant startTime;
    private Instant endTime;
    
    public void addError(String key, String message) {
        errors.add(new MigrationError(key, message));
    }
    
    public void incrementProcessedGroups() {
        processedGroups++;
    }
    
    public void incrementMigratedFiles() {
        migratedFiles++;
    }
    
    public void complete() {
        endTime = Instant.now();
    }
    
    public long getDurationSeconds() {
        if (endTime == null) {
            return Duration.between(startTime, Instant.now()).getSeconds();
        }
        return Duration.between(startTime, endTime).getSeconds();
    }
    
    @Data
    @AllArgsConstructor
    public static class MigrationError {
        private String key;
        private String message;
    }
}