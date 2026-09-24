package com.bufete.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bufete.backend.model.EtapaProceso;

public interface EtapaProcesoRepository extends JpaRepository<EtapaProceso, Integer> {
    List<EtapaProceso> findByActivoTrueOrderByOrdenAsc();
}