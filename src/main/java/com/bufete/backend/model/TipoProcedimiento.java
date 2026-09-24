package com.bufete.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_procedimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoProcedimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asunto_id", nullable = false)
    private Asunto asunto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etapa_id", nullable = false)
    private EtapaProceso etapa;

    @Column(name = "codigo", nullable = false, unique = true, length = 120)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 300)
    private String nombre;

    @Column(name = "norma", length = 150)
    private String norma;

    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}