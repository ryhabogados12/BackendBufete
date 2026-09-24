package com.bufete.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.ProcesoPersona;

public interface ProcesoPersonaRepository extends JpaRepository<ProcesoPersona, Long> {
    List<ProcesoPersona> findByProcesoIdOrderByOrdenAsc(Long procesoId);
    List<ProcesoPersona> findByProcesoIdAndRolOrderByOrdenAsc(Long procesoId, String rol);

}