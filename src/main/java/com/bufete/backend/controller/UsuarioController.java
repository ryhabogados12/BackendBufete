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
import com.bufete.backend.Dtos.ChangePasswordRequest;
import com.bufete.backend.Dtos.PageResponse;
import com.bufete.backend.Dtos.usuario.CreateUsuarioRequest;
import com.bufete.backend.Dtos.usuario.EditUsuarioDTO;
import com.bufete.backend.Dtos.usuario.UpdateUsuarioRequest;
import com.bufete.backend.Dtos.usuario.UsuarioDTO;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@Slf4j
public class UsuarioController {
    
    private final UsuarioService usuarioService;
    
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene una lista paginada de usuarios con filtros opcionales")
    public ResponseEntity<ApiResponse<List<UsuarioDTO>>> getAllUsuarios(Authentication authentication) {

        List<UsuarioDTO> usuarios = usuarioService.allUsuarios();
        return ResponseEntity.ok(ApiResponse.success(usuarios, "Usuarios obtenidos exitosamente"));
    }

    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los detalles de un usuario específico")
    public ResponseEntity<ApiResponse<EditUsuarioDTO>> getUsuarioById(
            @PathVariable @Positive Long id,
            Authentication authentication) {
        
        EditUsuarioDTO usuario = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Usuario obtenido exitosamente"));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en el sistema")
    public ResponseEntity<ApiResponse<UsuarioDTO>> createUsuario(
            @Valid @RequestBody CreateUsuarioRequest request,
            Authentication authentication) {
        
        UsuarioDTO usuario = usuarioService.createUsuario(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(usuario, "Usuario creado exitosamente"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    public ResponseEntity<ApiResponse<UsuarioDTO>> updateUsuario(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        
        UsuarioDTO usuario = usuarioService.updateUsuario(id, request);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Usuario actualizado exitosamente"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Eliminar usuario", description = "Desactiva un usuario del sistema")
    public ResponseEntity<ApiResponse<Void>> deleteUsuario(
            @PathVariable @Positive Long id,
            Authentication authentication) {
        
        usuarioService.deleteUsuario(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Usuario eliminado exitosamente"));
    }
    
    @GetMapping("/abogados")
    @Operation(summary = "Obtener abogados activos", description = "Obtiene la lista de todos los abogados activos")
    public ResponseEntity<ApiResponse<List<UsuarioDTO>>> getAbogados() {
        List<UsuarioDTO> abogados = usuarioService.getAbogados();
        return ResponseEntity.ok(ApiResponse.success(abogados, "Abogados obtenidos exitosamente"));
    }
    
    @PutMapping("/{id}/change-password")
    @Operation(summary = "Cambiar contrasena", description = "Cambia la contrasena de un usuario")
    public ResponseEntity<ApiResponse<UsuarioDTO>> changePassword(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ChangePasswordRequest request) {

        UsuarioDTO usuario = usuarioService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(usuario, "Contrasena cambiada exitosamente"));
    }
    
    
}
