package com.rentaherramientas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Equipo o herramienta de construcción publicada por un proveedor. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Herramienta {
    private Long id;
    private Long proveedorId;
    private String proveedorNombre;
    private Long categoriaId;
    private String categoriaNombre;
    private String nombre;
    private String descripcion;
    private String marca;
    private BigDecimal precioDia;
    private EstadoHerramienta estado;
    private String imagenUrl;
    /** Si es true, las reservas se aprueban solas (sin que el proveedor deba aceptarlas). */
    private boolean reservaAutomatica;
    private LocalDateTime fechaCreacion;

    public boolean estaDisponibleParaAlquiler() {
        return estado == EstadoHerramienta.DISPONIBLE;
    }
}
