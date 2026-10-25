package com.rentaherramientas.application.service;

import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.exception.RecursoNoEncontradoException;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.EstadoPago;
import com.rentaherramientas.domain.model.Factura;
import com.rentaherramientas.domain.model.Pago;
import com.rentaherramientas.domain.model.Reserva;
import com.rentaherramientas.domain.model.Rol;
import com.rentaherramientas.domain.port.out.FacturaRepositoryPort;
import com.rentaherramientas.domain.port.out.InvoicePdfPort;
import com.rentaherramientas.domain.port.out.PagoRepositoryPort;
import com.rentaherramientas.domain.port.out.ReservaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Mismo paquete que EmisorFacturas (visibilidad de paquete) para usar su implementación real. */
@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock FacturaRepositoryPort facturas;
    @Mock ReservaRepositoryPort reservas;
    @Mock PagoRepositoryPort pagos;
    @Mock InvoicePdfPort pdf;

    FacturaService servicio;

    final Actor admin = new Actor(1L, Rol.ADMIN);
    final Actor proveedorDueno = new Actor(2L, Rol.PROVEEDOR);
    final Actor otroProveedor = new Actor(3L, Rol.PROVEEDOR);
    final Actor cliente = new Actor(24L, Rol.CLIENTE);

    @BeforeEach
    void preparar() {
        servicio = new FacturaService(facturas, reservas, pagos, new EmisorFacturas(facturas), pdf);
    }

    private Reserva reserva() {
        return Reserva.builder().id(5L).clienteId(24L).proveedorId(2L).total(new BigDecimal("100000.00")).build();
    }

    private Factura factura() {
        return Factura.builder().id(9L).numero("FAC-2026-000001").reserva(reserva()).build();
    }

    @Test
    @DisplayName("El administrador lista todas las facturas; el proveedor y el cliente, solo las suyas")
    void listaSegunRol() {
        when(facturas.listarTodas()).thenReturn(List.of(factura()));
        when(facturas.listarPorProveedor(2L)).thenReturn(List.of(factura()));
        when(facturas.listarPorCliente(24L)).thenReturn(List.of(factura()));

        assertThat(servicio.listar(admin)).hasSize(1);
        assertThat(servicio.listar(proveedorDueno)).hasSize(1);
        assertThat(servicio.listar(cliente)).hasSize(1);
        verify(facturas).listarTodas();
        verify(facturas).listarPorProveedor(2L);
        verify(facturas).listarPorCliente(24L);
    }

    @Test
    @DisplayName("Obtener: el cliente dueño accede; otro proveedor recibe 'no permitido'")
    void accesoPorDuenio() {
        when(facturas.buscarPorId(9L)).thenReturn(Optional.of(factura()));

        assertThat(servicio.obtener(9L, cliente).getNumero()).isEqualTo("FAC-2026-000001");
        assertThatThrownBy(() -> servicio.obtener(9L, otroProveedor)).isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    @DisplayName("Obtener una factura inexistente lanza 'no encontrado'")
    void facturaInexistente() {
        when(facturas.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener(99L, admin)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("Generar: un cliente no puede emitir facturas")
    void clienteNoGenera() {
        when(reservas.buscarPorId(5L)).thenReturn(Optional.of(reserva()));

        assertThatThrownBy(() -> servicio.generar(5L, cliente)).isInstanceOf(OperacionNoPermitidaException.class);
        verify(facturas, never()).guardar(any());
    }

    @Test
    @DisplayName("Generar: sin pago aprobado no se factura")
    void sinPagoAprobado() {
        when(reservas.buscarPorId(5L)).thenReturn(Optional.of(reserva()));
        when(facturas.buscarPorReservaId(5L)).thenReturn(Optional.empty());
        when(pagos.buscarPorReservaId(5L)).thenReturn(Optional.of(Pago.builder().estado(EstadoPago.RECHAZADO).build()));

        assertThatThrownBy(() -> servicio.generar(5L, proveedorDueno)).isInstanceOf(ReglaNegocioException.class);
        verify(facturas, never()).guardar(any());
    }

    @Test
    @DisplayName("Generar: con pago aprobado emite la factura con IVA 19 % y numeración consecutiva")
    void generaFactura() {
        when(reservas.buscarPorId(5L)).thenReturn(Optional.of(reserva()));
        when(facturas.buscarPorReservaId(5L)).thenReturn(Optional.empty());
        when(pagos.buscarPorReservaId(5L)).thenReturn(Optional.of(Pago.builder().estado(EstadoPago.APROBADO).build()));
        when(facturas.siguienteConsecutivo()).thenReturn(7L);
        when(facturas.guardar(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        Factura emitida = servicio.generar(5L, proveedorDueno);

        assertThat(emitida.getNumero()).endsWith("-000007");
        assertThat(emitida.getSubtotal()).isEqualByComparingTo("100000.00");
        assertThat(emitida.getIva()).isEqualByComparingTo("19000.00");
        assertThat(emitida.getTotal()).isEqualByComparingTo("119000.00");
    }

    @Test
    @DisplayName("Generar es idempotente: si ya existe devuelve la misma factura")
    void generarEsIdempotente() {
        Factura existente = factura();
        when(reservas.buscarPorId(5L)).thenReturn(Optional.of(reserva()));
        when(facturas.buscarPorReservaId(5L)).thenReturn(Optional.of(existente));

        assertThat(servicio.generar(5L, admin)).isSameAs(existente);
        verify(facturas, never()).guardar(any());
    }

    @Test
    @DisplayName("Descargar PDF delega en el generador tras validar el acceso")
    void descargaPdf() {
        Factura f = factura();
        byte[] contenido = {1, 2, 3};
        when(facturas.buscarPorId(9L)).thenReturn(Optional.of(f));
        when(pdf.generar(f)).thenReturn(contenido);

        assertThat(servicio.descargarPdf(9L, cliente)).isEqualTo(contenido);
    }
}
