package com.rentaherramientas.application.service;

import com.rentaherramientas.domain.model.Factura;
import com.rentaherramientas.domain.model.Pago;
import com.rentaherramientas.domain.model.Reserva;
import com.rentaherramientas.domain.port.out.FacturaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Calcula IVA y emite facturas con numeración consecutiva. Lo usan PagoService y FacturaService. */
@Component
@RequiredArgsConstructor
class EmisorFacturas {

    /** IVA general en Colombia. */
    static final BigDecimal TASA_IVA = new BigDecimal("0.19");

    private final FacturaRepositoryPort facturas;

    static BigDecimal calcularIva(BigDecimal subtotal) {
        return subtotal.multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);
    }

    Factura emitir(Reserva reserva, Pago pago) {
        BigDecimal subtotal = reserva.getTotal();
        BigDecimal iva = calcularIva(subtotal);
        String numero = "FAC-%d-%06d".formatted(LocalDate.now().getYear(), facturas.siguienteConsecutivo());
        return facturas.guardar(Factura.builder()
                .numero(numero)
                .reserva(reserva)
                .pago(pago)
                .subtotal(subtotal)
                .iva(iva)
                .total(subtotal.add(iva))
                .build());
    }
}
