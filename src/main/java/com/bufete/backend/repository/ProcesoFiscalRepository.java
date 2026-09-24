package com.bufete.backend.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.ProcesoFiscal;
public interface ProcesoFiscalRepository extends JpaRepository<ProcesoFiscal, Long> { Optional<ProcesoFiscal> findByProcesoId(Long procesoId); }