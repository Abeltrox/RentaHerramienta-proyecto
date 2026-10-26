package com.rentaherramientas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Incidencia de daño detectada en una herramienta durante o después de un alquiler. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDanio {
    private Long id;
    private Long reservaId;
    private Long herramientaId;
    private String herramientaNombre;
    private Long proveedorId;
    private String descripcion;
    private BigDecimal costoEstimado;
    private EstadoDanio estado;
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaResolucion;
}
