package com.bufete.backend.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.ProcesoJuzgado;
public interface ProcesoJuzgadoRepository extends JpaRepository<ProcesoJuzgado, Long> { List<ProcesoJuzgado> findByProcesoId(Long procesoId); }