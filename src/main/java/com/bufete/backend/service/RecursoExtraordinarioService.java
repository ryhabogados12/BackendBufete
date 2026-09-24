package com.bufete.backend.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bufete.backend.Dtos.juridico.CrearRecursoExtraordinarioRequest;
import com.bufete.backend.Dtos.juridico.RecursoExtraordinarioDTO;
import com.bufete.backend.model.*;
import com.bufete.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;

@Service
public class RecursoExtraordinarioService {
    private final ProcesoRepository procesos;
    private final ExpedienteRepository expedientes;
    private final RecursoExtraordinarioRepository recursos;
    private final UsuarioRepository usuarios;
    private final NodeRepository nodes;

    public RecursoExtraordinarioService(ProcesoRepository procesos, ExpedienteRepository expedientes,
            RecursoExtraordinarioRepository recursos, UsuarioRepository usuarios, NodeRepository nodes) {
        this.procesos = procesos;
        this.expedientes = expedientes;
        this.recursos = recursos;
        this.usuarios = usuarios;
        this.nodes = nodes;
    }

    @Transactional
    public RecursoExtraordinarioDTO crear(Long procesoId, CrearRecursoExtraordinarioRequest request, Long abogadoId) {
        Proceso proceso = procesos.findById(procesoId).orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado"));
        Expediente expediente = expedientes.findById(request.getExpedienteId()).orElseThrow(() -> new EntityNotFoundException("Expediente no encontrado"));
        if (!procesoId.equals(expediente.getProceso().getId())) throw new ValidationException("El expediente no pertenece al proceso");
        if (recursos.findByExpedienteId(expediente.getId()).isPresent()) throw new ValidationException("El expediente ya tiene un recurso extraordinario");
        Usuario abogado = usuarios.findById(abogadoId).orElseThrow(() -> new EntityNotFoundException("Abogado no encontrado"));
        RecursoExtraordinario recurso = new RecursoExtraordinario();
        recurso.setProceso(proceso);
        recurso.setExpediente(expediente);
        recurso.setAbogado(abogado);
        recurso.setCreatedBy(abogado);
        recurso.setContexto(request.getContexto());
        recurso.setFechaPresentacion(request.getFechaPresentacion());
        if (request.getDocumentoNodeId() != null) recurso.setDocumentoNode(nodes.findById(request.getDocumentoNodeId()).orElseThrow(() -> new EntityNotFoundException("Nodo no encontrado")));
        try {
            recurso = recursos.save(recurso);
        } catch (DataIntegrityViolationException ex) {
            throw new ValidationException("El expediente ya tiene un recurso extraordinario");
        }
        return RecursoExtraordinarioDTO.builder().id(recurso.getId()).procesoId(procesoId).expedienteId(expediente.getId())
                .abogadoId(abogadoId).contexto(recurso.getContexto()).fechaPresentacion(recurso.getFechaPresentacion())
                .documentoNodeId(recurso.getDocumentoNode() == null ? null : recurso.getDocumentoNode().getId()).build();
    }
}