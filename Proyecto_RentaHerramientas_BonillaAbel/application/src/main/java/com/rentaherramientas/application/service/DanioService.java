package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.DanioUseCase;
import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.exception.RecursoNoEncontradoException;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.EstadoDanio;
import com.rentaherramientas.domain.model.EstadoHerramienta;
import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.Herramienta;
import com.rentaherramientas.domain.model.ReporteDanio;
import com.rentaherramientas.domain.model.Reserva;
import com.rentaherramientas.domain.port.out.DanioRepositoryPort;
import com.rentaherramientas.domain.port.out.HerramientaRepositoryPort;
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
public class DanioService implements DanioUseCase {

    private final DanioRepositoryPort danios;
    private final ReservaRepositoryPort reservas;
    private final HerramientaRepositoryPort herramientas;

    @Override
    @Transactional(readOnly = true)
    public List<ReporteDanio> listar(Actor actor) {
        if (actor.esAdmin()) {
            return danios.listarTodos();
        }
        if (actor.esProveedor()) {
            return danios.listarPorProveedor(actor.usuarioId());
        }
        throw new OperacionNoPermitidaException("Los clientes no consultan reportes de daños");
    }

    @Override
    public ReporteDanio crear(CrearDanioComando c, Actor actor) {
        Reserva reserva = reservas.buscarPorId(c.reservaId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Reserva", c.reservaId()));
        boolean esProveedorDueno = actor.esProveedor() && actor.usuarioId().equals(reserva.getProveedorId());
        if (!actor.esAdmin() && !esProveedorDueno) {
            throw new OperacionNoPermitidaException("Solo el proveedor de la herramienta o un administrador pueden reportar daños");
        }
        if (reserva.getEstado() != EstadoReserva.ENTREGADA && reserva.getEstado() != EstadoReserva.DEVUELTA) {
            throw new ReglaNegocioException("Solo se reportan daños de reservas entregadas o devueltas");
        }
        ReporteDanio reporte = danios.guardar(ReporteDanio.builder()
                .reservaId(reserva.getId())
                .herramientaId(reserva.getHerramientaId())
                .herramientaNombre(reserva.getHerramientaNombre())
                .proveedorId(reserva.getProveedorId())
                .descripcion(c.descripcion().trim())
                .costoEstimado(c.costoEstimado() == null ? BigDecimal.ZERO : c.costoEstimado())
                .estado(EstadoDanio.REPORTADO)
                .build());
        cambiarEstadoHerramienta(reserva.getHerramientaId(), EstadoHerramienta.DANADA);
        return reporte;
    }

    @Override
    public ReporteDanio cambiarEstado(Long id, EstadoDanio nuevoEstado, Actor actor) {
        if (!actor.esAdmin()) {
            throw new OperacionNoPermitidaException("Solo un administrador puede gestionar las incidencias");
        }
        ReporteDanio reporte = danios.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Reporte de daño", id));
        if (nuevoEstado.ordinal() <= reporte.getEstado().ordinal()) {
            throw new ReglaNegocioException("La incidencia ya está en " + reporte.getEstado() + "; solo puede avanzar de estado");
        }
        reporte.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoDanio.RESUELTO) {
            reporte.setFechaResolucion(LocalDateTime.now());
            // Al resolverse la incidencia el equipo vuelve al catálogo.
            herramientas.buscarPorId(reporte.getHerramientaId())
                    .filter(h -> h.getEstado() == EstadoHerramienta.DANADA)
                    .ifPresent(h -> cambiarEstadoHerramienta(h.getId(), EstadoHerramienta.DISPONIBLE));
        }
        return danios.guardar(reporte);
    }

    private void cambiarEstadoHerramienta(Long herramientaId, EstadoHerramienta estado) {
        Herramienta herramienta = herramientas.buscarPorId(herramientaId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Herramienta", herramientaId));
        herramienta.setEstado(estado);
        herramientas.guardar(herramienta);
    }
}
