package com.rentaherramientas.domain.model;

import com.rentaherramientas.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Alquiler de una herramienta por un cliente en un rango de fechas.
 * Las transiciones de estado viven aquí para que las reglas no se puedan saltar.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private Long herramientaId;
    private String herramientaNombre;
    private Long proveedorId;
    private String proveedorNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int dias;
    private BigDecimal precioDia;
    private BigDecimal total; // subtotal sin IVA
    private EstadoReserva estado;
    private String observaciones;
    private String motivoRechazo;
    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaDevolucion;
    private LocalDateTime fechaCreacion;

    public void aprobar() {
        exigir(EstadoReserva.PENDIENTE, "aprobar");
        estado = EstadoReserva.APROBADA;
    }

    public void rechazar(String motivo) {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.APROBADA) {
            throw new ReglaNegocioException("Solo se pueden rechazar reservas pendientes o aprobadas (estado actual: " + estado + ")");
        }
        estado = EstadoReserva.RECHAZADA;
        motivoRechazo = motivo;
    }

    public void cancelar() {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.APROBADA) {
            throw new ReglaNegocioException("Solo se pueden cancelar reservas pendientes o aprobadas. "
                    + "Si ya fue pagada, contacta al proveedor (estado actual: " + estado + ")");
        }
        estado = EstadoReserva.CANCELADA;
    }

    public void marcarPagada() {
        exigir(EstadoReserva.APROBADA, "pagar");
        estado = EstadoReserva.PAGADA;
    }

    public void registrarEntrega() {
        exigir(EstadoReserva.PAGADA, "entregar");
        estado = EstadoReserva.ENTREGADA;
        fechaEntrega = LocalDateTime.now();
    }

    public void registrarDevolucion() {
        exigir(EstadoReserva.ENTREGADA, "registrar la devolución de");
        estado = EstadoReserva.DEVUELTA;
        fechaDevolucion = LocalDateTime.now();
    }

    public void vencer() {
        estado = EstadoReserva.VENCIDA;
    }

    private void exigir(EstadoReserva esperado, String accion) {
        if (estado != esperado) {
            throw new ReglaNegocioException("No se puede " + accion + " una reserva en estado " + estado
                    + " (se requiere " + esperado + ")");
        }
    }
}
