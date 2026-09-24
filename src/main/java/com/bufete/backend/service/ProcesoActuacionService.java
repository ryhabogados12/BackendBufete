package com.bufete.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bufete.backend.Dtos.FileUploadResponse;
import com.bufete.backend.Dtos.folder.CreateFolderRequest;
import com.bufete.backend.Dtos.folder.FileUploadRequest;
import com.bufete.backend.Dtos.folder.NodeDTO;
import com.bufete.backend.Dtos.juridico.ActuacionDTO;
import com.bufete.backend.Dtos.juridico.CrearActuacionRequest;
import com.bufete.backend.Dtos.juridico.TerminoDTO;
import com.bufete.backend.model.*;
import com.bufete.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;

@Service
public class ProcesoActuacionService {
    private final ProcesoRepository procesos;
    private final ExpedienteRepository expedientes;
    private final TipoActuacionRepository tipos;
    private final ProcesoActuacionRepository actuaciones;
    private final TerminoProcesoRepository terminos;
    private final UsuarioRepository usuarios;
    private final ActuacionDocumentoRepository actuacionDocumentos;
    private final NodeRepository nodes;
    private final NodeService nodeService;

    public ProcesoActuacionService(ProcesoRepository procesos, ExpedienteRepository expedientes,
            TipoActuacionRepository tipos, ProcesoActuacionRepository actuaciones,
            TerminoProcesoRepository terminos, UsuarioRepository usuarios,
            ActuacionDocumentoRepository actuacionDocumentos, NodeRepository nodes,
            NodeService nodeService) {
        this.procesos = procesos; this.expedientes = expedientes; this.tipos = tipos;
        this.actuaciones = actuaciones; this.terminos = terminos; this.usuarios = usuarios;
        this.actuacionDocumentos = actuacionDocumentos; this.nodes = nodes; this.nodeService = nodeService;
    }

    @Transactional
    public ActuacionDTO crear(Long procesoId, CrearActuacionRequest request, Long usuarioId) {
        Proceso proceso = procesos.findById(procesoId).orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado"));
        Expediente expediente = expedientes.findById(request.getExpedienteId()).orElseThrow(() -> new EntityNotFoundException("Expediente no encontrado"));
        if (!procesoId.equals(expediente.getProceso().getId())) throw new ValidationException("El expediente no pertenece al proceso");
        TipoActuacion tipo = tipos.findById(request.getTipoActuacionId()).orElseThrow(() -> new EntityNotFoundException("Tipo de actuación no encontrado"));
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (tipo.getAsunto() != null && proceso.getAsunto() != null && !tipo.getAsunto().getId().equals(proceso.getAsunto().getId())) {
            throw new ValidationException("La actuación no pertenece al asunto del proceso");
        }
        ProcesoActuacion objProcesoActuacion = new ProcesoActuacion(); 
        objProcesoActuacion.setProceso(proceso); 
        objProcesoActuacion.setExpediente(expediente);
        objProcesoActuacion.setTipoActuacion(tipo); 
        objProcesoActuacion.setFechaActuacion(request.getFechaActuacion()); 
        objProcesoActuacion.setContexto(request.getContexto());
        objProcesoActuacion.setObservaciones(request.getObservaciones()); 
        objProcesoActuacion.setCreatedBy(usuario);
        objProcesoActuacion.setCreatedAt(Instant.now());
        objProcesoActuacion.setUpdatedAt(Instant.now());;
        return map(actuaciones.save(objProcesoActuacion));
    }

    @Transactional(readOnly = true)
    public List<ActuacionDTO> listar(Long procesoId) { return actuaciones.findByProcesoIdOrderByFechaActuacionAsc(procesoId).stream().map(this::map).toList(); }

    @Transactional(readOnly = true)
    public List<TerminoDTO> listarTerminos(Long procesoId) {
        return terminos.findByActuacionProcesoIdOrderByFechaVencimientoAsc(procesoId).stream().map(item -> TerminoDTO.builder()
                .id(item.getId()).actuacionId(item.getActuacion().getId()).reglaTerminoId(item.getReglaTermino().getId())
                .fechaInicio(item.getFechaInicio()).diasPlazo(item.getDiasPlazo()).fechaVencimiento(item.getFechaVencimiento())
                .eventoId(item.getEvento() == null ? null : item.getEvento().getId()).build()).toList();
    }

        @Transactional
        public FileUploadResponse subirDocumento(Long procesoId, Long actuacionId, MultipartFile file,
            String description, String note, boolean esPrincipal, Long usuarioId) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Es necesario subir un archivo");
        }

        ProcesoActuacion actuacion = actuaciones.findById(actuacionId)
            .orElseThrow(() -> new EntityNotFoundException("Actuación no encontrada"));
        if (!procesoId.equals(actuacion.getProceso().getId())) {
            throw new ValidationException("La actuación no pertenece al proceso");
        }

        UUID rootNodeId = actuacion.getExpediente().getRootNodeId();
        Node expedienteRoot = rootNodeId == null
            ? nodes.findRootNodeByExpedienteId(actuacion.getExpediente().getId())
                .orElseThrow(() -> new EntityNotFoundException("El expediente no tiene carpeta raíz"))
            : nodes.findById(rootNodeId)
                .orElseThrow(() -> new EntityNotFoundException("La carpeta raíz del expediente no existe"));

        Node tipoActuacionFolder = nodes.findByNameAndParentId(
            actuacion.getTipoActuacion().getNombre(), expedienteRoot.getId())
            .orElseThrow(() -> new EntityNotFoundException("No existe la carpeta del tipo de actuación: "
                + actuacion.getTipoActuacion().getNombre()));

        Node actasFolder = nodes.findByNameAndParentId("Actas", tipoActuacionFolder.getId())
            .orElseGet(() -> {
                NodeDTO folder = nodeService.createFolder(CreateFolderRequest.builder()
                    .name("Actas")
                    .description("Documentos de actas de la actuación")
                    .parentId(tipoActuacionFolder.getId())
                    .build(), usuarioId);
                return nodes.findById(folder.getId())
                    .orElseThrow(() -> new EntityNotFoundException("No se pudo crear la carpeta Actas"));
            });

        FileUploadRequest request = FileUploadRequest.builder()
            .file(file)
            .parentId(actasFolder.getId())
            .description(description)
            .note(note)
            .build();
        FileUploadResponse response = nodeService.uploadFileToExpediente(request, usuarioId);

        Node node = nodes.findById(response.getNodeId())
            .orElseThrow(() -> new EntityNotFoundException("El nodo subido no existe"));
        if (esPrincipal) {
            actuacionDocumentos.findByActuacionId(actuacionId)
                .forEach(documento -> documento.setEsPrincipal(false));
        }

        ActuacionDocumento documento = new ActuacionDocumento();
        documento.setActuacion(actuacion);
        documento.setNode(node);
        documento.setEsPrincipal(esPrincipal);
        documento.setCreatedAt(Instant.now());
        actuacionDocumentos.save(documento);

        return response;
        }

    private ActuacionDTO map(ProcesoActuacion item) { return ActuacionDTO.builder().id(item.getId()).procesoId(item.getProceso().getId())
            .expedienteId(item.getExpediente().getId()).tipoActuacionId(item.getTipoActuacion().getId())
            .tipoActuacionCodigo(item.getTipoActuacion().getCodigo()).tipoActuacionNombre(item.getTipoActuacion().getNombre())
            .fechaActuacion(item.getFechaActuacion()).contexto(item.getContexto()).observaciones(item.getObservaciones()).build(); }
}