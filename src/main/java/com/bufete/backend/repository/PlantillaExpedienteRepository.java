package com.bufete.backend.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.PlantillaExpediente;
public interface PlantillaExpedienteRepository extends JpaRepository<PlantillaExpediente, Integer> { Optional<PlantillaExpediente> findFirstByAsuntoIdAndActivoTrueOrderByIdAsc(Integer asuntoId); }