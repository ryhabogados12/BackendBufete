package com.bufete.backend.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bufete.backend.Dtos.cliente.ClienteDTO;
import com.bufete.backend.Dtos.juridico.*;
import com.bufete.backend.Dtos.proceso.ProcesoDTO;
import com.bufete.backend.model.*;
import com.bufete.backend.repository.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;

@Service
public class ProcesoPenalService {
    private final ProcesoRepository procesoRepository;
    private final ProcesoPenalRepository procesoPenalRepository;
    private final JurisdiccionRepository jurisdiccionRepository;
    private final AsuntoRepository asuntoRepositpry;
    private final EtapaProcesoRepository etapasRepository;
    private final TipoProcedimientoRepository TipoProcedimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final ProcesoPersonaRepository procesoPersonaRepository;
    private final DelitoRepository delitoRepository;
    private final BienJuridicoRepository bienesRepository;
    private final ProcesoPenalDelitoRepository delitosProcesoRepository;
    private final ProcesoPenalBienJuridicoRepository bienesProcesoRepository;
    private final ProcesoPenalImputacionRepository imputacionesRepository;
    private final ProcesoFiscalRepository fiscalesRepository;
    private final ProcesoJuzgadoRepository juzgadosRepository;
    private final ExpedienteRepository expedientesRepository;
    private final NodeRepository nodesRepository;
    private final PlantillaExpedienteRepository PlantillaExpedienteRepository;
    private final PlantillaExpedienteItemRepository plantillaExpedienteItemRepository;
    private final EventoRepository eventoRepository;
    private final ProcesoActuacionRepository procesoActuacionRepository;
    private final TerminoProcesoRepository terminoProcesoRepository;

    public ProcesoPenalService(ProcesoRepository procesos, ProcesoPenalRepository penales,
            JurisdiccionRepository jurisdicciones, AsuntoRepository asuntos, EtapaProcesoRepository etapas,
            TipoProcedimientoRepository procedimientos, UsuarioRepository usuarios, PersonaRepository personas,
            ProcesoPersonaRepository participantes, DelitoRepository delitos, BienJuridicoRepository bienes,
            ProcesoPenalDelitoRepository delitosProceso, ProcesoPenalBienJuridicoRepository bienesProceso,
            ProcesoPenalImputacionRepository imputaciones, ProcesoFiscalRepository fiscales,
            ProcesoJuzgadoRepository juzgados, ExpedienteRepository expedientes, NodeRepository nodes,
            PlantillaExpedienteRepository plantillas, PlantillaExpedienteItemRepository items,
            EventoRepository eventos, ProcesoActuacionRepository actuaciones, TerminoProcesoRepository terminos) {
        this.procesoRepository = procesos;
        this.procesoPenalRepository = penales;
        this.jurisdiccionRepository = jurisdicciones;
        this.asuntoRepositpry = asuntos;
        this.etapasRepository = etapas;
        this.TipoProcedimientoRepository = procedimientos;
        this.usuarioRepository = usuarios;
        this.personaRepository = personas;
        this.procesoPersonaRepository = participantes;
        this.delitoRepository = delitos;
        this.bienesRepository = bienes;
        this.delitosProcesoRepository = delitosProceso;
        this.bienesProcesoRepository = bienesProceso;
        this.imputacionesRepository = imputaciones;
        this.fiscalesRepository = fiscales;
        this.juzgadosRepository = juzgados;
        this.expedientesRepository = expedientes;
        this.nodesRepository = nodes;
        this.PlantillaExpedienteRepository = plantillas;
        this.plantillaExpedienteItemRepository = items;
        this.eventoRepository = eventos;
        this.procesoActuacionRepository = actuaciones;
        this.terminoProcesoRepository = terminos;
    }

    @Transactional
    public ProcesoPenalResponse crear(CrearAsuntoPenalRequest request, Long usuarioId) {
        Jurisdiccion jurisdiccion = require(jurisdiccionRepository, request.getJurisdiccionId(), "Jurisdicción");
        Asunto asunto = require(asuntoRepositpry, request.getAsuntoId(), "Asunto");
        EtapaProceso etapa = require(etapasRepository, request.getEtapaId(), "Etapa");
        TipoProcedimiento procedimiento = require(TipoProcedimientoRepository, request.getTipoProcedimientoId(),
                "Procedimiento");
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (!asunto.getJurisdiccion().getId().equals(jurisdiccion.getId())
                || !procedimiento.getAsunto().getId().equals(asunto.getId())
                || !procedimiento.getEtapa().getId().equals(etapa.getId())) {
            throw new ValidationException("La jurisdicción, asunto, etapa y procedimiento no son compatibles");
        }
        if (request.getNunc() != null && procesoPenalRepository.findByNunc(request.getNunc()).isPresent()) {
            throw new ValidationException("Ya existe un proceso penal con ese NUNC");
        }

        Proceso proceso = new Proceso();
        proceso.setNumeroProceso(request.getNunc());
        proceso.setNombre(asunto.getNombre());
        proceso.setTipoProceso("PENAL");
        proceso.setEstado(Proceso.EstadoProceso.ACTIVO);
        proceso.setFechaInicio(LocalDate.now());
        proceso.setJurisdiccion(jurisdiccion);
        proceso.setAsunto(asunto);
        proceso.setEtapaActual(etapa);
        proceso.setTipoProcedimiento(procedimiento);
        proceso.setAbogadoResponsable(usuario);
        proceso.setCreatedBy(usuario);
        proceso.setActivo(true);
        proceso = procesoRepository.save(proceso);

        ProcesoPenal penal = new ProcesoPenal();
        penal.setProceso(proceso);
        penal.setNunc(request.getNunc());
        penal.setHuboVictimas(request.isHuboVictimas());

        penal.setEtapaProceso(request.getEtapaProceso());
        penal.setCompetenciaAsunto(request.getCompetenciaAsunto());
        penal.setTipoJuzgado(request.getTipoJuzgado());

        penal.setCreatedAt(Instant.now());
        penal.setUpdatedAt(Instant.now());
        procesoPenalRepository.save(penal);

        saveParticipants(proceso, request.getImputados(), "IMPUTADO");
        saveParticipants(proceso, request.getVictimas(), "VICTIMA");
        for (Integer delitoId : safe(request.getDelitosIds())) {
            Delito delito = require(delitoRepository, delitoId, "Delito");
            delitosProcesoRepository.save(new ProcesoPenalDelito(proceso.getId(), delito.getId(), penal, delito, null));
        }
        for (Integer bienId : safe(request.getBienesJuridicosIds())) {
            BienJuridico bien = require(bienesRepository, bienId, "Bien jurídico");
            bienesProcesoRepository.save(new ProcesoPenalBienJuridico(proceso.getId(), bien.getId(), penal, bien));
        }
        if (request.getImputacion() != null) {
            ProcesoPenalImputacion imputacion = new ProcesoPenalImputacion();
            imputacion.setProcesoPenal(penal);
            imputacion.setDescripcion(request.getImputacion());
            imputacion.setFechaImputacion(LocalDate.now());
            imputacion.setCreatedAt(Instant.now());
            imputacion.setUpdatedAt(Instant.now());
            imputacionesRepository.save(imputacion);
        }
        if (request.getFiscal() != null) {
            FiscalRequest source = request.getFiscal();
            ProcesoFiscal fiscal = new ProcesoFiscal();
            fiscal.setProceso(proceso);
            fiscal.setNombreFiscal(source.getNombreFiscal());
            fiscal.setUnidad(source.getUnidad());
            fiscal.setNumeroFiscalia(source.getNumeroFiscalia());
            fiscal.setCorreo(source.getCorreo());
            fiscal.setTelefono(source.getTelefono());

            fiscal.setCreatedAt(Instant.now());
            fiscal.setUpdatedAt(Instant.now());
            fiscalesRepository.save(fiscal);
        }
        for (JuzgadoRequest source : safe(request.getJuzgados())) {
            ProcesoJuzgado juzgado = new ProcesoJuzgado();
            juzgado.setProceso(proceso);
            juzgado.setTipoJuzgado(source.getTipoJuzgado());
            juzgado.setNombreJuzgado(source.getNombreJuzgado());
            juzgado.setCreatedAt(Instant.now());
            juzgado.setUpdatedAt(Instant.now());
            juzgadosRepository.save(juzgado);
        }

        Expediente expediente = new Expediente();
        expediente.setNombre(request.getNunc());
        expediente.setProceso(proceso);
        expediente.setCreatedBy(usuario);
        expediente.setEstado(Expediente.EstadoExpediente.ACTIVO);
        expediente.setIsDeleted(false);
        expediente = expedientesRepository.save(expediente);
        crearArbolSiEsNecesario(expediente, asunto, usuario);
        return ProcesoPenalResponse.builder().procesoId(proceso.getId()).expedienteId(expediente.getId())
                .jurisdiccionId(jurisdiccion.getId()).asuntoId(asunto.getId()).etapaId(etapa.getId())
                .tipoProcedimientoId(procedimiento.getId()).nunc(penal.getNunc()).huboVictimas(penal.getHuboVictimas())
                .participantesIds(procesoPersonaRepository.findByProcesoIdOrderByOrdenAsc(proceso.getId()).stream()
                        .map(ProcesoPersona::getId).toList())
                .delitosIds(safe(request.getDelitosIds())).bienesJuridicosIds(safe(request.getBienesJuridicosIds()))
                .rootNodeId(expediente.getRootNodeId() == null ? null : expediente.getRootNodeId().toString()).build();
    }

    @Transactional(readOnly = true)
    public AsuntoPenalDetalleDTO obtenerDetalle(Long procesoId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado"));
        ProcesoPenal penal = procesoPenalRepository.findById(procesoId)
                .orElseThrow(() -> new EntityNotFoundException("El proceso no tiene información penal"));


        return AsuntoPenalDetalleDTO.builder()
                .proceso(AsuntoPenalDetalleDTO.ProcesoDetalle.builder().id(proceso.getId())
                        .numeroProceso(proceso.getNumeroProceso())
                        .estado(proceso.getEstado().name())
                        .fechaInicio(proceso.getFechaInicio())
                        .fechaCierre(proceso.getFechaCierre())
                        .jurisdiccionNombre(
                                proceso.getJurisdiccion() == null ? null : proceso.getJurisdiccion().getNombre())
                        .asuntoNombre(proceso.getAsunto() == null ? null : proceso.getAsunto().getNombre())
                        .etapaActualNombre(
                                proceso.getEtapaActual() == null ? null : proceso.getEtapaActual().getNombre())
                        .tipoProcedimientoNorma(proceso.getTipoProcedimiento() == null ? null
                                : proceso.getTipoProcedimiento().getNorma())
                        .abogadoResponsableNombre(nombreCompleto(proceso.getAbogadoResponsable())).build())
                .procesoPenal(AsuntoPenalDetalleDTO.ProcesoPenalDetalle.builder().procesoId(penal.getProcesoId())
                        .nunc(penal.getNunc())
                        .competenciaMunicipal(penal.getEtapaProceso())
                        .huboVictimas(penal.getHuboVictimas()).build())
                .imputacion(imputacionesRepository.findByProcesoPenalProcesoId(procesoId)
                        .map(item -> AsuntoPenalDetalleDTO.ImputacionDetalle.builder()
                                .descripcion(item.getDescripcion()).build())
                        .orElse(null))
                /* .participantes(procesoPersonaRepository.findByProcesoIdOrderByOrdenAsc(procesoId).stream()
                        .map(this::mapParticipante).toList()) */
                .participantes(procesoPersonaRepository.findByProcesoIdAndRolOrderByOrdenAsc(procesoId, "IMPUTADO").stream()
                        .map(this::mapParticipante).toList())
                .delitos(delitosProcesoRepository.findByProcesoId(procesoId).stream()
                        .map(item -> AsuntoPenalDetalleDTO.CatalogoRelacionDetalle.builder()
                                .id(item.getDelito().getId()).codigo(item.getDelito().getCodigo())
                                .nombre(item.getDelito().getNombre())
                                .descripcion(item.getDelito().getDescripcion())
                                .build())
                        .toList())
                .bienesJuridicos(bienesProcesoRepository.findByProcesoId(procesoId).stream()
                        .map(item -> AsuntoPenalDetalleDTO.CatalogoRelacionDetalle.builder()
                                .id(item.getBienJuridico().getId()).codigo(item.getBienJuridico().getCodigo())
                                .nombre(item.getBienJuridico().getNombre())
                                .descripcion(item.getBienJuridico().getDescripcion()).build())
                        .toList())
                .fiscal(fiscalesRepository.findByProcesoId(procesoId)
                        .map(item -> AsuntoPenalDetalleDTO.FiscalDetalle.builder().id(item.getId())
                                .nombreFiscal(item.getNombreFiscal()).unidad(item.getUnidad())
                                .correo(item.getCorreo()).telefono(item.getTelefono()).build())
                        .orElse(null))
                .juzgados(juzgadosRepository.findByProcesoId(procesoId).stream()
                        .map(item -> AsuntoPenalDetalleDTO.JuzgadoDetalle.builder()
                                .id(item.getId()).tipoJuzgado(item.getTipoJuzgado())
                                .nombreJuzgado(item.getNombreJuzgado()).build())
                        .toList())
                .actuaciones(procesoActuacionRepository.findByProcesoIdOrderByFechaActuacionAsc(procesoId).stream()
                        .map(this::mapActuacion).toList())
                .terminos(terminoProcesoRepository.findByActuacionProcesoIdOrderByFechaVencimientoAsc(procesoId)
                        .stream().map(this::mapTermino).toList())
                .eventos(eventoRepository.findByProcesoIdAndIsDeletedFalse(procesoId).stream().map(this::mapEvento)
                        .toList())
                .build();
    }

    private AsuntoPenalDetalleDTO.ParticipanteDetalle mapParticipante(ProcesoPersona item) {
        Persona persona = item.getPersona();
        return AsuntoPenalDetalleDTO.ParticipanteDetalle.builder().id(item.getId()).rol(item.getRol())
                .privadoLibertad(item.getPrivadoLibertad()).tipoDetencion(item.getTipoDetencion())
                .establecimientoOResidencia(item.getEstablecimientoOResidencia())
                .personaId(persona.getId()).tipoDocumento(persona.getTipoDocumento())
                .numeroDocumento(persona.getNumeroDocumento())
                .nombres(persona.getNombres()).apellidos(persona.getApellidos())
                .lugarResidencia(persona.getLugarResidencia())
                .email(persona.getEmail()).telefono(persona.getTelefono())
                .build();
    }

    private ActuacionDTO mapActuacion(ProcesoActuacion item) {
        return ActuacionDTO.builder().id(item.getId()).procesoId(item.getProceso().getId())
                .expedienteId(item.getExpediente().getId())
                .tipoActuacionId(item.getTipoActuacion().getId())
                .tipoActuacionCodigo(item.getTipoActuacion().getCodigo())
                .tipoActuacionNombre(item.getTipoActuacion().getNombre()).fechaActuacion(item.getFechaActuacion())
                .contexto(item.getContexto()).observaciones(item.getObservaciones()).build();
    }

    private TerminoDTO mapTermino(TerminoProceso item) {
        return TerminoDTO.builder().id(item.getId()).actuacionId(item.getActuacion().getId())
                .reglaTerminoId(item.getReglaTermino().getId())
                .fechaInicio(item.getFechaInicio()).diasPlazo(item.getDiasPlazo())
                .fechaVencimiento(item.getFechaVencimiento())
                .eventoId(item.getEvento() == null ? null : item.getEvento().getId()).build();
    }

    private AsuntoPenalDetalleDTO.EventoDetalle mapEvento(Evento item) {
        return AsuntoPenalDetalleDTO.EventoDetalle.builder().id(item.getId()).titulo(item.getTitulo())
                .descripcion(item.getDescripcion())
                .tipoEventoId(item.getTipoEvento().getId()).tipoEventoNombre(item.getTipoEvento().getNombre())
                .fechaInicio(item.getFechaInicio())
                .fechaFin(item.getFechaFin()).allDay(item.getAllDay())
                .expedienteId(item.getExpediente() == null ? null : item.getExpediente().getId())
                .terminoProcesoId(null).build();
    }

    private String nombreCompleto(Usuario usuario) {
        if (usuario == null)
            return null;
        return String.join(" ", java.util.Arrays.asList(usuario.getNombre(), usuario.getApellido())).trim();
    }

    private void saveParticipants(Proceso proceso, List<PersonaRequest> sources, String role) {

        if (sources == null)
            return;
        IntStream.range(0, sources.size()).forEach(index -> {
            PersonaRequest source = sources.get(index);
            Persona persona = new Persona();

            Persona persona2 = personaRepository.findByNumeroDocumento(source.getNumeroDocumento());

            if (persona2 != null) {
                persona = persona2;
            } else {
                persona.setTipoDocumento(source.getTipoDocumento());
                persona.setNumeroDocumento(source.getNumeroDocumento());
                persona.setNombres(source.getNombres());
                persona.setApellidos(source.getApellidos());
                persona.setLugarResidencia(source.getLugarResidencia());
                persona.setEmail(source.getEmail());
                persona.setTelefono(source.getTelefono());
                persona.setFechaNacimiento(source.getFechaNacimiento());
                persona.setCreatedAt(Instant.now());
                persona.setUpdatedAt(Instant.now());
            }
            persona = personaRepository.save(persona);

            ProcesoPersona participant = new ProcesoPersona();
            participant.setProceso(proceso);
            participant.setPersona(persona);
            participant.setRol(role);
            participant.setOrden(index + 1);
            participant.setPrivadoLibertad(source.getPrivadoLibertad());
            participant.setTipoDetencion(source.getTipoDetencion());
            participant.setEstablecimientoOResidencia(source.getEstablecimientoOResidencia());
            participant.setCreatedAt(Instant.now());

            procesoPersonaRepository.save(participant);
        });
    }

    private void crearArbolSiEsNecesario(Expediente expediente, Asunto asunto, Usuario usuario) {
        if (nodesRepository.findRootNodeByExpedienteId(expediente.getId()).isPresent()) {
            Node root = nodesRepository.findRootNodeByExpedienteId(expediente.getId()).orElseThrow();
            expediente.setRootNodeId(root.getId());
            expediente.setPlantillaExpedienteId(null);
            expedientesRepository.save(expediente);
            return;
        }
        PlantillaExpediente plantilla = PlantillaExpedienteRepository
                .findFirstByAsuntoIdAndActivoTrueOrderByIdAsc(asunto.getId()).orElse(null);
        if (plantilla == null)
            return;
        expediente.setPlantillaExpedienteId(plantilla.getId());
        List<PlantillaExpedienteItem> all = plantillaExpedienteItemRepository
                .findByPlantillaIdAndActivoTrueOrderByOrdenAscNombreAsc(plantilla.getId());
        java.util.Map<Long, Node> created = new java.util.HashMap<>();
        for (PlantillaExpedienteItem item : all) {
            Node node = new Node();
            node.setExpediente(expediente);
            node.setType(Node.NodeType.FOLDER);
            node.setName(item.getNombre());
            node.setModulo(Node.Modulo.DOCUMENTAL);
            node.setCreatedBy(usuario);
            node.setPlantillaItemId(item.getId());
            node.setParent(item.getParentItem() == null ? null : created.get(item.getParentItem().getId()));
            node = nodesRepository.save(node);
            created.put(item.getId(), node);
            if (item.getParentItem() == null && expediente.getRootNodeId() == null)
                expediente.setRootNodeId(node.getId());
        }
        expedientesRepository.save(expediente);
    }

    private <T, ID> T require(org.springframework.data.jpa.repository.JpaRepository<T, ID> repository, ID id,
            String name) {
        if (id == null)
            throw new ValidationException(name + " es obligatorio");
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(name + " no encontrado"));
    }

    private <T> List<T> safe(List<T> source) {
        return source == null ? Collections.emptyList() : source;
    }

}