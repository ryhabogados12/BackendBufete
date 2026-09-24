package com.bufete.backend.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bufete.backend.model.ActuacionDocumento;
public interface ActuacionDocumentoRepository extends JpaRepository<ActuacionDocumento, Long> { List<ActuacionDocumento> findByActuacionId(Long actuacionId); }