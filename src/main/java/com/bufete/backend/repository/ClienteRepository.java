package com.bufete.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bufete.backend.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {
    
    Optional<Cliente> findById(Long id);

    Optional<Cliente> findByIdentificacion(String identificacion);
    
    boolean existsByIdentificacion(String identificacion);
    
    @Query("SELECT c FROM Cliente c WHERE c.activo = true")
    Page<Cliente> findAllActive(Pageable pageable);
}