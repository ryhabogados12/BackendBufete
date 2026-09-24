package com.bufete.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bufete.backend.model.TipoProcedimiento;

public interface TipoProcedimientoRepository extends JpaRepository<TipoProcedimiento, Integer> {
    List<TipoProcedimiento> findByAsuntoIdAndActivoTrueOrderByNombreAsc(Integer asuntoId);
}