package com.bufete.backend.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.ProcesoActuacion;
public interface ProcesoActuacionRepository extends JpaRepository<ProcesoActuacion, Long> { List<ProcesoActuacion> findByProcesoIdOrderByFechaActuacionAsc(Long procesoId); }