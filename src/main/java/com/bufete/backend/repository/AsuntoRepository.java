package com.bufete.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bufete.backend.model.Asunto;

public interface AsuntoRepository extends JpaRepository<Asunto, Integer> {
    List<Asunto> findByJurisdiccionIdAndActivoTrueOrderByNombreAsc(Integer jurisdiccionId);
}