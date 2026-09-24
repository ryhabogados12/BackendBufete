package com.bufete.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bufete.backend.model.ProcesoEtapa;

public interface ProcesoEtapaRepository extends JpaRepository<ProcesoEtapa, Long> {
    List<ProcesoEtapa> findByProcesoIdOrderByFechaInicioAsc(Long procesoId);
}