package com.bufete.backend.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.bufete.backend.Dtos.FileUploadResponse;
import com.bufete.backend.Dtos.folder.CreateFolderRequest;
import com.bufete.backend.Dtos.folder.DownloadUrlDTO;
import com.bufete.backend.Dtos.folder.FileStorageResult;
import com.bufete.backend.Dtos.folder.FileUploadRequest;
import com.bufete.backend.Dtos.folder.NodeDTO;
import com.bufete.backend.Dtos.folder.PlantillaRequest;
import com.bufete.backend.Dtos.sentencia.CreateSentenciaRequest;
import com.bufete.backend.Dtos.sentencia.SentenciaRequest;
import com.bufete.backend.model.Categoria;
import com.bufete.backend.model.Expediente;
import com.bufete.backend.model.FileBlob;
import com.bufete.backend.model.FileVersion;
import com.bufete.backend.model.Node;
import com.bufete.backend.model.Plantilla;
import com.bufete.backend.model.Proceso;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.model.Categoria.TipoCategoria;
import com.bufete.backend.model.Cliente;
import com.bufete.backend.repository.CategoriaRepository;
import com.bufete.backend.repository.ExpedienteRepository;
import com.bufete.backend.repository.FileBlobRepository;
import com.bufete.backend.repository.FileVersionRepository;
import com.bufete.backend.repository.NodeRepository;
import com.bufete.backend.repository.PlantillaRepository;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.utils.NodeMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class NodeService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final SentenciaService sentenciaService;
    private final CategoriaRepository categoryRepository;
    private final NodeRepository nodeRepository;
    private final PlantillaRepository plantillaRepository;
    private final FileVersionRepository fileVersionRepository;
    private final FileBlobRepository fileBlobRepository;
    private final UsuarioRepository usuarioRepository;
    private final NodeMapper nodeMapper;
    private final S3Service s3Service;

    public NodeService(NodeRepository nodeRepository,
            FileVersionRepository fileVersionRepository,
            FileBlobRepository fileBlobRepository,
            UsuarioRepository usuarioRepository,
            NodeMapper nodeMapper,
            S3Service s3Service,
            CategoriaRepository categoryRepository,
            PlantillaRepository plantillaRepository, 
            SentenciaService sentenciaService) {
        this.sentenciaService = sentenciaService;
        this.categoryRepository = categoryRepository;
        this.nodeRepository = nodeRepository;
        this.plantillaRepository = plantillaRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.fileBlobRepository = fileBlobRepository;
        this.usuarioRepository = usuarioRepository;
        this.nodeMapper = nodeMapper;
        this.s3Service = s3Service;
    }

    @Transactional(readOnly = true)
    public List<NodeDTO> getNodeChildren(UUID parentId) {
        List<Node> children = nodeRepository.findByParentIdAndIsDeletedFalse(parentId);
        return children.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NodeDTO getNodeById(UUID id) {
        Node node = nodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nodo no encontrado con ID: " + id));

        if (node.getIsDeleted()) {
            throw new EntityNotFoundException("El nodo ha sido eliminado");
        }

        return convertToDTO(node);
    }

    public NodeDTO createFolder(CreateFolderRequest request, Long createdById) {
        // Validar que el padre existe
        Node parent = nodeRepository.findById(request.getParentId())
                .orElseThrow(() -> new EntityNotFoundException("Nodo padre no encontrado"));

        if (parent.getType() != Node.NodeType.FOLDER) {
            throw new ValidationException("Solo se pueden crear carpetas dentro de otras carpetas");
        }

        // Validar nombre único dentro del padre
        if (nodeRepository.findByNameAndParentId(request.getName(), request.getParentId()).isPresent()) {
            throw new ValidationException("Ya existe un elemento con el nombre: " + request.getName());
        }

        Usuario createdBy = usuarioRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Node folder = Node.builder()
                .parent(parent)
                .type(Node.NodeType.FOLDER)
                .name(request.getName())
                .description(request.getDescription())
                .modulo(parent.getModulo())
                .expediente(parent.getExpediente())
                .contable(parent.getContable())
                .createdBy(createdBy)
                .sizeBytes(0L)
                .itemCount(0)
                .build();

        Node savedFolder = nodeRepository.save(folder);

        // Actualizar contador del padre
        updateParentItemCount(parent);

        log.info("Carpeta creada: {} (ID: {})", savedFolder.getName(), savedFolder.getId());

        return convertToDTO(savedFolder);
    }

    @Transactional
    public FileUploadResponse uploadFileToExpediente(FileUploadRequest request, Long uploadedById) {

        // Validar que el padre existe y es una carpeta
        Node parent = nodeRepository.findById(request.getParentId())
                .orElseThrow(() -> new EntityNotFoundException("Nodo padre no encontrado"));

        if (parent.getType() != Node.NodeType.FOLDER) {
            throw new ValidationException("Solo se pueden subir archivos a carpetas");
        }

        // Validar nombre único dentro del padre
        String fileName = request.getFile().getOriginalFilename();
        if (nodeRepository.findByNameAndParentId(fileName, request.getParentId()).isPresent()) {
            throw new ValidationException("Ya existe un archivo con el nombre: " + fileName);
        }

        Usuario uploadedBy = usuarioRepository.findById(uploadedById)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        try {

            // Calcular checksum
            String checksum = s3Service.calculateSHA256(request.getFile());

            // Buscar blob existente (deduplicación)
            Optional<FileBlob> existingBlob = fileBlobRepository
                    .findByChecksumSha256AndSizeBytes(checksum, request.getFile().getSize());

            FileBlob blob;
            String storageKey;

            if (existingBlob.isPresent()) {
                blob = existingBlob.get();
                storageKey = blob.getStorageKey();
                log.info("Reutilizando blob existente: {}", storageKey);
            } else {
                // Crear nodo primero para generar ID
                Node fileNode = new Node();
                fileNode.setExpediente(parent.getExpediente());
                fileNode.setParent(parent);
                fileNode.setType(Node.NodeType.FILE);
                fileNode.setName(request.getFile().getOriginalFilename());
                fileNode.setDescription(request.getDescription());
                fileNode.setCreatedBy(uploadedBy);
                fileNode.setCreatedAt(Instant.now());
                fileNode.setIsDeleted(false);
                fileNode.setSizeBytes(request.getFile().getSize());

                Node savedNode = nodeRepository.save(fileNode);

                // Subir a S3
                storageKey = s3Service.uploadFileToExpediente(request.getFile(), parent.getExpediente().getId(),
                        savedNode.getId(), uploadedById);

                // Crear blob
                blob = new FileBlob();
                blob.setStorageKey(storageKey);
                blob.setBucketName(bucketName);
                blob.setSizeBytes(request.getFile().getSize());
                blob.setChecksumSha256(checksum);
                blob.setMimeType(request.getFile().getContentType());
                blob.setOriginalName(request.getFile().getOriginalFilename());
                blob.setIsImage(isImageFile(request.getFile().getContentType()));
                blob.setCreatedAt(Instant.now());

                blob = fileBlobRepository.save(blob);

                // Crear versión
                FileVersion version = new FileVersion();
                version.setNode(savedNode);
                version.setBlob(blob);
                version.setVersionNum(1);
                version.setUploadedBy(uploadedBy);
                version.setNote(request.getNote());
                version.setIsCurrent(true);
                version.setUploadedAt(Instant.now());

                FileVersion savedVersion = fileVersionRepository.save(version);

                // Actualizar nodo con versión actual
                savedNode.setCurrentVersion(savedVersion);
                nodeRepository.save(savedNode);

                log.info("Archivo subido: {} (ID: {})", savedNode.getName(), savedNode.getId());

                return FileUploadResponse.builder()
                        .nodeId(savedNode.getId())
                        .name(savedNode.getName())
                        .sizeBytes(savedNode.getSizeBytes())
                        .mimeType(blob.getMimeType())
                        .versionNumber(1)
                        .message("Archivo subido exitosamente")
                        .build();
            }
            return null;

        } catch (Exception e) {
            log.error("Error subiendo archivo: {}", fileName, e);
            throw new RuntimeException("Error subiendo archivo: " + e.getMessage());
        }
    }

    public void deleteNode(UUID id) {
        Node node = nodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nodo no encontrado con ID: " + id));

        softDeleteNodeTree(id);

        // Actualizar contadores del padre si existe
        if (node.getParent() != null) {
            updateParentItemCount(node.getParent());
            updateParentSize(node.getParent());
        }

        log.info("Nodo eliminado: {} (ID: {})", node.getName(), node.getId());
    }

    public void softDeleteNodeTree(UUID nodeId) {
        Node node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new EntityNotFoundException("Nodo no encontrado"));

        // Marcar como eliminado recursivamente
        markNodeAsDeleted(node);
    }

    private void markNodeAsDeleted(Node node) {
        node.setIsDeleted(true);
        nodeRepository.save(node);

        // Eliminar hijos recursivamente
        List<Node> children = nodeRepository.findByParentIdAndIsDeletedFalse(node.getId());
        for (Node child : children) {
            markNodeAsDeleted(child);
        }
    }

    public FileUploadResponse uploadFilePlantilla(PlantillaRequest request, Long uploadedById) {

        Usuario uploadedBy = usuarioRepository.findById(uploadedById)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Categoria category = categoryRepository.findById(request.getIdCategory())
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada"));

        if (request.getFile().getContentType() == null) {
            throw new ValidationException("Es necesario subir un archivo ");
        }

        // Validar nombre único del archivo dentro del modulo
        String fileName = request.getFile().getOriginalFilename();
        List<UUID> listaDocsPorModulo = plantillaRepository.listarFileBlobsPorModulo(category.getTipo());
        if (existFileForName(listaDocsPorModulo, fileName)) {
            throw new ValidationException("Ya existe un archivo con el nombre " + fileName + " en esta categoria");
        }

        try {
            // Calcular checksum
            String checksum = s3Service.calculateSHA256(request.getFile());
            FileBlob blob;
            String storageKey;

            // Subir a S3
            storageKey = s3Service.uploadFilePlantilla(request.getFile(), category.getTipo(), category.getNombre());

            // Crear blob
            blob = new FileBlob();
            blob.setStorageKey(storageKey);
            blob.setBucketName(bucketName);
            blob.setSizeBytes(request.getFile().getSize());
            blob.setChecksumSha256(checksum);
            blob.setMimeType(request.getFile().getContentType());
            blob.setOriginalName(request.getFile().getOriginalFilename());
            blob.setIsImage(isImageFile(request.getFile().getContentType()));
            blob.setCreatedAt(Instant.now());

            blob = fileBlobRepository.save(blob);

            Plantilla savedPlantilla = new Plantilla();
            savedPlantilla.setNombre(request.getNombre());
            savedPlantilla.setCategoria(category);
            savedPlantilla.setFechaCreacion(Instant.now());
            savedPlantilla.setResponsable(uploadedBy);
            savedPlantilla.setDescripcion(request.getDescription());
            savedPlantilla.setFileBlob(blob);

            // Actualizar nodo con versión actual
            plantillaRepository.save(savedPlantilla);

            return FileUploadResponse.builder()
                    .nodeId(null)
                    .name(savedPlantilla.getNombre())
                    .sizeBytes(null)
                    .mimeType(blob.getMimeType())
                    .versionNumber(1)
                    .message("Archivo subido exitosamente")
                    .build();

        } catch (Exception e) {
            log.error("Error subiendo archivo: {}", fileName, e);
            throw new RuntimeException("Error subiendo archivo: " + e.getMessage());
        }
    }

    public FileUploadResponse uploadFileSentencia(SentenciaRequest request, Long uploadedById) {

        if (request.getFile().getContentType() == null) {
            throw new ValidationException("Es necesario subir un archivo ");
        }

        try {
            // Calcular checksum
            String checksum = s3Service.calculateSHA256(request.getFile());
            FileBlob blob;
            String storageKey;

            // Subir a S3
            storageKey = s3Service.uploadFileSentencia(request.getFile(), request.getTipoDoc());

            // Crear blob
            blob = new FileBlob();
            blob.setStorageKey(storageKey);
            blob.setBucketName(bucketName);
            blob.setSizeBytes(request.getFile().getSize());
            blob.setChecksumSha256(checksum);
            blob.setMimeType(request.getFile().getContentType());
            blob.setOriginalName(request.getFile().getOriginalFilename());
            blob.setIsImage(isImageFile(request.getFile().getContentType()));
            blob.setCreatedAt(Instant.now());

            blob = fileBlobRepository.save(blob);

            CreateSentenciaRequest savedSentencia = new CreateSentenciaRequest();
            savedSentencia.setNombre(request.getNombre());
            savedSentencia.setAbogadoId(uploadedById);
            savedSentencia.setFechaSentencia(LocalDate.now());
            savedSentencia.setClienteId(request.getClienteId());
            savedSentencia.setTipoSentencia(request.getTipoDoc());
            savedSentencia.setProcesoId(request.getProcesosId());
            savedSentencia.setFileBlobId(blob.getId());

            sentenciaService.crearSentencia(savedSentencia, uploadedById);

            return FileUploadResponse.builder()
                    .nodeId(null)
                    .name(savedSentencia.getNombre())
                    .sizeBytes(null)
                    .mimeType(blob.getMimeType())
                    .versionNumber(1)
                    .message("Archivo subido exitosamente")
                    .build();

        } catch (Exception e) {
            log.error("Error subiendo archivo !", e);
            throw new RuntimeException("Error subiendo archivo: " + e.getMessage());
        }
    }

    public UUID createNodeFile(String name, Node.Modulo modulo, Long createdById) {
        Usuario createdBy = usuarioRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Node rootNode = Node.builder()
                .parent(null)
                .type(Node.NodeType.FILE)
                .name(name)
                .modulo(modulo)
                .createdBy(createdBy)
                .sizeBytes(0L)
                .itemCount(0)
                .build();

        Node savedNode = nodeRepository.save(rootNode);
        return savedNode.getId();
    }

    public UUID createRootNode(String name, Node.Modulo modulo, Long createdById) {
        Usuario createdBy = usuarioRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Node rootNode = Node.builder()
                .parent(null)
                .type(Node.NodeType.FOLDER)
                .name(name)
                .modulo(modulo)
                .createdBy(createdBy)
                .sizeBytes(0L)
                .itemCount(0)
                .build();

        Node savedNode = nodeRepository.save(rootNode);
        return savedNode.getId();
    }

    public void associateNodeToExpediente(UUID nodeId, Expediente expedienteId) {
        Node node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new EntityNotFoundException("Nodo no encontrado"));

        node.setExpediente(expedienteId);

        nodeRepository.save(node);
    }

    private void updateParentItemCount(Node parent) {
        int count = nodeRepository.countChildrenByParentId(parent.getId());
        parent.setItemCount(count);
        nodeRepository.save(parent);
    }

    private void updateParentSize(Node parent) {
        long size = nodeRepository.getTotalSizeByParentId(parent.getId());
        parent.setSizeBytes(size);
        nodeRepository.save(parent);
    }

    private NodeDTO convertToDTO(Node node) {
        NodeDTO dto = nodeMapper.toDTO(node);

        // Agregar información adicional
        if (node.getType() == Node.NodeType.FILE && node.getCurrentVersion() != null) {
            dto.setCurrentVersionId(node.getCurrentVersion().getId());
            dto.setMimeType(node.getCurrentVersion().getBlob().getMimeType());
            dto.setVersionNum(node.getCurrentVersion().getVersionNum());
            dto.setOriginalName(node.getCurrentVersion().getBlob().getOriginalName());
        }
        // Obtener path completo
        /* String path = nodeRepository.getNodePath(node.getId()); */
        String path = node.getName();
        dto.setPath(path != null ? "" + path : "/");

        return dto;
    }

    @Transactional(readOnly = true)
    public DownloadUrlDTO getViewUrl(UUID id, Duration duration) {
        Node node = nodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nodo no encontrado"));
        
        if (node.getType() != Node.NodeType.FILE) {
            throw new ValidationException("Solo se pueden descargar archivos");
        }
        
        FileVersion currentVersion = node.getCurrentVersion();
        if (currentVersion == null) {
            throw new EntityNotFoundException("No hay versión actual del archivo");
        }
        
        FileBlob blob = currentVersion.getBlob();
        String url = s3Service.generatePresignedViewUrl(blob.getStorageKey(), duration, blob.getOriginalName(), blob.getMimeType());
        
        // Actualizar último acceso
        node.setLastAccessed(Instant.now());
        nodeRepository.save(node);
        
        return DownloadUrlDTO.builder()
                .url(url)
                .fileName(blob.getOriginalName())
                .mimeType(blob.getMimeType())
                .sizeBytes(blob.getSizeBytes())
                .expiresInSeconds((int) duration.getSeconds())
                .build();
    }

    @Transactional(readOnly = true)
    public DownloadUrlDTO getDownloadUrl(UUID id, Duration duration) {
        Node node = nodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nodo no encontrado"));
        
        if (node.getType() != Node.NodeType.FILE) {
            throw new ValidationException("Solo se pueden descargar archivos");
        }
        
        FileVersion currentVersion = node.getCurrentVersion();
        if (currentVersion == null) {
            throw new EntityNotFoundException("No hay versión actual del archivo");
        }
        
        FileBlob blob = currentVersion.getBlob();
        String url = s3Service.generatePresignedDownloadUrl(blob.getStorageKey(), duration, blob.getOriginalName());
        
        // Actualizar último acceso
        node.setLastAccessed(Instant.now());
        nodeRepository.save(node);
        
        return DownloadUrlDTO.builder()
                .url(url)
                .fileName(blob.getOriginalName())
                .mimeType(blob.getMimeType())
                .sizeBytes(blob.getSizeBytes())
                .expiresInSeconds((int) duration.getSeconds())
                .build();
    }

    @Transactional(readOnly = true)
    public DownloadUrlDTO getViewFileBlob(UUID id, Duration duration) {
        FileBlob blob = fileBlobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nodo no encontrado"));

        String url = s3Service.generatePresignedViewUrl(blob.getStorageKey(), duration, blob.getOriginalName(), blob.getMimeType());
                
        return DownloadUrlDTO.builder()
                .url(url)
                .fileName(blob.getOriginalName())
                .mimeType(blob.getMimeType())
                .sizeBytes(blob.getSizeBytes())
                .expiresInSeconds((int) duration.getSeconds())
                .build();
    }

    @Transactional(readOnly = true)
    public DownloadUrlDTO getDownloadFileBlob(UUID id, Duration duration) {
        FileBlob blob = fileBlobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nodo no encontrado"));

        String url = s3Service.generatePresignedDownloadUrl(blob.getStorageKey(), duration, blob.getOriginalName());
                
        return DownloadUrlDTO.builder()
                .url(url)
                .fileName(blob.getOriginalName())
                .mimeType(blob.getMimeType())
                .sizeBytes(blob.getSizeBytes())
                .expiresInSeconds((int) duration.getSeconds())
                .build();
    }

    private boolean isImageFile(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    private boolean existFileForName(List<UUID> listaDocsPorModulo, String name) {
        boolean exist = false;
    
        for (UUID uuid : listaDocsPorModulo) {
            FileBlob blobAux = fileBlobRepository.findById(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("File Blob no encontrado"));

            if (blobAux.getOriginalName().equals(name)) {
                exist = true;
                return true;
            }

        }
        return exist;
    }

}
