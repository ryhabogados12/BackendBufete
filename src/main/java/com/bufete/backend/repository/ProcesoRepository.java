package com.bufete.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bufete.backend.model.Proceso;
import com.bufete.backend.model.Usuario;

@Repository
public interface ProcesoRepository extends JpaRepository<Proceso, Long>, JpaSpecificationExecutor<Proceso> {
    
    boolean existsByNumeroProceso(String numeroProceso);
    
    @Query("SELECT p FROM Proceso p WHERE p.activo = true")
    Page<Proceso> findAllActive(Pageable pageable);

    List<Proceso> findAllByActivoTrue();
    
    @Query("SELECT p FROM Proceso p WHERE p.cliente.id = :clienteId AND p.activo = true")
    List<Proceso> findByClienteIdAndActivoTrue(@Param("clienteId") Long clienteId);
    
    @Query("SELECT p FROM Proceso p WHERE p.abogadoResponsable.id = :abogadoId AND p.activo = true")
    List<Proceso> findByAbogadoResponsableIdAndActivoTrue(@Param("abogadoId") Long abogadoId);
    
    @Query("SELECT COUNT(e) FROM Expediente e WHERE e.proceso.id = :procesoId AND e.isDeleted = false")
    int countExpedientesByProcesoId(@Param("procesoId") Long procesoId);
    
    @Query("SELECT COUNT(ev) FROM Evento ev WHERE ev.proceso.id = :procesoId AND ev.isDeleted = false")
    int countEventosByProcesoId(@Param("procesoId") Long procesoId);

    Optional<Proceso> findByNumeroProceso(String numeroProceso);
}
