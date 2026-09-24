package com.bufete.backend.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.ReglaTermino;
public interface ReglaTerminoRepository extends JpaRepository<ReglaTermino, Integer> { Optional<ReglaTermino> findByTipoProcedimientoIdAndTipoActuacionIdAndActivoTrue(Integer procedimientoId, Integer actuacionId); }