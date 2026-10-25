package com.rentaherramientas.infrastructure.persistence.entity;

import com.rentaherramientas.domain.model.EstadoHerramienta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "herramientas")
@Getter
@Setter
@NoArgsConstructor
public class HerramientaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private ProveedorEntity proveedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 80)
    private String marca;

    @Column(name = "precio_dia", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioDia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoHerramienta estado = EstadoHerramienta.DISPONIBLE;

    @Column(name = "imagen_url", length = 300)
    private String imagenUrl;

    @Column(name = "reserva_automatica", nullable = false)
    private boolean reservaAutomatica;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void antesDeGuardar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
