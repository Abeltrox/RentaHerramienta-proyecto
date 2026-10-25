package com.rentaherramientas.application.port.in;

import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Factura;
import com.rentaherramientas.domain.model.MetodoPago;
import com.rentaherramientas.domain.model.Pago;

import java.util.List;

/** Caso de uso: pagos en línea. Al aprobarse el pago se emite la factura automáticamente. */
public interface PagoUseCase {

    PagoResultado pagar(PagarComando comando, Actor actor);

    List<Pago> listar(Actor actor);

    Pago obtener(Long id, Actor actor);

    record PagarComando(Long reservaId, MetodoPago metodo, String ultimosDigitos) {
    }

    record PagoResultado(Pago pago, Factura factura) {
    }
}
