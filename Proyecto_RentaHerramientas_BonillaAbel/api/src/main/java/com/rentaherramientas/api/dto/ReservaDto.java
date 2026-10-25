package com.rentaherramientas.api.dto;

import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.Reserva;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ReservaDto {

    private ReservaDto() {
    }

    public record CrearRequest(
            @Schema(example = "1") @NotNull Long herramientaId,
            @Schema(example = "2026-11-10") @NotNull LocalDate fechaInicio,
            @Schema(example = "2026-11-13") @NotNull LocalDate fechaFin,
            @Schema(example = "Entrega en obra, barrio Cabecera") @Size(max = 500) String observaciones) {
    }

    public record RechazoRequest(
            @Schema(example = "El equipo estará en mantenimiento esas fechas") @NotBlank @Size(max = 300) String motivo) {
    }

    public record DevolucionRequest(
            @Schema(example = "false") boolean hayDanio,
            @Schema(example = "Carcasa rota") @Size(max = 500) String descripcionDanio,
            @Schema(example = "120000") @PositiveOrZero BigDecimal costoEstimado) {
    }

    public record DisponibilidadResponse(Long herramientaId, LocalDate desde, LocalDate hasta, boolean disponible) {
    }

    public record Response(Long id, Long clienteId, String cliente, Long herramientaId, String herramienta,
                           Long proveedorId, String proveedor, LocalDate fechaInicio, LocalDate fechaFin, int dias,
                           BigDecimal precioDia, BigDecimal subtotal, BigDecimal iva, BigDecimal totalConIva,
                           EstadoReserva estado, String observaciones, String motivoRechazo,
                           LocalDateTime fechaEntrega, LocalDateTime fechaDevolucion, LocalDateTime fechaCreacion) {

        /** El IVA (19 %) se calcula igual que en la factura para que el cliente vea el total real antes de pagar. */
        public static Response de(Reserva r) {
            BigDecimal iva = r.getTotal().multiply(new BigDecimal("0.19")).setScale(2, java.math.RoundingMode.HALF_UP);
            return new Response(r.getId(), r.getClienteId(), r.getClienteNombre(), r.getHerramientaId(), r.getHerramientaNombre(),
                    r.getProveedorId(), r.getProveedorNombre(), r.getFechaInicio(), r.getFechaFin(), r.getDias(),
                    r.getPrecioDia(), r.getTotal(), iva, r.getTotal().add(iva), r.getEstado(), r.getObservaciones(),
                    r.getMotivoRechazo(), r.getFechaEntrega(), r.getFechaDevolucion(), r.getFechaCreacion());
        }
    }
}
