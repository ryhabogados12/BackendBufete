package com.bufete.backend.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.TipoActuacion;
public interface TipoActuacionRepository extends JpaRepository<TipoActuacion, Integer> { Optional<TipoActuacion> findByCodigo(String codigo); List<TipoActuacion> findByAsuntoIdAndActivoTrueOrderByOrdenAsc(Integer asuntoId); }