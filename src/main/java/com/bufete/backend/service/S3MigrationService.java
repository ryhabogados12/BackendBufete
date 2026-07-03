package com.bufete.backend.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bufete.backend.Dtos.expediente.CreateExpedienteRequest;
import com.bufete.backend.Dtos.expediente.ExpedienteDTO;
import com.bufete.backend.Dtos.migracion.S3ObjectSummary;
import com.bufete.backend.Dtos.proceso.CreateProcesoRequest;
import com.bufete.backend.Dtos.proceso.ProcesoDTO;
import com.bufete.backend.model.*;
import com.bufete.backend.repository.*;
import com.bufete.backend.utils.MigrationReport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3MigrationService {
    
    private final S3Client s3Client;
    private final UsuarioRepository usuarioRepository;
    private final ProcesoRepository procesoRepository;
    private final ExpedienteRepository expedienteRepository;
    private final NodeRepository nodeRepository;
    private final FileBlobRepository fileBlobRepository;
    private final FileVersionRepository fileVersionRepository;
    private final ProcesoService procesoService;
    private final ExpedienteService expedienteService;
    private final ClienteRepository clienteRepository;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    /**
     * Método principal de migración
     */
    @Transactional
    public MigrationReport migrateS3Structure() {
        log.info("========================================");
        log.info("INICIANDO MIGRACIÓN DE ESTRUCTURA S3");
        log.info("Bucket: {}", bucketName);
        log.info("========================================");
        
        MigrationReport report = new MigrationReport();
        report.setStartTime(Instant.now());
        
        try {
            // PASO 1: Listar todos los objetos en S3
            log.info("PASO 1: Listando objetos en S3...");
            List<S3ObjectSummary> s3Objects = listAllS3Objects();
            log.info("Total de objetos encontrados: {}", s3Objects.size());
            report.setTotalFiles(s3Objects.size());
            
            if (s3Objects.isEmpty()) {
                log.warn("No se encontraron archivos para migrar");
                report.complete();
                return report;
            }
            
            // PASO 2: Agrupar por usuario y SPOA
            log.info("PASO 2: Agrupando archivos por usuario y SPOA...");
            Map<String, List<S3ObjectSummary>> groupedByUserAndSpoa = 
                groupByUserAndSpoa(s3Objects);
            report.setTotalGroups(groupedByUserAndSpoa.size());
            log.info("Total de grupos (usr-X/SPOA-Y): {}", groupedByUserAndSpoa.size());
            
            // PASO 3: Procesar cada grupo
            log.info("PASO 3: Procesando grupos...");
            int groupIndex = 0;
            for (Map.Entry<String, List<S3ObjectSummary>> entry : 
                 groupedByUserAndSpoa.entrySet()) {
                
                groupIndex++;
                String groupKey = entry.getKey();
                List<S3ObjectSummary> files = entry.getValue();
                
                log.info("Procesando grupo {}/{}: {} ({} archivos)", 
                         groupIndex, 
                         groupedByUserAndSpoa.size(), 
                         groupKey, 
                         files.size());
                
                try {
                    migrateUserSpoaGroup(groupKey, files, report);
                    report.incrementProcessedGroups();
                    log.info("✓ Grupo {} procesado exitosamente", groupKey);
                } catch (Exception e) {
                    log.error("✗ Error procesando grupo {}: {}", groupKey, e.getMessage(), e);
                    report.addError(groupKey, e.getMessage());
                }
            }
            
            report.complete();
            log.info("========================================");
            log.info("MIGRACIÓN COMPLETADA");
            log.info("Grupos procesados: {}/{}", report.getProcessedGroups(), report.getTotalGroups());
            log.info("Archivos migrados: {}/{}", report.getMigratedFiles(), report.getTotalFiles());
            log.info("Errores: {}", report.getErrors().size());
            log.info("Duración: {} segundos", report.getDurationSeconds());
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("Error crítico durante la migración", e);
            report.addError("MIGRATION_CRITICAL", e.getMessage());
            report.complete();
        }
        
        return report;
    }
    
    /**
     * Lista todos los objetos en S3 usando paginación
     */
    private List<S3ObjectSummary> listAllS3Objects() {
        List<S3ObjectSummary> allObjects = new ArrayList<>();
        
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("documentos/usr-"); // Solo carpetas de usuarios
            
            ListObjectsV2Request request = requestBuilder.build();
            ListObjectsV2Response response;
            
            int pageCount = 0;
            do {
                pageCount++;
                log.debug("Listando página {} de objetos...", pageCount);
                
                response = s3Client.listObjectsV2(request);
                
                for (S3Object s3Object : response.contents()) {
                    // Ignorar "carpetas" vacías (keys que terminan en /)
                    if (!s3Object.key().endsWith("/")) {
                        allObjects.add(new S3ObjectSummary(
                            s3Object.key(),
                            s3Object.size(),
                            s3Object.lastModified(),
                            s3Object.eTag()
                        ));
                    }
                }
                
                // Preparar siguiente página si existe
                if (response.isTruncated()) {
                    request = requestBuilder
                        .continuationToken(response.nextContinuationToken())
                        .build();
                }
                
            } while (response.isTruncated());
            
            log.info("Listado completo: {} páginas, {} objetos", pageCount, allObjects.size());
            
        } catch (S3Exception e) {
            log.error("Error listando objetos de S3: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Error accediendo a S3: " + e.awsErrorDetails().errorMessage());
        }
        
        return allObjects;
    }
    
    /**
     * Agrupa objetos por usuario y SPOA
     */
    private Map<String, List<S3ObjectSummary>> groupByUserAndSpoa(
            List<S3ObjectSummary> objects) {
        
        return objects.stream()
            .filter(obj -> {
                String[] parts = obj.getKey().split("/");
                return parts.length >= 3; // Mínimo: documentos/usr-X/SPOA
            })
            .collect(Collectors.groupingBy(obj -> {
                // documentos/usr-123/SPOA-456/subcarpeta/archivo.pdf
                String[] parts = obj.getKey().split("/");
                String userId = parts[1];   // usr-123
                String spoa = parts[2];     // SPOA-456 o número directo
                return userId + "_" + spoa;
            }));
    }
    
    /**
     * Procesa un grupo completo (usr-X + SPOA-Y)
     */
    @Transactional
    private void migrateUserSpoaGroup(
            String groupKey, 
            List<S3ObjectSummary> files,
            MigrationReport report) throws Exception {
        
        // Parsear groupKey: "usr-123_SPOA-456" o "usr-123_456"
        String[] parts = groupKey.split("_");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato de grupo inválido: " + groupKey);
        }
        
        String userIdStr = parts[0].replace("usr-", "");
        String spoaStr = parts[1].replace("SPOA-", "");
        
        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID de usuario inválido: " + userIdStr);
        }
        
        log.debug("  - Usuario ID: {}, SPOA: {}", userId, spoaStr);
        
        // PASO 3.1: Validar usuario
        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException(
                "Usuario " + userId + " no existe en DB. " +
                "Debe crearse antes de la migración."));
        
        log.debug("  - Usuario encontrado: {} {}", usuario.getNombre(), usuario.getApellido());
        
        // PASO 3.2: Crear o recuperar Proceso
        Proceso proceso = createOrGetProceso(spoaStr, usuario);
        log.debug("  - Proceso: {} (ID: {})", proceso.getNumeroProceso(), proceso.getId());
        
        // PASO 3.3: Crear o recuperar Expediente
        Expediente expediente = createOrGetExpediente(spoaStr, proceso, usuario);
        log.debug("  - Expediente: {} (ID: {})", expediente.getNombre(), expediente.getId());
        
        // PASO 3.4: Crear o recuperar Node root
        Node rootNode = createOrGetRootNode(expediente, usuario);
        log.debug("  - Nodo raíz: {} (ID: {})", rootNode.getName(), rootNode.getId());
        
        // PASO 3.5: Construir árbol de carpetas y archivos
        log.debug("  - Construyendo árbol de {} archivos...", files.size());
        buildNodeTree(files, rootNode, expediente, usuario, report);
        
        log.info("  ✓ Grupo completado: {} archivos procesados", files.size());
    }
    
    /**
     * Crea o recupera un Proceso
     */
    private Proceso createOrGetProceso(String spoa, Usuario usuario) {
        String numeroProceso = "PROC-" + spoa;
        
        Optional<Proceso> existing = procesoRepository.findByNumeroProceso(numeroProceso);
        if (existing.isPresent()) {
            log.debug("    Proceso existente encontrado: {}", numeroProceso);
            return existing.get();
        }
        
        // Crear cliente por defecto si no existe
        String clienteId = "MIGRADO-" + spoa;
        Cliente cliente = clienteRepository.findByIdentificacion(clienteId)
            .orElseGet(() -> {
                Cliente nuevoCliente = Cliente.builder()
                    .tipoCliente(Cliente.TipoCliente.NATURAL)
                    .nombre("Cliente migrado - " + spoa)
                    .identificacion(clienteId)
                    .tipoDocumento("CC")
                    .createdBy(usuario)
                    .activo(true)
                    .build();
                return clienteRepository.save(nuevoCliente);
            });
        
        CreateProcesoRequest request = CreateProcesoRequest.builder()
            .numeroProceso(numeroProceso)
            .nombre("Proceso migrado: " + spoa)
            .descripcion("Migrado automáticamente desde S3 el " + LocalDate.now())
            .tipoProceso("MIGRACION_S3")
            .clienteId(cliente.getIdentificacion())
            .tipoDocumentoCliente("CC")
            .abogadoResponsableId(usuario.getId())
            .fechaInicio(LocalDate.now())
            .build();
        
        ProcesoDTO dto = procesoService.createProceso(request, usuario.getId());
        log.debug("    Proceso creado: {}", numeroProceso);
        
        return procesoRepository.findById(dto.getId()).orElseThrow();
    }
    
    /**
     * Crea o recupera un Expediente
     */
    private Expediente createOrGetExpediente(
            String spoa, 
            Proceso proceso, 
            Usuario usuario) {
        
        // Buscar expediente por nombre y proceso
        List<Expediente> expedientes = expedienteRepository
            .findByProcesoIdAndIsDeletedFalse(proceso.getId());
        
        Optional<Expediente> existing = expedientes.stream()
            .filter(e -> spoa.equals(e.getNombre()))
            .findFirst();
        
        if (existing.isPresent()) {
            log.debug("    Expediente existente encontrado: {}", spoa);
            return existing.get();
        }
        
        CreateExpedienteRequest request = CreateExpedienteRequest.builder()
            .nombre(spoa)
            .descripcion("Expediente migrado desde S3")
            .procesoId(proceso.getId())
            .orden(1)
            .build();
        
        ExpedienteDTO dto = expedienteService.createExpediente(request, usuario.getId());
        log.debug("    Expediente creado: {}", spoa);
        
        return expedienteRepository.findById(dto.getId()).orElseThrow();
    }
    
    /**
     * Crea o recupera el nodo raíz del expediente
     */
    private Node createOrGetRootNode(Expediente expediente, Usuario usuario) {
        if (expediente.getRootNodeId() != null) {
            Optional<Node> existing = nodeRepository.findById(expediente.getRootNodeId());
            if (existing.isPresent()) {
                log.debug("    Nodo raíz existente encontrado");
                return existing.get();
            }
        }
        
        Node rootNode = Node.builder()
            .expediente(expediente)
            .parent(null)
            .type(Node.NodeType.FOLDER)
            .name("Raíz - " + expediente.getNombre())
            .modulo(Node.Modulo.DOCUMENTAL)
            .createdBy(usuario)
            .sizeBytes(0L)
            .itemCount(0)
            .isDeleted(false)
            .build();
        
        rootNode = nodeRepository.save(rootNode);
        
        expediente.setRootNodeId(rootNode.getId());
        expedienteRepository.save(expediente);
        
        log.debug("    Nodo raíz creado");
        return rootNode;
    }
    
    /**
     * Construye el árbol completo de nodos a partir de los archivos S3
     */
    private void buildNodeTree(
            List<S3ObjectSummary> files,
            Node rootNode,
            Expediente expediente,
            Usuario usuario,
            MigrationReport report) {
        
        // Mapa para cachear carpetas ya creadas (evitar duplicados)
        Map<String, Node> folderCache = new HashMap<>();
        folderCache.put("", rootNode); // Raíz vacía
        
        int fileIndex = 0;
        for (S3ObjectSummary fileObj : files) {
            fileIndex++;
            try {
                log.debug("    [{}/{}] Procesando: {}", 
                         fileIndex, files.size(), fileObj.getKey());
                
                migrateFile(fileObj, rootNode, expediente, usuario, folderCache);
                report.incrementMigratedFiles();
                
            } catch (Exception e) {
                log.error("    ✗ Error en archivo {}: {}", 
                         fileObj.getKey(), e.getMessage());
                report.addError(fileObj.getKey(), e.getMessage());
            }
        }
    }
    
    /**
     * Migra un archivo individual creando su nodo y metadata
     */
    @Transactional
    private void migrateFile(
            S3ObjectSummary fileObj,
            Node rootNode,
            Expediente expediente,
            Usuario usuario,
            Map<String, Node> folderCache) {
        
        // Ejemplo: documentos/usr-123/SPOA-456/subcarpeta1/documento.pdf
        String fullPath = fileObj.getKey();
        String[] parts = fullPath.split("/");
        
        if (parts.length < 4) {
            throw new IllegalArgumentException("Ruta inválida: " + fullPath);
        }
        
        // Extraer ruta relativa: subcarpeta1/documento.pdf
        String relativePath = String.join("/", 
            Arrays.copyOfRange(parts, 3, parts.length));
        
        String[] pathParts = relativePath.split("/");
        String fileName = pathParts[pathParts.length - 1];
        
        // PASO 1: Asegurar que todas las carpetas padres existen
        Node currentParent = rootNode;
        
        if (pathParts.length > 1) {
            // Hay subcarpetas
            String[] folders = Arrays.copyOfRange(pathParts, 0, pathParts.length - 1);
            
            String cumulativePath = "";
            for (String folderName : folders) {
                cumulativePath += "/" + folderName;
                final Node parentForLambda = currentParent;
                
                currentParent = folderCache.computeIfAbsent(
                    cumulativePath, 
                    k -> createFolderNode(folderName, parentForLambda, expediente, usuario)
                );
            }
        }
        
        Node parentNode = currentParent;
        
        // PASO 2: Verificar si el archivo ya existe
        Optional<Node> existingFile = nodeRepository
            .findByNameAndParentId(fileName, parentNode.getId());
        
        if (existingFile.isPresent()) {
            log.debug("      Archivo ya existe, omitiendo: {}", fileName);
            return;
        }
        
        // PASO 3: Crear Node para el archivo
        Node fileNode = createFileNode(fileName, parentNode, expediente, usuario);
        
        // PASO 4: Crear FileBlob (sin mover archivo en S3)
        FileBlob blob = createFileBlobFromS3(fileObj);
        
        // PASO 5: Crear FileVersion y enlazar
        FileVersion version = FileVersion.builder()
            .node(fileNode)
            .blob(blob)
            .versionNum(1)
            .uploadedBy(usuario)
            .note("Migrado desde S3")
            .isCurrent(true)
            .uploadedAt(fileObj.getLastModified())
            .build();
        
        version = fileVersionRepository.save(version);
        
        // PASO 6: Actualizar node con versión y tamaño
        fileNode.setCurrentVersion(version);
        fileNode.setSizeBytes(fileObj.getSize());
        nodeRepository.save(fileNode);
        
        // PASO 7: Actualizar metadata del padre
        updateParentMetadata(parentNode);
        
        log.debug("      ✓ Archivo migrado: {}", fileName);
    }
    
    /**
     * Crea un nodo de tipo carpeta
     */
    private Node createFolderNode(
            String name,
            Node parent,
            Expediente expediente,
            Usuario usuario) {
        
        // Verificar si ya existe
        Optional<Node> existing = nodeRepository
            .findByNameAndParentId(name, parent.getId());
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        Node folder = Node.builder()
            .expediente(expediente)
            .parent(parent)
            .type(Node.NodeType.FOLDER)
            .name(name)
            .modulo(Node.Modulo.DOCUMENTAL)
            .createdBy(usuario)
            .sizeBytes(0L)
            .itemCount(0)
            .isDeleted(false)
            .build();
        
        folder = nodeRepository.save(folder);
        
        // Actualizar contador del padre
        updateParentMetadata(parent);
        
        return folder;
    }
    
    /**
     * Crea un nodo de tipo archivo
     */
    private Node createFileNode(
            String name,
            Node parent,
            Expediente expediente,
            Usuario usuario) {
        
        Node file = Node.builder()
            .expediente(expediente)
            .parent(parent)
            .type(Node.NodeType.FILE)
            .name(name)
            .modulo(Node.Modulo.DOCUMENTAL)
            .createdBy(usuario)
            .sizeBytes(0L)
            .isDeleted(false)
            .build();
        
        return nodeRepository.save(file);
    }
    
    /**
     * Crea FileBlob desde metadata de S3 (sin descargar)
     */
    private FileBlob createFileBlobFromS3(S3ObjectSummary s3Obj) {
        // Limpiar ETag (viene con comillas)
        String eTag = s3Obj.getETag().replace("\"", "");
        
        // Determinar checksum
        String checksum;
        if (eTag.contains("-")) {
            // Archivo multipart: ETag no es MD5 directo
            checksum = "multipart_" + eTag;
            log.debug("      Archivo multipart detectado, ETag: {}", eTag);
        } else {
            // Archivo estándar: ETag es MD5
            checksum = eTag;
        }
        
        // Buscar si ya existe (deduplicación)
        Optional<FileBlob> existing = fileBlobRepository
            .findByChecksumSha256AndSizeBytes(checksum, s3Obj.getSize());
        
        if (existing.isPresent()) {
            log.debug("      ✓ Archivo duplicado encontrado (deduplicación)");
            return existing.get();
        }
        
        // Determinar MIME type
        String mimeType = determineMimeType(s3Obj.getKey());
        
        FileBlob blob = FileBlob.builder()
            .storageKey(s3Obj.getKey()) // ⚠️ CRÍTICO: mantener ruta original
            .bucketName(bucketName)
            .sizeBytes(s3Obj.getSize())
            .checksumSha256(checksum)
            .mimeType(mimeType)
            .originalName(extractFileName(s3Obj.getKey()))
            .isImage(mimeType.startsWith("image/"))
            .createdAt(s3Obj.getLastModified())
            .build();
        
        return fileBlobRepository.save(blob);
    }
    
    /**
     * Determina el MIME type por extensión
     */
    private String determineMimeType(String key) {
        String lowerKey = key.toLowerCase();
        
        if (lowerKey.endsWith(".pdf")) return "application/pdf";
        if (lowerKey.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lowerKey.endsWith(".doc")) return "application/msword";
        if (lowerKey.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lowerKey.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lowerKey.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (lowerKey.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lowerKey.endsWith(".jpg") || lowerKey.endsWith(".jpeg")) return "image/jpeg";
        if (lowerKey.endsWith(".png")) return "image/png";
        if (lowerKey.endsWith(".gif")) return "image/gif";
        if (lowerKey.endsWith(".txt")) return "text/plain";
        if (lowerKey.endsWith(".csv")) return "text/csv";
        if (lowerKey.endsWith(".zip")) return "application/zip";
        if (lowerKey.endsWith(".rar")) return "application/x-rar-compressed";
        
        return "application/octet-stream";
    }
    
    /**
     * Extrae el nombre del archivo de la key
     */
    private String extractFileName(String key) {
        return key.substring(key.lastIndexOf('/') + 1);
    }
    
    /**
     * Actualiza itemCount y sizeBytes del padre (recursivo)
     */
    private void updateParentMetadata(Node parent) {
        int itemCount = nodeRepository.countChildrenByParentId(parent.getId());
        long totalSize = nodeRepository.getTotalSizeByParentId(parent.getId());
        
        parent.setItemCount(itemCount);
        parent.setSizeBytes(totalSize);
        nodeRepository.save(parent);
        
        // Propagar hacia arriba
        if (parent.getParent() != null) {
            updateParentMetadata(parent.getParent());
        }
    }
}