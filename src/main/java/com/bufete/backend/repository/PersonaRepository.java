package com.bufete.backend.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.Persona;
public interface PersonaRepository extends JpaRepository<Persona, Long> { 

    Persona findByNumeroDocumento(String identificacion);
}