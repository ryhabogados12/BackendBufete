package com.bufete.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bufete.backend.Dtos.juridico.CatalogoDTO;
import com.bufete.backend.model.Asunto;
import com.bufete.backend.model.BienJuridico;
import com.bufete.backend.model.Delito;
import com.bufete.backend.model.EtapaProceso;
import com.bufete.backend.model.Jurisdiccion;
import com.bufete.backend.model.TipoProcedimiento;
import com.bufete.backend.repository.AsuntoRepository;
import com.bufete.backend.repository.BienJuridicoRepository;
import com.bufete.backend.repository.DelitoRepository;
import com.bufete.backend.repository.EtapaProcesoRepository;
import com.bufete.backend.repository.JurisdiccionRepository;
import com.bufete.backend.repository.TipoProcedimientoRepository;

@Service
public class CatalogoJuridicoService {
    private final JurisdiccionRepository jurisdicciones;
    private final AsuntoRepository asuntos;
    private final EtapaProcesoRepository etapas;
    private final TipoProcedimientoRepository procedimientos;
    private final DelitoRepository delitos;
    private final BienJuridicoRepository bienesJuridicos;

    public CatalogoJuridicoService(JurisdiccionRepository jurisdicciones, AsuntoRepository asuntos,
            EtapaProcesoRepository etapas, TipoProcedimientoRepository procedimientos,
            DelitoRepository delitos, BienJuridicoRepository bienesJuridicos) {
        this.jurisdicciones = jurisdicciones;
        this.asuntos = asuntos;
        this.etapas = etapas;
        this.procedimientos = procedimientos;
        this.delitos = delitos;
        this.bienesJuridicos = bienesJuridicos;
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> jurisdicciones() {
        return jurisdicciones.findByActivoTrueOrderByNombreAsc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> asuntos(Integer jurisdiccionId) {
        return asuntos.findByJurisdiccionIdAndActivoTrueOrderByNombreAsc(jurisdiccionId).stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> etapas() {
        return etapas.findByActivoTrueOrderByOrdenAsc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> procedimientos(Integer asuntoId) {
        return procedimientos.findByAsuntoIdAndActivoTrueOrderByNombreAsc(asuntoId).stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> delitos() {
        return delitos.findByActivoTrueOrderByNombreAsc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoDTO> bienesJuridicos() {
        return bienesJuridicos.findByActivoTrueOrderByNombreAsc().stream().map(this::map).toList();
    }

    private CatalogoDTO map(Jurisdiccion item) { return new CatalogoDTO(item.getId(), item.getCodigo(), item.getNombre(), item.getDescripcion()); }
    private CatalogoDTO map(Asunto item) { return new CatalogoDTO(item.getId(), item.getCodigo(), item.getNombre(), item.getDescripcion()); }
    private CatalogoDTO map(EtapaProceso item) { return new CatalogoDTO(item.getId(), item.getCodigo(), item.getNombre(), item.getDescripcion()); }
    private CatalogoDTO map(TipoProcedimiento item) { return new CatalogoDTO(item.getId(), item.getCodigo(), item.getNombre(), item.getDescripcion()); }
    private CatalogoDTO map(Delito item) { return new CatalogoDTO(item.getId(), item.getCodigo(), item.getNombre(), item.getDescripcion()); }
    private CatalogoDTO map(BienJuridico item) { return new CatalogoDTO(item.getId(), item.getCodigo(), item.getNombre(), item.getDescripcion()); }
}