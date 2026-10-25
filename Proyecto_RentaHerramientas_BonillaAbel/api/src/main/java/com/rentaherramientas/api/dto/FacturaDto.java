package com.rentaherramientas.api.dto;

import com.rentaherramientas.domain.model.Factura;
import com.rentaherramientas.domain.model.MetodoPago;
import com.rentaherramientas.domain.model.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class FacturaDto {

    private FacturaDto() {
    }

    public record Response(Long id, String numero, Long reservaId, String cliente, String proveedor, String herramienta,
                           LocalDate fechaInicio, LocalDate fechaFin, int dias, BigDecimal precioDia,
                           BigDecimal subtotal, BigDecimal iva, BigDecimal total,
                           MetodoPago metodoPago, String referenciaPago, LocalDateTime fechaEmision) {
        public static Response de(Factura f) {
            Reserva r = f.getReserva();
            return new Response(f.getId(), f.getNumero(), r.getId(), r.getClienteNombre(), r.getProveedorNombre(),
                    r.getHerramientaNombre(), r.getFechaInicio(), r.getFechaFin(), r.getDias(), r.getPrecioDia(),
                    f.getSubtotal(), f.getIva(), f.getTotal(), f.getPago().getMetodo(), f.getPago().getReferencia(),
                    f.getFechaEmision());
        }
    }
}
