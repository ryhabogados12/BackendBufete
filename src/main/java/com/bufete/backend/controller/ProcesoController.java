package com.bufete.backend.controller;

import java.util.List;

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
import com.bufete.backend.Dtos.proceso.CreateProcesoRequest;
import com.bufete.backend.Dtos.proceso.EditProcesoRequest;
import com.bufete.backend.Dtos.proceso.ProcesoDTO;
import com.bufete.backend.Dtos.proceso.UpdateEstadoProcesoRequest;
import com.bufete.backend.model.Proceso;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.service.ProcesoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/procesos")
@Tag(name = "Procesos", description = "Gestión de procesos legales")
@Slf4j
public class ProcesoController {
    
    private final ProcesoService procesoService;
    private final UsuarioRepository usuarioRepository;
    
    public ProcesoController(ProcesoService procesoService, UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.procesoService = procesoService;
    }
    
    @GetMapping
    @Operation(summary = "Listar procesos", description = "Obtiene una lista paginada de procesos con filtros opcionales")
    public ResponseEntity<ApiResponse<PageResponse<ProcesoDTO>>> getAllProcesos(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String numeroProceso,
            @RequestParam(required = false) Proceso.EstadoProceso estado,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long abogadoId,
            @RequestParam(required = false) Boolean activo) {
        
        PageResponse<ProcesoDTO> procesos = procesoService.getAllProcesos(
                page, size, sortBy, sortDir, nombre, numeroProceso, estado, clienteId, abogadoId, activo);
        
        return ResponseEntity.ok(ApiResponse.success(procesos, "Procesos obtenidos exitosamente"));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener proceso por ID", description = "Obtiene los detalles de un proceso específico")
    public ResponseEntity<ApiResponse<ProcesoDTO>> getProcesoById(
            @PathVariable @Positive Long id) {
        
        ProcesoDTO proceso = procesoService.getProcesoById(id);
        return ResponseEntity.ok(ApiResponse.success(proceso, "Proceso obtenido exitosamente"));
    }
    
    @PostMapping
    @Operation(summary = "Crear proceso", description = "Crea un nuevo proceso legal")
    public ResponseEntity<ApiResponse<ProcesoDTO>> createProceso(
            @Valid @RequestBody CreateProcesoRequest request,
            Authentication authentication) {
        Long createdById = getUserIdFromAuthentication(authentication);
        
        request.setAbogadoResponsableId(createdById);

        ProcesoDTO proceso = procesoService.createProceso(request, createdById);
        return ResponseEntity.ok(ApiResponse.success(proceso, "Proceso creado exitosamente"));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proceso", description = "Actualiza los datos de un proceso existente")
    public ResponseEntity<ApiResponse<ProcesoDTO>> updateProceso(
            @PathVariable @Positive Long id,
            @Valid @RequestBody EditProcesoRequest request,
        Authentication authentication) {

            Long abogadoId = getUserIdFromAuthentication(authentication);
        
        ProcesoDTO proceso = procesoService.updateProceso(id, request, abogadoId);
        return ResponseEntity.ok(ApiResponse.success(proceso, "Proceso actualizado exitosamente"));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proceso", description = "Desactiva un proceso")
    public ResponseEntity<ApiResponse<Void>> deleteProceso(
            @PathVariable @Positive Long id,
        Authentication authentication) {
        
        procesoService.deleteProceso(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Proceso eliminado exitosamente"));
    }
    
    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del proceso", description = "Cambia el estado de un proceso")
    public ResponseEntity<ApiResponse<ProcesoDTO>> updateEstadoProceso(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateEstadoProcesoRequest request) {
        
        ProcesoDTO proceso = procesoService.updateEstadoProceso(id, request.getEstado());
        return ResponseEntity.ok(ApiResponse.success(proceso, "Estado del proceso actualizado exitosamente"));
    }
    
    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtener procesos por cliente", description = "Obtiene todos los procesos activos de un cliente")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> getProcesosByCliente(
            @PathVariable @Positive Long clienteId) {
        
        List<ProcesoDTO> procesos = procesoService.getProcesosByCliente(clienteId);
        return ResponseEntity.ok(ApiResponse.success(procesos, "Procesos del cliente obtenidos exitosamente"));
    }
    
    @GetMapping("/abogado")
    @Operation(summary = "Obtener procesos por abogado", description = "Obtiene todos los procesos activos de un abogado")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> getProcesosByAbogado(
        Authentication authentication) {
        Long createdById = getUserIdFromAuthentication(authentication);
        
        List<ProcesoDTO> procesos = procesoService.getProcesosByAbogado(createdById);
        return ResponseEntity.ok(ApiResponse.success(procesos, "Procesos del abogado obtenidos exitosamente"));
    }
    
    @GetMapping("/sistema")
    @Operation(summary = "Obtener procesos del sistema", description = "Obtiene todos los procesos activos del sistema")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> getProcesosSistema(
        Authentication authentication) {
        Long createdById = getUserIdFromAuthentication(authentication);
        
        List<ProcesoDTO> procesos = procesoService.getProcesosSistema();
        return ResponseEntity.ok(ApiResponse.success(procesos, "Procesos del sistema obtenidos exitosamente"));
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