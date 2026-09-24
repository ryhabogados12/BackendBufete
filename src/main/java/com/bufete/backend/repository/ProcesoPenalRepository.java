package com.bufete.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bufete.backend.model.ProcesoPenal;

public interface ProcesoPenalRepository extends JpaRepository<ProcesoPenal, Long> {
    Optional<ProcesoPenal> findByNunc(String nunc);
}