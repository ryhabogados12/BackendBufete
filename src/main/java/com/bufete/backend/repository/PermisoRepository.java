package com.bufete.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bufete.backend.Dtos.permiso.PermisoDTO;
import com.bufete.backend.model.Permiso;


public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    Optional<Permiso> findByNombre(String nombre);

    @Query("SELECT new com.bufete.backend.Dtos.permiso.PermisoDTO(c.id, c.nombre, c.descripcion) " +
       "FROM Permiso c ORDER BY c.nombre ASC")
    List<PermisoDTO> listarTodosPermisos();
}
