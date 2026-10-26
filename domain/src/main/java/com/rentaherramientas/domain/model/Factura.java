package com.rentaherramientas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Factura generada automáticamente al aprobarse el pago de una reserva. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Factura {
    private Long id;
    private String numero;
    private Reserva reserva;
    private Pago pago;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private LocalDateTime fechaEmision;
}
