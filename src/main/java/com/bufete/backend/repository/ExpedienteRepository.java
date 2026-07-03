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

import com.bufete.backend.model.Expediente;
import com.bufete.backend.model.Usuario;

@Repository
public interface ExpedienteRepository extends JpaRepository<Expediente, Long>, JpaSpecificationExecutor<Expediente> {
    
    @Query("SELECT e FROM Expediente e WHERE e.isDeleted = false")
    Page<Expediente> findAllActive(Pageable pageable);
    
    @Query("SELECT e FROM Expediente e WHERE e.proceso.id = :procesoId AND e.isDeleted = false")
    List<Expediente> findByProcesoIdAndIsDeletedFalse(@Param("procesoId") Long procesoId);
    
    @Query("SELECT COUNT(n) FROM Node n WHERE n.expediente.id = :expedienteId AND n.isDeleted = false AND n.type = 'FILE'")
    int countDocumentosByExpedienteId(@Param("expedienteId") Long expedienteId);
    
    @Query("SELECT COALESCE(SUM(n.sizeBytes), 0) FROM Node n WHERE n.expediente.id = :expedienteId AND n.isDeleted = false")
    long getTotalSizeByExpedienteId(@Param("expedienteId") Long expedienteId);

    Optional<Expediente> findByProcesoIdAndNombre(Long id, String spoa);

    @Query("SELECT e FROM Expediente e WHERE e.proceso.abogadoResponsable.id = :abogadoId AND e.isDeleted = false")
    List<Expediente> findByProcesoAbogadoResponsableIdAndIsDeletedFalse(@Param("abogadoId") Long abogadoId);
}