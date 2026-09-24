package com.bufete.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.bufete.backend.Dtos.ApiResponse;
import com.bufete.backend.Dtos.juridico.*;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.service.RecursoExtraordinarioService;
import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/procesos/{procesoId}/recursos-extraordinarios")
public class RecursoExtraordinarioController {
    private final RecursoExtraordinarioService service;
    private final UsuarioRepository usuarios;
    public RecursoExtraordinarioController(RecursoExtraordinarioService service, UsuarioRepository usuarios) { this.service = service; this.usuarios = usuarios; }

    @PostMapping
    public ResponseEntity<ApiResponse<RecursoExtraordinarioDTO>> crear(@PathVariable Long procesoId,
            @RequestBody CrearRecursoExtraordinarioRequest request, Authentication authentication) {
        Usuario usuario = usuarios.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.crear(procesoId, request, usuario.getId()), "Recurso extraordinario creado"));
    }
}