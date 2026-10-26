package com.rentaherramientas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pago en línea de una reserva (monto con IVA incluido). */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {
    private Long id;
    private Long reservaId;
    private BigDecimal monto;
    private MetodoPago metodo;
    private EstadoPago estado;
    private String referencia;
    private LocalDateTime fechaPago;
}
