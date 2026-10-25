package com.rentaherramientas.application.service;

import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Reportes.ResumenGeneral;
import com.rentaherramientas.domain.model.Rol;
import com.rentaherramientas.domain.port.out.ReporteRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock ReporteRepositoryPort reportes;

    ReporteService servicio;

    final Actor admin = new Actor(1L, Rol.ADMIN);
    final Actor proveedor = new Actor(2L, Rol.PROVEEDOR);
    final Actor cliente = new Actor(24L, Rol.CLIENTE);

    @BeforeEach
    void preparar() {
        servicio = new ReporteService(reportes);
    }

    @Test
    @DisplayName("El administrador consulta toda la plataforma (alcance nulo)")
    void adminVeTodo() {
        ResumenGeneral resumen = new ResumenGeneral(1, 2, 3, 4, 5, 6, 7, BigDecimal.TEN);
        when(reportes.resumen(null)).thenReturn(resumen);

        assertThat(servicio.resumen(admin)).isSameAs(resumen);
    }

    @Test
    @DisplayName("El proveedor solo ve lo suyo (alcance = su id)")
    void proveedorVeLoSuyo() {
        when(reportes.ingresosMensuales(2L)).thenReturn(List.of());
        when(reportes.disponibilidad(2L)).thenReturn(List.of());

        assertThat(servicio.ingresosMensuales(proveedor)).isEmpty();
        assertThat(servicio.disponibilidad(proveedor)).isEmpty();
        verify(reportes).ingresosMensuales(2L);
        verify(reportes).disponibilidad(2L);
    }

    @Test
    @DisplayName("El cliente no tiene acceso a ningún reporte")
    void clienteSinAcceso() {
        assertThatThrownBy(() -> servicio.resumen(cliente)).isInstanceOf(OperacionNoPermitidaException.class);
        assertThatThrownBy(() -> servicio.herramientasMasAlquiladas(cliente, 5)).isInstanceOf(OperacionNoPermitidaException.class);
        verifyNoInteractions(reportes);
    }

    @Test
    @DisplayName("El límite de los rankings se acota entre 1 y 50")
    void limiteAcotado() {
        when(reportes.herramientasMasAlquiladas(null, 1)).thenReturn(List.of());
        when(reportes.clientesFrecuentes(null, 50)).thenReturn(List.of());

        servicio.herramientasMasAlquiladas(admin, -10);
        servicio.clientesFrecuentes(admin, 999);

        verify(reportes).herramientasMasAlquiladas(null, 1);
        verify(reportes).clientesFrecuentes(null, 50);
    }
}
