package com.rentaherramientas.domain.model;

import java.util.List;

/**
 * Ciclo de vida de una reserva:
 * PENDIENTE -> APROBADA -> PAGADA -> ENTREGADA -> DEVUELTA
 * con salidas: RECHAZADA, CANCELADA, VENCIDA.
 */
public enum EstadoReserva {
    PENDIENTE,
    APROBADA,
    PAGADA,
    ENTREGADA,
    DEVUELTA,
    RECHAZADA,
    CANCELADA,
    VENCIDA;

    /** Estados que mantienen ocupada la herramienta en el rango de fechas. */
    public static List<EstadoReserva> bloqueantes() {
        return List.of(PENDIENTE, APROBADA, PAGADA, ENTREGADA);
    }

    /** Estados en los que ya existe un pago aprobado. */
    public static List<EstadoReserva> conPagoAprobado() {
        return List.of(PAGADA, ENTREGADA, DEVUELTA);
    }
}
