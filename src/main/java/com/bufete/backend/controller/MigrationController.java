package com.bufete.backend.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bufete.backend.Dtos.ApiResponse;
import com.bufete.backend.service.S3MigrationService;
import com.bufete.backend.utils.MigrationReport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/migration")
@Tag(name = "Migración", description = "Endpoints para migración de datos desde S3")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {
    
    private final S3MigrationService migrationService;
    
    @PostMapping("/execute")
    @Operation(summary = "Ejecutar migración desde S3", 
               description = "Migra la estructura de carpetas y archivos existentes en S3 a la base de datos")
    public ResponseEntity<ApiResponse<MigrationReport>> executeMigration() {
        log.warn("========================================");
        log.warn("INICIO DE MIGRACIÓN SOLICITADA");
        log.warn("Usuario: {}", org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName());
        log.warn("========================================");
        
        try {
            MigrationReport report = migrationService.migrateS3Structure();
            
            if (report.getErrors().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(
                    report, 
                    "Migración completada exitosamente"
                ));
            } else {
                return ResponseEntity.ok(ApiResponse.success(
                    report, 
                    String.format("Migración completada con %d errores", 
                                report.getErrors().size())
                ));
            }
            
        } catch (Exception e) {
            log.error("Error crítico en migración", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(
                    List.of(e.getMessage()),
                    "Error ejecutando migración",
                    "/api/migration/execute"
                ));
        }
    }
    
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Verificar estado de preparación para migración")
    public ResponseEntity<ApiResponse<MigrationStatusDTO>> checkStatus() {
        // Implementación para verificar pre-requisitos
        MigrationStatusDTO status = new MigrationStatusDTO();
        status.setReady(true);
        status.setMessage("Sistema listo para migración");
        
        return ResponseEntity.ok(ApiResponse.success(status, "Estado verificado"));
    }

    @Data
    public static class MigrationStatusDTO {
        private boolean ready;
        private String message;
        private List<String> warnings = new ArrayList<>();
    }
}