package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.PagoUseCase.PagarComando;
import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.EstadoPago;
import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.MetodoPago;
import com.rentaherramientas.domain.model.Reserva;
import com.rentaherramientas.domain.model.Rol;
import com.rentaherramientas.domain.port.out.FacturaRepositoryPort;
import com.rentaherramientas.domain.port.out.PagoRepositoryPort;
import com.rentaherramientas.domain.port.out.PaymentGatewayPort.ResultadoPasarela;
import com.rentaherramientas.domain.port.out.PaymentGatewayPort;
import com.rentaherramientas.domain.port.out.ReservaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Está en el mismo paquete que EmisorFacturas (visibilidad de paquete) para probarlo con su implementación real. */
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock PagoRepositoryPort pagos;
    @Mock ReservaRepositoryPort reservas;
    @Mock PaymentGatewayPort pasarela;
    @Mock FacturaRepositoryPort facturas;

    PagoService servicio;
    final Actor cliente = new Actor(24L, Rol.CLIENTE);

    @BeforeEach
    void preparar() {
        servicio = new PagoService(pagos, reservas, pasarela, new EmisorFacturas(facturas));
    }

    private Reserva reservaAprobada() {
        return Reserva.builder().id(5L).clienteId(24L).herramientaNombre("Planta eléctrica")
                .total(new BigDecimal("100000.00")).estado(EstadoReserva.APROBADA).build();
    }

    @Test
    @DisplayName("Pago aprobado: cobra subtotal + IVA 19 %, marca la reserva PAGADA y genera la factura")
    void pagoAprobadoGeneraFactura() {
        Reserva reserva = reservaAprobada();
        when(reservas.buscarPorId(5L)).thenReturn(Optional.of(reserva));
        when(pagos.buscarPorReservaId(5L)).thenReturn(Optional.empty());
        when(pasarela.procesar(any())).thenReturn(new ResultadoPasarela(true, "PAY-123", "Aprobado"));
        when(pagos.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(reservas.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(facturas.siguienteConsecutivo()).thenReturn(7L);
        when(facturas.guardar(any())).thenAnswer(i -> i.getArgument(0));

        var resultado = servicio.pagar(new PagarComando(5L, MetodoPago.PSE, "4242"), cliente);

        assertThat(resultado.pago().getMonto()).isEqualByComparingTo("119000");
        assertThat(resultado.pago().getEstado()).isEqualTo(EstadoPago.APROBADO);
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.PAGADA);
        assertThat(resultado.factura().getSubtotal()).isEqualByComparingTo("100000");
        assertThat(resultado.factura().getIva()).isEqualByComparingTo("19000");
        assertThat(resultado.factura().getTotal()).isEqualByComparingTo("119000");
        assertThat(resultado.factura().getNumero()).startsWith("FAC-").endsWith("-000007");
    }

    @Test
    @DisplayName("Pago rechazado por la pasarela: no se guarda nada y la reserva sigue APROBADA")
    void pagoRechazado() {
        Reserva reserva = reservaAprobada();
        when(reservas.buscarPorId(5L)).thenReturn(Optional.of(reserva));
        when(pagos.buscarPorReservaId(5L)).thenReturn(Optional.empty());
        when(pasarela.procesar(any())).thenReturn(new ResultadoPasarela(false, null, "Fondos insuficientes"));

        assertThatThrownBy(() -> servicio.pagar(new PagarComando(5L, MetodoPago.TARJETA_CREDITO, "0000"), cliente))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("Fondos insuficientes");
        verify(pagos, never()).guardar(any());
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.APROBADA);
    }

    @Test
    @DisplayName("Un cliente no puede pagar la reserva de otro")
    void reservaAjena() {
        when(reservas.buscarPorId(5L)).thenReturn(Optional.of(reservaAprobada()));

        assertThatThrownBy(() -> servicio.pagar(new PagarComando(5L, MetodoPago.NEQUI, null), new Actor(99L, Rol.CLIENTE)))
                .isInstanceOf(OperacionNoPermitidaException.class);
        verify(pasarela, never()).procesar(any());
    }

    @Test
    @DisplayName("No se paga una reserva que aún está pendiente de aprobación")
    void reservaNoAprobada() {
        Reserva r = reservaAprobada();
        r.setEstado(EstadoReserva.PENDIENTE);
        when(reservas.buscarPorId(5L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> servicio.pagar(new PagarComando(5L, MetodoPago.PSE, null), cliente))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("El IVA se calcula al 19 % redondeado a 2 decimales")
    void calculoIva() {
        assertThat(EmisorFacturas.calcularIva(new BigDecimal("135000"))).isEqualByComparingTo("25650.00");
        assertThat(EmisorFacturas.calcularIva(new BigDecimal("33333.33"))).isEqualByComparingTo("6333.33");
    }
}
