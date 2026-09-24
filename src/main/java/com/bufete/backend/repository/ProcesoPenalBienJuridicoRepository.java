package com.bufete.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.bufete.backend.model.ProcesoPenalBienJuridico;
import com.bufete.backend.model.ProcesoPenalBienJuridicoId;
public interface ProcesoPenalBienJuridicoRepository extends JpaRepository<ProcesoPenalBienJuridico, ProcesoPenalBienJuridicoId> {
	List<ProcesoPenalBienJuridico> findByProcesoId(Long procesoId);
}