package com.bufete.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bufete.backend.Dtos.sentencia.CreateSentenciaRequest;
import com.bufete.backend.Dtos.sentencia.SentenciaDto;
import com.bufete.backend.Dtos.sentencia.SentenciaResponse;
import com.bufete.backend.Dtos.sentencia.UpdateSentenciaRequest;
import com.bufete.backend.model.Cliente;
import com.bufete.backend.model.Expediente;
import com.bufete.backend.model.FileBlob;
import com.bufete.backend.model.Proceso;
import com.bufete.backend.model.Sentencia;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.ClienteRepository;
import com.bufete.backend.repository.ExpedienteRepository;
import com.bufete.backend.repository.FileBlobRepository;
import com.bufete.backend.repository.ProcesoRepository;
import com.bufete.backend.repository.SentenciaRepository;
import com.bufete.backend.repository.UsuarioRepository;

@Service
public class SentenciaService {
    private final SentenciaRepository sentenciaRepository;
    private final ProcesoRepository procesoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final FileBlobRepository fileBlobRepository;
    private final ExpedienteRepository expedienteRepository;

    public SentenciaService(ClienteRepository clienteRepository, ExpedienteRepository expedienteRepository,
            FileBlobRepository fileBlobRepository, ProcesoRepository procesoRepository,
            SentenciaRepository sentenciaRepository, UsuarioRepository usuarioRepository) {
        this.sentenciaRepository = sentenciaRepository;
        this.procesoRepository = procesoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.fileBlobRepository = fileBlobRepository;
        this.expedienteRepository = expedienteRepository;

    }
    

    public List<SentenciaResponse> listarPorCliente(String idCliente) {

        List<SentenciaResponse> lista = new ArrayList<>();

        Cliente cliente = clienteRepository.findByIdentificacion(idCliente)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        

        List<Sentencia> listaAux = sentenciaRepository.findByClienteId(cliente.getId());
        for (Sentencia sentencia : listaAux) {
            SentenciaResponse resp = new SentenciaResponse(
                sentencia.getId(),
                sentencia.getNombre(),
                sentencia.getTipoSentencia(),
                sentencia.getFechaSentencia(),
                sentencia.getProceso().getNombre(),
                sentencia.getCliente().getIdentificacion(),
                sentencia.getAbogado().getNombre(),
                sentencia.getFileBlob().getId().toString(),
                sentencia.getCreatedBy().getId());
            lista.add(resp);
        }

        return lista;
    }

    public List<SentenciaResponse> listarPorTipoSentencia(String tipoSentencia) {

        List<SentenciaResponse> lista = new ArrayList<>();

        List<Sentencia> listaAux = sentenciaRepository.findByTipoSentencia(tipoSentencia);

        for (Sentencia sentencia : listaAux) {
            SentenciaResponse resp = new SentenciaResponse(
                sentencia.getId(),
                sentencia.getNombre(),
                sentencia.getTipoSentencia(),
                sentencia.getFechaSentencia(),
                sentencia.getProceso().getNombre(),
                sentencia.getCliente().getIdentificacion(),
                sentencia.getAbogado().getNombre(),
                sentencia.getFileBlob().getId().toString(),
                sentencia.getCreatedBy().getId());
            lista.add(resp);
        }

        return lista;
    }

    @Transactional
    public SentenciaResponse crearSentencia(CreateSentenciaRequest request, Long usuarioId) {
        Proceso proceso = procesoRepository.findById(request.getProcesoId())
                .orElseThrow(() -> new RuntimeException("Proceso no encontrado"));

        System.out.println("request.getClienteId(): " + request.getClienteId());

        Cliente cliente = clienteRepository.findByIdentificacion(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Usuario abogado = usuarioRepository.findById(request.getAbogadoId())
                .orElseThrow(() -> new RuntimeException("Abogado no encontrado"));

        FileBlob fileBlob = fileBlobRepository.findById(request.getFileBlobId())
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

        Usuario createdBy = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario creador no encontrado"));

        Sentencia saved = Sentencia.builder()
                .nombre(request.getNombre())
                .tipoSentencia(request.getTipoSentencia())
                .fechaSentencia(request.getFechaSentencia())
                .proceso(proceso)
                .cliente(cliente)
                .fileBlob(fileBlob)
                .abogado(abogado)
                .createdBy(createdBy)
                .estado(Sentencia.EstadoSentencia.PRIMERA_INSTANCIA)
                .isDeleted(false)
                .build();

        sentenciaRepository.save(saved);

        return new SentenciaResponse(
                saved.getId(),
                saved.getNombre(),
                saved.getTipoSentencia(),
                saved.getFechaSentencia(),
                saved.getProceso().getNombre(),
                saved.getCliente().getIdentificacion(),
                saved.getAbogado().getNombre(),
                saved.getFileBlob().getId().toString(),
                saved.getCreatedBy().getId());
    }

    @Transactional
    public Sentencia actualizarSentencia(Long id, UpdateSentenciaRequest request) {
        Sentencia sentencia = sentenciaRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Sentencia no encontrada"));

        if (request.getNumeroSentencia() != null) {
            sentencia.setNumeroSentencia(request.getNumeroSentencia());
        }
        if (request.getNombre() != null) {
            sentencia.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null) {
            sentencia.setDescripcion(request.getDescripcion());
        }
        if (request.getTipoSentencia() != null) {
            sentencia.setTipoSentencia(request.getTipoSentencia());
        }
        if (request.getFechaSentencia() != null) {
            sentencia.setFechaSentencia(request.getFechaSentencia());
        }
        if (request.getFechaNotificacion() != null) {
            sentencia.setFechaNotificacion(request.getFechaNotificacion());
        }
        if (request.getJuzgado() != null) {
            sentencia.setJuzgado(request.getJuzgado());
        }
        if (request.getMagistrado() != null) {
            sentencia.setMagistrado(request.getMagistrado());
        }
        if (request.getExpedienteId() != null) {
            Expediente expediente = expedienteRepository.findById(request.getExpedienteId())
                    .orElseThrow(() -> new RuntimeException("Expediente no encontrado"));
            sentencia.setExpediente(expediente);
        }
        if (request.getEstado() != null) {
            sentencia.setEstado(request.getEstado());
        }
        if (request.getEsFavorable() != null) {
            sentencia.setEsFavorable(request.getEsFavorable());
        }
        if (request.getObservaciones() != null) {
            sentencia.setObservaciones(request.getObservaciones());
        }
        if (request.getFileBlobId() != null) {
            FileBlob fileBlob = fileBlobRepository.findById(request.getFileBlobId())
                    .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));
            sentencia.setFileBlob(fileBlob);
        }

        return sentenciaRepository.save(sentencia);
    }

    @Transactional
    public void eliminarSentencia(Long id) {
        Sentencia sentencia = sentenciaRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Sentencia no encontrada"));

        sentencia.setIsDeleted(true);
        sentenciaRepository.save(sentencia);
    }

    public Sentencia obtenerPorId(Long id) {
        return sentenciaRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Sentencia no encontrada"));
    }

    public List<Sentencia> listarTodas() {
        return sentenciaRepository.findAllNotDeleted();
    }
}
