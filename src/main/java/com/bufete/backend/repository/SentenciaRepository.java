package com.bufete.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bufete.backend.model.Sentencia;
import java.util.Optional;

@Repository
public interface SentenciaRepository extends JpaRepository<Sentencia, Long> {

    @Query("SELECT s FROM Sentencia s WHERE s.tipoSentencia = :tipoSentencia")
    List<Sentencia> findByTipoSentencia(@Param("tipoSentencia") String tipoSentencia);

    @Query("SELECT s FROM Sentencia s WHERE s.id = :id AND s.isDeleted = false")
    Optional<Sentencia> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT s FROM Sentencia s WHERE s.isDeleted = false")
    List<Sentencia> findAllNotDeleted();

    @Query("SELECT s FROM Sentencia s WHERE s.cliente.id = :clienteId")
    List<Sentencia> findByClienteId(@Param("clienteId") Long clienteId);
}