package com.bufete.backend.controller;

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
import com.bufete.backend.Dtos.cliente.ClienteConsultarDTO;
import com.bufete.backend.Dtos.cliente.ClienteDTO;
import com.bufete.backend.Dtos.cliente.CreateClienteRequest;
import com.bufete.backend.model.Cliente;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/clientes")
/* @Tag(name = "Clientes", description = "Gestión de clientes") */
@Slf4j
public class ClienteController {

    private final UsuarioRepository usuarioRepository;

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService, UsuarioRepository usuarioRepository) {
        this.clienteService = clienteService;
        this.usuarioRepository = usuarioRepository;
    }

    

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Obtiene una lista paginada de clientes con filtros opcionales")
    public ResponseEntity<ApiResponse<PageResponse<ClienteDTO>>> getAllClientes(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String identificacion,
            @RequestParam(required = false) Cliente.TipoCliente tipoCliente,
            @RequestParam(required = false) Boolean activo) {

        PageResponse<ClienteDTO> clientes = clienteService.getAllClientes(
                page, size, sortBy, sortDir, nombre, identificacion, tipoCliente, activo);

        return ResponseEntity.ok(ApiResponse.success(clientes, "Clientes obtenidos exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Obtiene los detalles de un cliente específico")
    public ResponseEntity<ApiResponse<ClienteDTO>> getClienteById(
            @PathVariable @Positive Long id) {

        ClienteDTO cliente = clienteService.getClienteById(id);
        return ResponseEntity.ok(ApiResponse.success(cliente, "Cliente obtenido exitosamente"));
    }

    @GetMapping("/consulta/{identificacion}")
    @Operation(summary = "Obtener cliente por identificación", description = "Obtiene los detalles de un cliente específico")
    public ResponseEntity<ApiResponse<ClienteConsultarDTO>> getClienteByIdentificacion(
            @PathVariable String identificacion) {

        ClienteConsultarDTO cliente = clienteService.getClienteByIdentificacion(identificacion);
        return ResponseEntity.ok(ApiResponse.success(cliente, "Cliente obtenido exitosamente"));
    }

    @PostMapping
    @Operation(summary = "Crear cliente", description = "Crea un nuevo cliente")
    public ResponseEntity<ApiResponse<ClienteDTO>> createCliente(
            @Valid @RequestBody CreateClienteRequest request,
            Authentication authentication) {

        // Obtener ID del usuario autenticado
        Long createdById = getUserIdFromAuthentication(authentication);

        ClienteDTO cliente = clienteService.createCliente(request, createdById);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cliente, "Cliente creado exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    public ResponseEntity<ApiResponse<ClienteDTO>> updateCliente(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CreateClienteRequest request) {

        ClienteDTO cliente = clienteService.updateCliente(id, request);
        return ResponseEntity.ok(ApiResponse.success(cliente, "Cliente actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente", description = "Desactiva un cliente")
    public ResponseEntity<ApiResponse<Void>> deleteCliente(
            @PathVariable @Positive Long id) {

        clienteService.deleteCliente(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cliente eliminado exitosamente"));
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