package com.bufete.backend.controller;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bufete.backend.Dtos.ApiResponse;
import com.bufete.backend.Dtos.FileUploadResponse;
import com.bufete.backend.Dtos.folder.CreateFolderRequest;
import com.bufete.backend.Dtos.folder.DownloadUrlDTO;
import com.bufete.backend.Dtos.folder.FileUploadRequest;
import com.bufete.backend.Dtos.folder.NodeDTO;
import com.bufete.backend.Dtos.folder.PlantillaRequest;
import com.bufete.backend.Dtos.proceso.ProcesoDTO;
import com.bufete.backend.Dtos.sentencia.CreateSentenciaRequest;
import com.bufete.backend.Dtos.sentencia.SentenciaRequest;
import com.bufete.backend.model.Cliente;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.service.ClienteService;
import com.bufete.backend.service.NodeService;
import com.bufete.backend.service.ProcesoService;
import com.bufete.backend.service.SentenciaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/nodes")
@Tag(name = "Gestión de Archivos", description = "Sistema de archivos jerárquico")
@Slf4j
public class NodeController {
    
    private final NodeService nodeService;
    private final SentenciaService sentenciaService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteService clienteService;
    private final ProcesoService procesoService;    
    
    public NodeController(NodeService nodeService, SentenciaService sentenciaService, UsuarioRepository usuarioRepository, ClienteService clienteService, ProcesoService procesoService    ) {
        this.nodeService = nodeService;
        this.sentenciaService = sentenciaService;
        this.usuarioRepository = usuarioRepository;
        this.clienteService = clienteService;
        this.procesoService = procesoService;
    }
    
    @GetMapping("/{parentId}/children")
    @Operation(summary = "Obtener contenido de una carpeta")
    public ResponseEntity<ApiResponse<List<NodeDTO>>> getNodeChildren(
            @PathVariable String parentId) {
        
        UUID paren = UUID.fromString(parentId);       
        List<NodeDTO> children = nodeService.getNodeChildren(paren);
        return ResponseEntity.ok(ApiResponse.success(children, "Contenido obtenido exitosamente"));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener información de un nodo")
    public ResponseEntity<ApiResponse<NodeDTO>> getNodeById(
            @PathVariable UUID id) {
        
        NodeDTO node = nodeService.getNodeById(id);
        return ResponseEntity.ok(ApiResponse.success(node, "Nodo obtenido exitosamente"));
    }
    
    @PostMapping("/folders")
    @Operation(summary = "Crear carpeta")
    public ResponseEntity<ApiResponse<NodeDTO>> createFolder(
            @Valid @RequestBody CreateFolderRequest request,
            Authentication authentication) {
        
        Long createdById = getUserIdFromAuthentication(authentication);
        
        NodeDTO folder = nodeService.createFolder(request, createdById);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(folder, "Carpeta creada exitosamente"));
    }

    @PostMapping("/uploadPlantilla")
    @Operation(summary = "Subir archivo")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadPlantilla(
            @RequestParam("file") MultipartFile file,
            @RequestParam String categoriaId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String description,
            
            Authentication authentication) {
        
        Long uploadedById = getUserIdFromAuthentication(authentication);

        Long nuevaCat = Long.valueOf(categoriaId);

        PlantillaRequest request = PlantillaRequest.builder()
                .file(file)
                .description(description)
                .idCategory(nuevaCat)
                .nombre(nombre)
                .build();
        
        FileUploadResponse fileNode = nodeService.uploadFilePlantilla(request, uploadedById);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(fileNode, "Archivo subido exitosamente"));
    }

    @PostMapping("/uploadSentencia")
    @Operation(summary = "Subir archivo")
    public ResponseEntity<ApiResponse<CreateSentenciaRequest>> uploadSentencia(
            @RequestParam("file") MultipartFile file,
            @RequestParam String procesoId,
            @RequestParam(required = false) String expedienteId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String tipoDoc,
            
            Authentication authentication) {
        
        Long uploadedById = getUserIdFromAuthentication(authentication);

        Long procID = Long.valueOf(procesoId);
        if(expedienteId != null){
            Long expID = Long.valueOf(expedienteId);
        }

        String id_cliente = procesoService.getProcesoById(procID).getClienteId().toString();
        System.out.println("id_cliente: " + id_cliente);

        SentenciaRequest request = SentenciaRequest.builder()
                .file(file)
                .description(description)
                .procesosId(procID)
                .expedId(procID)
                .clienteId(id_cliente)
                .nombre(nombre)
                .build();        

        CreateSentenciaRequest sentSaved = nodeService.uploadFileSentencia(request, uploadedById, tipoDoc);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(sentSaved, "Archivo subido exitosamente"));
    }
    
    @PostMapping("/uploadFile")
    @Operation(summary = "Subir archivo")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFileToExpediente(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam UUID parentId,
            @RequestParam(required = false) String note,
            Authentication authentication) {
        
        Long uploadedById = getUserIdFromAuthentication(authentication);
        
        FileUploadRequest request = FileUploadRequest.builder()
                .file(file)
                .description(description)
                .parentId(parentId)
                .note(note)
                .build();
        
        FileUploadResponse fileNode = nodeService.uploadFileToExpediente(request, uploadedById);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(fileNode, "Archivo subido exitosamente"));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar nodo")
    public ResponseEntity<ApiResponse<Void>> deleteNode(
            @PathVariable UUID id) {
        
        nodeService.deleteNode(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Nodo eliminado exitosamente"));
    }

    @GetMapping("/{id}/downloadFileExpediente")
    @Operation(summary = "Obtener URL de descarga")
    public ResponseEntity<ApiResponse<DownloadUrlDTO>> getDownloadUrlInExpediente(
            @PathVariable UUID id) {
        
        DownloadUrlDTO downloadUrl = nodeService.getDownloadUrl(id, Duration.ofHours(1));
        return ResponseEntity.ok(ApiResponse.success(downloadUrl, "URL de descarga generada"));
    }

    @GetMapping("/{id}/viewDoc")
    @Operation(summary = "Obtener URL de visualizacion")
    public ResponseEntity<ApiResponse<DownloadUrlDTO>> getViewUrlDoc(
            @PathVariable UUID id) {
        
        DownloadUrlDTO downloadUrl = nodeService.getViewFileBlob(id, Duration.ofHours(1));
        return ResponseEntity.ok(ApiResponse.success(downloadUrl, "URL de descarga generada"));
    }

    @GetMapping("/{id}/viewFileExpediente")
    @Operation(summary = "Obtener URL de visualizacion")
    public ResponseEntity<ApiResponse<DownloadUrlDTO>> getViewUrlInExpediente(
            @PathVariable UUID id) {
        
        DownloadUrlDTO downloadUrl = nodeService.getViewUrl(id, Duration.ofHours(1));
        return ResponseEntity.ok(ApiResponse.success(downloadUrl, "URL de descarga generada"));
    }

    @GetMapping("/{id}/downloadDoc")
    @Operation(summary = "Obtener URL de descarga")
    public ResponseEntity<ApiResponse<DownloadUrlDTO>> getDownloadUrlDoc(
            @PathVariable UUID id) {
        
        DownloadUrlDTO downloadUrl = nodeService.getDownloadFileBlob(id, Duration.ofHours(1));
        return ResponseEntity.ok(ApiResponse.success(downloadUrl, "URL de descarga generada"));
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
