package com.bufete.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bufete.backend.Dtos.ApiResponse;
import com.bufete.backend.Dtos.PageResponse;
import com.bufete.backend.Dtos.expediente.CreateExpedienteRequest;
import com.bufete.backend.Dtos.expediente.ExpedienteDTO;
import com.bufete.backend.Dtos.expediente.UpdateEstadoExpedienteRequest;
import com.bufete.backend.model.Expediente;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.service.ExpedienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/expedientes")
@Tag(name = "Expedientes", description = "Gestión de expedientes")
@Slf4j
public class ExpedienteController {
    

    private final UsuarioRepository usuarioRepository;
    private final ExpedienteService expedienteService;
    
    public ExpedienteController(ExpedienteService expedienteService, UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.expedienteService = expedienteService;
    }
    
    @GetMapping
    @Operation(summary = "Listar expedientes", description = "Obtiene una lista paginada de expedientes")
    public ResponseEntity<ApiResponse<PageResponse<ExpedienteDTO>>> getAllExpedientes(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Expediente.EstadoExpediente estado,
            @RequestParam(required = false) Long procesoId) {
        
        PageResponse<ExpedienteDTO> expedientes = expedienteService.getAllExpedientes(
                page, size, sortBy, sortDir, nombre, estado, procesoId);
        
        return ResponseEntity.ok(ApiResponse.success(expedientes, "Expedientes obtenidos exitosamente"));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener expediente por ID")
    public ResponseEntity<ApiResponse<ExpedienteDTO>> getExpedienteById(
            @PathVariable @Positive Long id) {
        
        ExpedienteDTO expediente = expedienteService.getExpedienteById(id);
        return ResponseEntity.ok(ApiResponse.success(expediente, "Expediente obtenido exitosamente"));
    }
    
    @GetMapping("/proceso/{procesoId}")
    @Operation(summary = "Obtener expedientes por proceso")
    public ResponseEntity<ApiResponse<List<ExpedienteDTO>>> getExpedientesByProceso(
            @PathVariable @Positive Long procesoId) {
        
        List<ExpedienteDTO> expedientes = expedienteService.getExpedientesByProceso(procesoId);
        return ResponseEntity.ok(ApiResponse.success(expedientes, "Expedientes del proceso obtenidos exitosamente"));
    }
    
    @PostMapping
    @Operation(summary = "Crear expediente")
    public ResponseEntity<ApiResponse<ExpedienteDTO>> createExpediente(
            @Valid @RequestBody CreateExpedienteRequest request,
            Authentication authentication) {
        
        Long createdById = getUserIdFromAuthentication(authentication);
        
        ExpedienteDTO expediente = expedienteService.createExpediente(request, createdById);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(expediente, "Expediente creado exitosamente"));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar expediente")
    public ResponseEntity<ApiResponse<ExpedienteDTO>> updateExpediente(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CreateExpedienteRequest request,
        Authentication authentication) {
        
        ExpedienteDTO expediente = expedienteService.updateExpediente(id, request);
        return ResponseEntity.ok(ApiResponse.success(expediente, "Expediente actualizado exitosamente"));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar expediente")
    public ResponseEntity<ApiResponse<Void>> deleteExpediente(
            @PathVariable @Positive Long id) {
        
        expedienteService.deleteExpediente(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Expediente eliminado exitosamente"));
    }
    
    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del expediente")
    public ResponseEntity<ApiResponse<ExpedienteDTO>> updateEstadoExpediente(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateEstadoExpedienteRequest request) {
        
        ExpedienteDTO expediente = expedienteService.updateEstadoExpediente(id, request.getEstado());
        return ResponseEntity.ok(ApiResponse.success(expediente, "Estado del expediente actualizado exitosamente"));
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }

        // Obtener el email del usuario autenticado
        String email = authentication.getName();

        // Buscar el usuario en la base de datos por email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));

        return usuario.getId();
    }
}