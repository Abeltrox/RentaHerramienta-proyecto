package com.rentaherramientas.application.port.in;

import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Caso de uso: ciclo completo de reservas (solicitud, aprobación, entrega y devolución). */
public interface ReservaUseCase {

    Reserva crear(CrearReservaComando comando, Actor actor);

    Reserva aprobar(Long id, Actor actor);

    Reserva rechazar(Long id, String motivo, Actor actor);

    Reserva cancelar(Long id, Actor actor);

    Reserva registrarEntrega(Long id, Actor actor);

    Reserva registrarDevolucion(Long id, DevolucionComando comando, Actor actor);

    Reserva obtener(Long id, Actor actor);

    /** Admin: todas. Proveedor: las de sus herramientas. Cliente: las propias. */
    List<Reserva> listar(Actor actor);

    boolean estaDisponible(Long herramientaId, LocalDate inicio, LocalDate fin);

    /** Marca como VENCIDAS las reservas que nadie atendió o pagó a tiempo. Devuelve cuántas. */
    int expirarVencidas();

    record CrearReservaComando(Long herramientaId, LocalDate fechaInicio, LocalDate fechaFin, String observaciones) {
    }

    record DevolucionComando(boolean hayDanio, String descripcionDanio, BigDecimal costoEstimado) {
    }
}
