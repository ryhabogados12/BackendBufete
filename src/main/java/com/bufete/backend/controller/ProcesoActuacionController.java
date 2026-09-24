package com.bufete.backend.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.bufete.backend.Dtos.ApiResponse;
import com.bufete.backend.Dtos.FileUploadResponse;
import com.bufete.backend.Dtos.juridico.*;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.service.ProcesoActuacionService;
import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/procesos/{procesoId}")
public class ProcesoActuacionController {
    private final ProcesoActuacionService service;
    private final UsuarioRepository usuarios;
    public ProcesoActuacionController(ProcesoActuacionService service, UsuarioRepository usuarios) { this.service = service; this.usuarios = usuarios; }

    @PostMapping("/actuaciones")
    public ResponseEntity<ApiResponse<ActuacionDTO>> crear(@PathVariable Long procesoId, @RequestBody CrearActuacionRequest request, Authentication authentication) {
        Usuario usuario = usuarios.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.crear(procesoId, request, usuario.getId()), "Actuación registrada"));
    }

    @GetMapping("/actuaciones")
    public ResponseEntity<ApiResponse<List<ActuacionDTO>>> listar(@PathVariable Long procesoId) {
        return ResponseEntity.ok(ApiResponse.success(service.listar(procesoId), "Actuaciones obtenidas"));
    }

    @GetMapping("/terminos")
    public ResponseEntity<ApiResponse<List<TerminoDTO>>> terminos(@PathVariable Long procesoId) {
        return ResponseEntity.ok(ApiResponse.success(service.listarTerminos(procesoId), "Términos obtenidos"));
    }

        @PostMapping(value = "/actuaciones/{actuacionId}/documentos", consumes = "multipart/form-data")
        public ResponseEntity<ApiResponse<FileUploadResponse>> subirDocumento(
            @PathVariable Long procesoId,
            @PathVariable Long actuacionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String note,
            @RequestParam(defaultValue = "false") boolean esPrincipal,
            Authentication authentication) {
        Usuario usuario = usuarios.findByEmail(authentication.getName())
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        FileUploadResponse response = service.subirDocumento(procesoId, actuacionId, file, description, note,
            esPrincipal, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Documento asociado a la actuación"));
        }
}