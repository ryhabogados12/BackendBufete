package com.bufete.backend.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "persona")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tipo_documento", length = 30) private String tipoDocumento;
    @Column(name = "numero_documento", length = 100) private String numeroDocumento;
    @Column(name = "nombres", length = 200) private String nombres;
    @Column(name = "apellidos", length = 200) private String apellidos;
    @Column(name = "lugar_residencia") private String lugarResidencia;
    @Column(name = "email", length = 200) private String email;
    @Column(name = "telefono", length = 30) private String telefono;
    @Column(name = "fecha_nacimiento") private LocalDate fechaNacimiento;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}