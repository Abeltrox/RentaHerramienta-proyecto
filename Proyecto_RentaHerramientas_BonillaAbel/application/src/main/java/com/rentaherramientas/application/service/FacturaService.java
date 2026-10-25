package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.FacturaUseCase;
import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.exception.RecursoNoEncontradoException;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.EstadoPago;
import com.rentaherramientas.domain.model.Factura;
import com.rentaherramientas.domain.model.Pago;
import com.rentaherramientas.domain.model.Reserva;
import com.rentaherramientas.domain.port.out.FacturaRepositoryPort;
import com.rentaherramientas.domain.port.out.InvoicePdfPort;
import com.rentaherramientas.domain.port.out.PagoRepositoryPort;
import com.rentaherramientas.domain.port.out.ReservaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FacturaService implements FacturaUseCase {

    private final FacturaRepositoryPort facturas;
    private final ReservaRepositoryPort reservas;
    private final PagoRepositoryPort pagos;
    private final EmisorFacturas emisorFacturas;
    private final InvoicePdfPort generadorPdf;

    @Override
    @Transactional(readOnly = true)
    public List<Factura> listar(Actor actor) {
        if (actor.esAdmin()) {
            return facturas.listarTodas();
        }
        if (actor.esProveedor()) {
            return facturas.listarPorProveedor(actor.usuarioId());
        }
        return facturas.listarPorCliente(actor.usuarioId());
    }

    @Override
    @Transactional(readOnly = true)
    public Factura obtener(Long id, Actor actor) {
        Factura factura = facturas.buscarPorId(id).orElseThrow(() -> RecursoNoEncontradoException.de("Factura", id));
        exigirAcceso(factura, actor);
        return factura;
    }

    @Override
    @Transactional(readOnly = true)
    public Factura obtenerPorReserva(Long reservaId, Actor actor) {
        Factura factura = facturas.buscarPorReservaId(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva " + reservaId + " aún no tiene factura"));
        exigirAcceso(factura, actor);
        return factura;
    }

    @Override
    public Factura generar(Long reservaId, Actor actor) {
        Reserva reserva = reservas.buscarPorId(reservaId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Reserva", reservaId));
        boolean esProveedorDueno = actor.esProveedor() && actor.usuarioId().equals(reserva.getProveedorId());
        if (!actor.esAdmin() && !esProveedorDueno) {
            throw new OperacionNoPermitidaException("Solo el proveedor de la herramienta o un administrador pueden emitir la factura");
        }
        // Idempotente: si ya existe se devuelve la misma factura.
        return facturas.buscarPorReservaId(reservaId).orElseGet(() -> {
            Pago pago = pagos.buscarPorReservaId(reservaId)
                    .filter(p -> p.getEstado() == EstadoPago.APROBADO)
                    .orElseThrow(() -> new ReglaNegocioException("La reserva no tiene un pago aprobado, no se puede facturar"));
            return emisorFacturas.emitir(reserva, pago);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargarPdf(Long id, Actor actor) {
        return generadorPdf.generar(obtener(id, actor));
    }

    private void exigirAcceso(Factura factura, Actor actor) {
        Reserva reserva = factura.getReserva();
        if (!actor.puedeGestionar(reserva.getClienteId()) && !actor.puedeGestionar(reserva.getProveedorId())) {
            throw new OperacionNoPermitidaException("No tienes acceso a esta factura");
        }
    }
}
