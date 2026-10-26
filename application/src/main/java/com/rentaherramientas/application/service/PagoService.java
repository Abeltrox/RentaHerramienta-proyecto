package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.PagoUseCase;
import com.rentaherramientas.domain.exception.ConflictoException;
import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.exception.RecursoNoEncontradoException;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.EstadoPago;
import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.Factura;
import com.rentaherramientas.domain.model.Pago;
import com.rentaherramientas.domain.model.Reserva;
import com.rentaherramientas.domain.port.out.PagoRepositoryPort;
import com.rentaherramientas.domain.port.out.PaymentGatewayPort.ResultadoPasarela;
import com.rentaherramientas.domain.port.out.PaymentGatewayPort.SolicitudPago;
import com.rentaherramientas.domain.port.out.PaymentGatewayPort;
import com.rentaherramientas.domain.port.out.ReservaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PagoService implements PagoUseCase {

    private final PagoRepositoryPort pagos;
    private final ReservaRepositoryPort reservas;
    private final PaymentGatewayPort pasarela;
    private final EmisorFacturas emisorFacturas;

    @Override
    public PagoResultado pagar(PagarComando c, Actor actor) {
        if (!actor.esCliente()) {
            throw new OperacionNoPermitidaException("Solo los clientes pueden pagar reservas");
        }
        Reserva reserva = reservas.buscarPorId(c.reservaId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Reserva", c.reservaId()));
        if (!actor.usuarioId().equals(reserva.getClienteId())) {
            throw new OperacionNoPermitidaException("Esta reserva no te pertenece");
        }
        if (reserva.getEstado() != EstadoReserva.APROBADA) {
            throw new ReglaNegocioException("La reserva debe estar aprobada para poder pagarla (estado actual: "
                    + reserva.getEstado() + ")");
        }
        if (pagos.buscarPorReservaId(reserva.getId()).isPresent()) {
            throw new ConflictoException("La reserva ya tiene un pago registrado");
        }

        BigDecimal subtotal = reserva.getTotal();
        BigDecimal totalConIva = subtotal.add(EmisorFacturas.calcularIva(subtotal));

        ResultadoPasarela resultado = pasarela.procesar(new SolicitudPago(
                totalConIva, c.metodo(), c.ultimosDigitos(), "Alquiler de " + reserva.getHerramientaNombre()));
        if (!resultado.aprobado()) {
            throw new ReglaNegocioException("Pago rechazado por la pasarela: " + resultado.mensaje());
        }

        Pago pago = pagos.guardar(Pago.builder()
                .reservaId(reserva.getId())
                .monto(totalConIva)
                .metodo(c.metodo())
                .estado(EstadoPago.APROBADO)
                .referencia(resultado.referencia())
                .fechaPago(LocalDateTime.now())
                .build());

        reserva.marcarPagada();
        Reserva reservaPagada = reservas.guardar(reserva);

        Factura factura = emisorFacturas.emitir(reservaPagada, pago);
        return new PagoResultado(pago, factura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> listar(Actor actor) {
        if (actor.esAdmin()) {
            return pagos.listarTodos();
        }
        if (actor.esProveedor()) {
            return pagos.listarPorProveedor(actor.usuarioId());
        }
        return pagos.listarPorCliente(actor.usuarioId());
    }

    @Override
    @Transactional(readOnly = true)
    public Pago obtener(Long id, Actor actor) {
        Pago pago = pagos.buscarPorId(id).orElseThrow(() -> RecursoNoEncontradoException.de("Pago", id));
        Reserva reserva = reservas.buscarPorId(pago.getReservaId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Reserva", pago.getReservaId()));
        if (!actor.puedeGestionar(reserva.getClienteId()) && !actor.puedeGestionar(reserva.getProveedorId())) {
            throw new OperacionNoPermitidaException("No tienes acceso a este pago");
        }
        return pago;
    }
}
