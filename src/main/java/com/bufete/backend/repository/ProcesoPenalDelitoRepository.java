package com.bufete.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.bufete.backend.model.ProcesoPenalDelito;
import com.bufete.backend.model.ProcesoPenalDelitoId;
public interface ProcesoPenalDelitoRepository extends JpaRepository<ProcesoPenalDelito, ProcesoPenalDelitoId> {
	List<ProcesoPenalDelito> findByProcesoId(Long procesoId);
}