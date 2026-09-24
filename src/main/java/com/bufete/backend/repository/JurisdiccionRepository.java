package com.bufete.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bufete.backend.model.Jurisdiccion;

public interface JurisdiccionRepository extends JpaRepository<Jurisdiccion, Integer> {
    List<Jurisdiccion> findByActivoTrueOrderByNombreAsc();
}