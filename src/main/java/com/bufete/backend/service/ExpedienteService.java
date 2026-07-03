package com.bufete.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bufete.backend.Dtos.PageResponse;
import com.bufete.backend.Dtos.expediente.CreateExpedienteRequest;
import com.bufete.backend.Dtos.expediente.ExpedienteDTO;
import com.bufete.backend.model.Expediente;
import com.bufete.backend.model.Node;
import com.bufete.backend.model.Proceso;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.ExpedienteRepository;
import com.bufete.backend.repository.ProcesoRepository;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.utils.ExpedienteMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ExpedienteService {
    
    private final ExpedienteRepository expedienteRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NodeService nodeService;
    private final ExpedienteMapper expedienteMapper;
    
    public ExpedienteService(ExpedienteRepository expedienteRepository,
                           ProcesoRepository procesoRepository,
                           UsuarioRepository usuarioRepository,
                           NodeService nodeService,
                           ExpedienteMapper expedienteMapper) {
        this.expedienteRepository = expedienteRepository;
        this.procesoRepository = procesoRepository;
        this.usuarioRepository = usuarioRepository;
        this.nodeService = nodeService;
        this.expedienteMapper = expedienteMapper;
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
        return usuario.getId();
    }
    
    @Transactional(readOnly = true)
    public PageResponse<ExpedienteDTO> getAllExpedientes(int page, int size, String sortBy, String sortDir,
                                                       String nombre, Expediente.EstadoExpediente estado, 
                                                       Long procesoId) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Expediente> expedientesPage = expedienteRepository.findAllActive(pageable);
        
        List<ExpedienteDTO> expedientesDTOs = expedientesPage.getContent().stream()
                .map(expediente -> {
                    ExpedienteDTO dto = expedienteMapper.toDTO(expediente);
                    dto.setTotalDocumentos(expedienteRepository.countDocumentosByExpedienteId(expediente.getId()));
                    dto.setTotalSize(expedienteRepository.getTotalSizeByExpedienteId(expediente.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        
        return PageResponse.<ExpedienteDTO>builder()
                .content(expedientesDTOs)
                .page(expedientesPage.getNumber())
                .size(expedientesPage.getSize())
                .totalElements(expedientesPage.getTotalElements())
                .totalPages(expedientesPage.getTotalPages())
                .first(expedientesPage.isFirst())
                .last(expedientesPage.isLast())
                .empty(expedientesPage.isEmpty())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ExpedienteDTO> getAllExpedientesByAbogado(Authentication authentication) {
        Long abogadoId = getUserIdFromAuthentication(authentication);
        
        List<Expediente> expedientes = expedienteRepository.findByProcesoAbogadoResponsableIdAndIsDeletedFalse(abogadoId);
        return expedientes.stream()
                .map(expediente -> {
                    ExpedienteDTO dto = expedienteMapper.toDTO(expediente);
                    dto.setTotalDocumentos(expedienteRepository.countDocumentosByExpedienteId(expediente.getId()));
                    dto.setTotalSize(expedienteRepository.getTotalSizeByExpedienteId(expediente.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ExpedienteDTO getExpedienteById(Long id) {
        Expediente expediente = expedienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expediente no encontrado con ID: " + id));
        
        ExpedienteDTO dto = expedienteMapper.toDTO(expediente);
        dto.setTotalDocumentos(expedienteRepository.countDocumentosByExpedienteId(id));
        dto.setTotalSize(expedienteRepository.getTotalSizeByExpedienteId(id));
        
        return dto;
    }
    
    @Transactional(readOnly = true)
    public List<ExpedienteDTO> getExpedientesByProceso(Long procesoId) {
        List<Expediente> expedientes = expedienteRepository.findByProcesoIdAndIsDeletedFalse(procesoId);
        return expedientes.stream()
                .map(expediente -> {
                    ExpedienteDTO dto = expedienteMapper.toDTO(expediente);
                    dto.setTotalDocumentos(expedienteRepository.countDocumentosByExpedienteId(expediente.getId()));
                    dto.setTotalSize(expedienteRepository.getTotalSizeByExpedienteId(expediente.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    public ExpedienteDTO createExpediente(CreateExpedienteRequest request, Long createdById) {
        Proceso proceso = procesoRepository.findById(request.getProcesoId())
                .orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado con ID: " + request.getProcesoId()));
        
        Usuario createdBy = usuarioRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + createdById));
        
        Expediente expediente = expedienteMapper.toEntity(request);
        expediente.setProceso(proceso);
        expediente.setCreatedBy(createdBy);
        
        // Crear nodo raíz para el expediente
        UUID rootNodeId = nodeService.createRootNode(
                "Expediente: " + request.getNombre(),
                Node.Modulo.DOCUMENTAL,
                createdById
        );
        expediente.setRootNodeId(rootNodeId);
        
        Expediente savedExpediente = expedienteRepository.save(expediente);
        
        // Asociar el nodo raíz al expediente
        nodeService.associateNodeToExpediente(rootNodeId, savedExpediente);
        
        log.info("Expediente creado: {} (ID: {})", savedExpediente.getNombre(), savedExpediente.getId());
        
        return expedienteMapper.toDTO(savedExpediente);
    }
    
    public ExpedienteDTO updateExpediente(Long id, CreateExpedienteRequest request) {
        Expediente expediente = expedienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expediente no encontrado con ID: " + id));
        
        if (!request.getProcesoId().equals(expediente.getProceso().getId())) {
            Proceso proceso = procesoRepository.findById(request.getProcesoId())
                    .orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado con ID: " + request.getProcesoId()));
            expediente.setProceso(proceso);
        }
        
        expedienteMapper.updateEntity(expediente, request);
        Expediente updatedExpediente = expedienteRepository.save(expediente);
        log.info("Expediente actualizado: {} (ID: {})", updatedExpediente.getNombre(), updatedExpediente.getId());
        
        return expedienteMapper.toDTO(updatedExpediente);
    }
    
    public void deleteExpediente(Long id) {
        Expediente expediente = expedienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expediente no encontrado con ID: " + id));
        
        // Soft delete
        expediente.setIsDeleted(true);
        expedienteRepository.save(expediente);
        
        // Marcar todos los nodos del expediente como eliminados
        if (expediente.getRootNodeId() != null) {
            nodeService.softDeleteNodeTree(expediente.getRootNodeId());
        }
        
        log.info("Expediente eliminado: {} (ID: {})", expediente.getNombre(), expediente.getId());
    }
    
    public ExpedienteDTO updateEstadoExpediente(Long id, Expediente.EstadoExpediente nuevoEstado) {
        Expediente expediente = expedienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expediente no encontrado con ID: " + id));
        
        Expediente.EstadoExpediente estadoAnterior = expediente.getEstado();
        expediente.setEstado(nuevoEstado);
        
        // Si se cierra o archiva el expediente, establecer fecha de cierre
        if (nuevoEstado == Expediente.EstadoExpediente.CERRADO || 
            nuevoEstado == Expediente.EstadoExpediente.ARCHIVADO) {
            expediente.setFechaCierre(Instant.now());
        }
        
        Expediente updatedExpediente = expedienteRepository.save(expediente);
        log.info("Estado del expediente {} cambiado de {} a {}", 
                expediente.getNombre(), estadoAnterior, nuevoEstado);
        
        return expedienteMapper.toDTO(updatedExpediente);
    }
}