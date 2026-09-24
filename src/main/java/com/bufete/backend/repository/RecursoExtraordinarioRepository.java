package com.bufete.backend.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.RecursoExtraordinario;
public interface RecursoExtraordinarioRepository extends JpaRepository<RecursoExtraordinario, Long> { Optional<RecursoExtraordinario> findByExpedienteId(Long expedienteId); }