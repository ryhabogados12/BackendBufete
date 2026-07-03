package com.bufete.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bufete.backend.Dtos.sentencia.CreateSentenciaRequest;
import com.bufete.backend.Dtos.sentencia.SentenciaResponse;
import com.bufete.backend.Dtos.sentencia.UpdateSentenciaRequest;
import com.bufete.backend.model.Sentencia;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.service.SentenciaService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sentencias")
public class SentenciaController {

    private final SentenciaService sentenciaService;
    private final UsuarioRepository usuarioRepository;

    public SentenciaController(SentenciaService sentenciaService, UsuarioRepository usuarioRepository){
        this.sentenciaService = sentenciaService;
        this.usuarioRepository = usuarioRepository;
        
    }


    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<SentenciaResponse>> listarPorCliente(
            @PathVariable String idCliente) {
        List<SentenciaResponse> sentencias = sentenciaService.listarPorCliente(idCliente);
        return ResponseEntity.ok(sentencias);
    }

    /**
     * Listar sentencias por tipo de sentencia
     * GET /api/sentencias/tipo/{tipoSentencia}
     */
    @GetMapping("/tipo/{tipoSentencia}")
    public ResponseEntity<List<SentenciaResponse>> listarPorTipoSentencia(
            @PathVariable String tipoSentencia) {
        List<SentenciaResponse> sentencias = sentenciaService.listarPorTipoSentencia(tipoSentencia);
        return ResponseEntity.ok(sentencias);
    }

    /**
     * Crear una nueva sentencia
     * POST /api/sentencias
     */
    @PostMapping
    public ResponseEntity<SentenciaResponse> crearSentencia(
            @Valid @RequestBody CreateSentenciaRequest request,
            Authentication authentication) {
        
        Long createdById = getUserIdFromAuthentication(authentication);
        SentenciaResponse sentencia = sentenciaService.crearSentencia(request, createdById);
        return ResponseEntity.status(HttpStatus.CREATED).body(sentencia);
    }

    /**
     * Actualizar una sentencia existente
     * PUT /api/sentencias/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Sentencia> actualizarSentencia(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSentenciaRequest request) {
        Sentencia sentencia = sentenciaService.actualizarSentencia(id, request);
        return ResponseEntity.ok(sentencia);
    }

    /**
     * Eliminar (soft delete) una sentencia
     * DELETE /api/sentencias/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSentencia(@PathVariable Long id) {
        sentenciaService.eliminarSentencia(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener una sentencia por ID
     * GET /api/sentencias/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Sentencia> obtenerPorId(@PathVariable Long id) {
        Sentencia sentencia = sentenciaService.obtenerPorId(id);
        return ResponseEntity.ok(sentencia);
    }

    /**
     * Listar todas las sentencias
     * GET /api/sentencias
     */
    @GetMapping
    public ResponseEntity<List<Sentencia>> listarTodas() {
        List<Sentencia> sentencias = sentenciaService.listarTodas();
        return ResponseEntity.ok(sentencias);
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