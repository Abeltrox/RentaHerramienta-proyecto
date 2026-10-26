package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.ReservaUseCase.CrearReservaComando;
import com.rentaherramientas.domain.exception.ConflictoException;
import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Cliente;
import com.rentaherramientas.domain.model.EstadoHerramienta;
import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.Herramienta;
import com.rentaherramientas.domain.model.Reserva;
import com.rentaherramientas.domain.model.Rol;
import com.rentaherramientas.domain.port.out.ClienteRepositoryPort;
import com.rentaherramientas.domain.port.out.DanioRepositoryPort;
import com.rentaherramientas.domain.port.out.HerramientaRepositoryPort;
import com.rentaherramientas.domain.port.out.ReservaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock ReservaRepositoryPort reservas;
    @Mock HerramientaRepositoryPort herramientas;
    @Mock ClienteRepositoryPort clientes;
    @Mock DanioRepositoryPort danios;

    ReservaService servicio;
    final Actor cliente = new Actor(24L, Rol.CLIENTE);
    final LocalDate inicio = LocalDate.now().plusDays(5);

    @BeforeEach
    void preparar() {
        servicio = new ReservaService(reservas, herramientas, clientes, danios);
    }

    private Herramienta herramienta(EstadoHerramienta estado, boolean automatica) {
        return Herramienta.builder().id(1L).proveedorId(2L).nombre("Rotomartillo")
                .precioDia(new BigDecimal("50000")).estado(estado).reservaAutomatica(automatica).build();
    }

    private void herramientaYClienteExisten(Herramienta h) {
        when(clientes.buscarPorId(24L)).thenReturn(Optional.of(Cliente.builder().id(24L).build()));
        when(herramientas.buscarPorIdParaActualizar(1L)).thenReturn(Optional.of(h));
    }

    @Test
    @DisplayName("Crea una reserva PENDIENTE y calcula días y total")
    void creaReservaPendiente() {
        herramientaYClienteExisten(herramienta(EstadoHerramienta.DISPONIBLE, false));
        when(reservas.existeSolapamiento(1L, inicio, inicio.plusDays(2), -1L)).thenReturn(false);
        when(reservas.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Reserva r = servicio.crear(new CrearReservaComando(1L, inicio, inicio.plusDays(2), null), cliente);

        assertThat(r.getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
        assertThat(r.getDias()).isEqualTo(3);
        assertThat(r.getTotal()).isEqualByComparingTo("150000");
        assertThat(r.getClienteId()).isEqualTo(24L);
    }

    @Test
    @DisplayName("Si la herramienta tiene confirmación automática, la reserva nace APROBADA")
    void confirmacionAutomatica() {
        herramientaYClienteExisten(herramienta(EstadoHerramienta.DISPONIBLE, true));
        when(reservas.existeSolapamiento(1L, inicio, inicio, -1L)).thenReturn(false);
        when(reservas.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Reserva r = servicio.crear(new CrearReservaComando(1L, inicio, inicio, null), cliente);

        assertThat(r.getEstado()).isEqualTo(EstadoReserva.APROBADA);
        assertThat(r.getDias()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rechaza la reserva si las fechas se cruzan con otra activa")
    void rechazaSolapamiento() {
        herramientaYClienteExisten(herramienta(EstadoHerramienta.DISPONIBLE, false));
        when(reservas.existeSolapamiento(1L, inicio, inicio.plusDays(1), -1L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(new CrearReservaComando(1L, inicio, inicio.plusDays(1), null), cliente))
                .isInstanceOf(ConflictoException.class);
        verify(reservas, never()).guardar(any());
    }

    @Test
    @DisplayName("No permite reservar herramientas dañadas o en mantenimiento")
    void rechazaHerramientaNoDisponible() {
        herramientaYClienteExisten(herramienta(EstadoHerramienta.DANADA, false));

        assertThatThrownBy(() -> servicio.crear(new CrearReservaComando(1L, inicio, inicio, null), cliente))
                .isInstanceOf(ReglaNegocioException.class);
        verify(reservas, never()).guardar(any());
    }

    @Test
    @DisplayName("Valida fechas: no pasadas, fin >= inicio y máximo 90 días")
    void validaFechas() {
        assertThatThrownBy(() -> servicio.crear(new CrearReservaComando(1L, LocalDate.now().minusDays(1), inicio, null), cliente))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> servicio.crear(new CrearReservaComando(1L, inicio, inicio.minusDays(1), null), cliente))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> servicio.crear(new CrearReservaComando(1L, inicio, inicio.plusDays(120), null), cliente))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("Solo un CLIENTE puede solicitar reservas")
    void soloClientesReservan() {
        assertThatThrownBy(() -> servicio.crear(new CrearReservaComando(1L, inicio, inicio, null), new Actor(2L, Rol.PROVEEDOR)))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    @DisplayName("Un proveedor no puede aprobar reservas de herramientas ajenas")
    void proveedorAjenoNoAprueba() {
        Reserva r = Reserva.builder().id(9L).proveedorId(2L).estado(EstadoReserva.PENDIENTE).build();
        when(reservas.buscarPorId(9L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> servicio.aprobar(9L, new Actor(3L, Rol.PROVEEDOR)))
                .isInstanceOf(OperacionNoPermitidaException.class);
        verify(reservas, never()).guardar(any());
    }

    @Test
    @DisplayName("El proveedor dueño aprueba una reserva pendiente")
    void proveedorDuenoAprueba() {
        Reserva r = Reserva.builder().id(9L).proveedorId(2L).estado(EstadoReserva.PENDIENTE).build();
        when(reservas.buscarPorId(9L)).thenReturn(Optional.of(r));
        when(reservas.guardar(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio.aprobar(9L, new Actor(2L, Rol.PROVEEDOR)).getEstado()).isEqualTo(EstadoReserva.APROBADA);
    }
}
