package com.rentaherramientas.domain;

import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.Reserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaTest {

    private Reserva reservaEn(EstadoReserva estado) {
        return Reserva.builder().id(1L).estado(estado).build();
    }

    @Test
    @DisplayName("El ciclo completo PENDIENTE → APROBADA → PAGADA → ENTREGADA → DEVUELTA es válido")
    void cicloCompleto() {
        Reserva r = reservaEn(EstadoReserva.PENDIENTE);
        r.aprobar();
        r.marcarPagada();
        r.registrarEntrega();
        r.registrarDevolucion();

        assertThat(r.getEstado()).isEqualTo(EstadoReserva.DEVUELTA);
        assertThat(r.getFechaEntrega()).isNotNull();
        assertThat(r.getFechaDevolucion()).isNotNull();
    }

    @Test
    @DisplayName("No se puede pagar una reserva que no fue aprobada")
    void noSePuedePagarSinAprobar() {
        Reserva r = reservaEn(EstadoReserva.PENDIENTE);
        assertThatThrownBy(r::marcarPagada).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("No se puede entregar una herramienta cuya reserva no está pagada")
    void noSeEntregaSinPago() {
        Reserva r = reservaEn(EstadoReserva.APROBADA);
        assertThatThrownBy(r::registrarEntrega).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("Solo se cancelan reservas pendientes o aprobadas")
    void cancelarSoloAntesDelPago() {
        assertThatThrownBy(() -> reservaEn(EstadoReserva.PAGADA).cancelar()).isInstanceOf(ReglaNegocioException.class);
        Reserva r = reservaEn(EstadoReserva.APROBADA);
        r.cancelar();
        assertThat(r.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    @DisplayName("Al rechazar se guarda el motivo")
    void rechazarGuardaMotivo() {
        Reserva r = reservaEn(EstadoReserva.PENDIENTE);
        r.rechazar("Equipo en mantenimiento");
        assertThat(r.getEstado()).isEqualTo(EstadoReserva.RECHAZADA);
        assertThat(r.getMotivoRechazo()).isEqualTo("Equipo en mantenimiento");
    }

    @Test
    @DisplayName("Una reserva ya devuelta no puede volver a devolverse")
    void noSeDevuelveDosVeces() {
        Reserva r = reservaEn(EstadoReserva.DEVUELTA);
        assertThatThrownBy(() -> r.registrarDevolucion()).isInstanceOf(ReglaNegocioException.class);
    }
}
