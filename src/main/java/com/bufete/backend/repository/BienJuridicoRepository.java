package com.bufete.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.BienJuridico;

public interface BienJuridicoRepository extends JpaRepository<BienJuridico, Integer> {
    List<BienJuridico> findByActivoTrueOrderByNombreAsc();
}