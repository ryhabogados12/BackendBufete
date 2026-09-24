package com.bufete.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.Delito;

public interface DelitoRepository extends JpaRepository<Delito, Integer> {
    List<Delito> findByActivoTrueOrderByNombreAsc();
}