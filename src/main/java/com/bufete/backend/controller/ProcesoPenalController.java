package com.bufete.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.bufete.backend.Dtos.ApiResponse;
import com.bufete.backend.Dtos.juridico.CrearAsuntoPenalRequest;
import com.bufete.backend.Dtos.juridico.AsuntoPenalDetalleDTO;
import com.bufete.backend.Dtos.juridico.ProcesoPenalResponse;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.service.ProcesoPenalService;
import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/procesos")
public class ProcesoPenalController {
    private final ProcesoPenalService service;
    private final UsuarioRepository usuarios;

    public ProcesoPenalController(ProcesoPenalService service, UsuarioRepository usuarios) {
        this.service = service;
        this.usuarios = usuarios;
    }

    @PostMapping("/penales")
    public ResponseEntity<ApiResponse<ProcesoPenalResponse>> crear(@RequestBody CrearAsuntoPenalRequest request,
            Authentication authentication) {
        Usuario usuario = usuarios.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.crear(request, usuario.getId()), "Proceso penal creado"));
    }

    @GetMapping("/{procesoId}/penal")
    public ResponseEntity<ApiResponse<AsuntoPenalDetalleDTO>> obtenerDetalle(@PathVariable Long procesoId) {
        return ResponseEntity
                .ok(ApiResponse.success(service.obtenerDetalle(procesoId), "Información del asunto penal obtenida"));
    }
}