package com.bufete.backend.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.ProcesoPenalImputacion;
public interface ProcesoPenalImputacionRepository extends JpaRepository<ProcesoPenalImputacion, Long> { Optional<ProcesoPenalImputacion> findByProcesoPenalProcesoId(Long procesoId); }