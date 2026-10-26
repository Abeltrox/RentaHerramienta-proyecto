package com.rentaherramientas.api.dto;

import com.rentaherramientas.application.port.in.PagoUseCase.PagoResultado;
import com.rentaherramientas.domain.model.EstadoPago;
import com.rentaherramientas.domain.model.MetodoPago;
import com.rentaherramientas.domain.model.Pago;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PagoDto {

    private PagoDto() {
    }

    public record PagarRequest(
            @Schema(example = "1") @NotNull Long reservaId,
            @Schema(example = "TARJETA_CREDITO") @NotNull MetodoPago metodo,
            @Schema(description = "Últimos 4 dígitos de la tarjeta. En el sandbox, 0000 simula un rechazo", example = "4242")
            @Pattern(regexp = "\\d{4}", message = "Deben ser exactamente 4 dígitos") String ultimosDigitos) {
    }

    public record Response(Long id, Long reservaId, BigDecimal monto, MetodoPago metodo, EstadoPago estado,
                           String referencia, LocalDateTime fechaPago) {
        public static Response de(Pago p) {
            return new Response(p.getId(), p.getReservaId(), p.getMonto(), p.getMetodo(), p.getEstado(), p.getReferencia(), p.getFechaPago());
        }
    }

    public record ResultadoResponse(Response pago, FacturaDto.Response factura) {
        public static ResultadoResponse de(PagoResultado r) {
            return new ResultadoResponse(Response.de(r.pago()), FacturaDto.Response.de(r.factura()));
        }
    }
}
