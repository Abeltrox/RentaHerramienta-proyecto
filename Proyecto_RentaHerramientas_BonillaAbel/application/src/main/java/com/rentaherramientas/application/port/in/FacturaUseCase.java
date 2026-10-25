package com.rentaherramientas.application.port.in;

import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Factura;

import java.util.List;

public interface FacturaUseCase {

    List<Factura> listar(Actor actor);

    Factura obtener(Long id, Actor actor);

    Factura obtenerPorReserva(Long reservaId, Actor actor);

    /** Emite la factura de una reserva pagada (si ya existe, devuelve la existente). */
    Factura generar(Long reservaId, Actor actor);

    byte[] descargarPdf(Long id, Actor actor);
}
