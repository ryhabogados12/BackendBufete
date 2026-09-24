package com.bufete.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.TerminoProceso;

public interface TerminoProcesoRepository extends JpaRepository<TerminoProceso, Long> {
    List<TerminoProceso> findByActuacionProcesoIdOrderByFechaVencimientoAsc(Long procesoId);
}