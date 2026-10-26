package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.ReservaUseCase;
import com.rentaherramientas.domain.exception.ConflictoException;
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
import com.rentaherramientas.domain.port.out.ClienteRepositoryPort;
import com.rentaherramientas.domain.port.out.DanioRepositoryPort;
import com.rentaherramientas.domain.port.out.HerramientaRepositoryPort;
import com.rentaherramientas.domain.port.out.ReservaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservaService implements ReservaUseCase {

    static final int MAX_DIAS_ALQUILER = 90;
    static final int HORAS_MAX_PENDIENTE = 48;
    static final int HORAS_MAX_SIN_PAGO = 72;

    private final ReservaRepositoryPort reservas;
    private final HerramientaRepositoryPort herramientas;
    private final ClienteRepositoryPort clientes;
    private final DanioRepositoryPort danios;

    @Override
    public Reserva crear(CrearReservaComando c, Actor actor) {
        if (!actor.esCliente()) {
            throw new OperacionNoPermitidaException("Solo los clientes pueden solicitar reservas");
        }
        LocalDate hoy = LocalDate.now();
        if (c.fechaInicio().isBefore(hoy)) {
            throw new ReglaNegocioException("La fecha de inicio no puede ser anterior a hoy");
        }
        if (c.fechaFin().isBefore(c.fechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la de inicio");
        }
        int dias = (int) ChronoUnit.DAYS.between(c.fechaInicio(), c.fechaFin()) + 1;
        if (dias > MAX_DIAS_ALQUILER) {
            throw new ReglaNegocioException("El alquiler máximo es de " + MAX_DIAS_ALQUILER + " días");
        }
        clientes.buscarPorId(actor.usuarioId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Cliente", actor.usuarioId()));

        // Bloquea la fila de la herramienta: dos clientes no pueden reservar el mismo equipo a la vez.
        Herramienta herramienta = herramientas.buscarPorIdParaActualizar(c.herramientaId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Herramienta", c.herramientaId()));
        if (!herramienta.estaDisponibleParaAlquiler()) {
            throw new ReglaNegocioException("La herramienta no está disponible para alquiler (estado: " + herramienta.getEstado() + ")");
        }
        if (reservas.existeSolapamiento(herramienta.getId(), c.fechaInicio(), c.fechaFin(), -1L)) {
            throw new ConflictoException("La herramienta ya está reservada en alguna de las fechas seleccionadas");
        }

        Reserva reserva = Reserva.builder()
                .clienteId(actor.usuarioId())
                .herramientaId(herramienta.getId())
                .fechaInicio(c.fechaInicio())
                .fechaFin(c.fechaFin())
                .dias(dias)
                .precioDia(herramienta.getPrecioDia())
                .total(herramienta.getPrecioDia().multiply(BigDecimal.valueOf(dias)))
                // Confirmación automática si el proveedor lo habilitó en la herramienta
                .estado(herramienta.isReservaAutomatica() ? EstadoReserva.APROBADA : EstadoReserva.PENDIENTE)
                .observaciones(c.observaciones())
                .build();
        return reservas.guardar(reserva);
    }

    @Override
    public Reserva aprobar(Long id, Actor actor) {
        Reserva reserva = cargar(id);
        exigirProveedorODueno(reserva, actor);
        reserva.aprobar();
        return reservas.guardar(reserva);
    }

    @Override
    public Reserva rechazar(Long id, String motivo, Actor actor) {
        Reserva reserva = cargar(id);
        exigirProveedorODueno(reserva, actor);
        reserva.rechazar(motivo);
        return reservas.guardar(reserva);
    }

    @Override
    public Reserva cancelar(Long id, Actor actor) {
        Reserva reserva = cargar(id);
        boolean esDueno = actor.esCliente() && actor.usuarioId().equals(reserva.getClienteId());
        if (!actor.esAdmin() && !esDueno) {
            throw new OperacionNoPermitidaException("Solo el cliente de la reserva o un administrador pueden cancelarla");
        }
        reserva.cancelar();
        return reservas.guardar(reserva);
    }

    @Override
    public Reserva registrarEntrega(Long id, Actor actor) {
        Reserva reserva = cargar(id);
        exigirProveedorODueno(reserva, actor);
        reserva.registrarEntrega();
        return reservas.guardar(reserva);
    }

    @Override
    public Reserva registrarDevolucion(Long id, DevolucionComando c, Actor actor) {
        Reserva reserva = cargar(id);
        exigirProveedorODueno(reserva, actor);
        boolean hayDanio = c != null && c.hayDanio();
        if (hayDanio && (c.descripcionDanio() == null || c.descripcionDanio().isBlank())) {
            throw new ReglaNegocioException("Describe el daño encontrado en la herramienta");
        }
        reserva.registrarDevolucion();
        Reserva guardada = reservas.guardar(reserva);

        if (hayDanio) {
            danios.guardar(ReporteDanio.builder()
                    .reservaId(guardada.getId())
                    .herramientaId(guardada.getHerramientaId())
                    .herramientaNombre(guardada.getHerramientaNombre())
                    .proveedorId(guardada.getProveedorId())
                    .descripcion(c.descripcionDanio().trim())
                    .costoEstimado(c.costoEstimado() == null ? BigDecimal.ZERO : c.costoEstimado())
                    .estado(EstadoDanio.REPORTADO)
                    .build());
            Herramienta herramienta = herramientas.buscarPorId(guardada.getHerramientaId())
                    .orElseThrow(() -> RecursoNoEncontradoException.de("Herramienta", guardada.getHerramientaId()));
            herramienta.setEstado(EstadoHerramienta.DANADA);
            herramientas.guardar(herramienta);
        }
        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Reserva obtener(Long id, Actor actor) {
        Reserva reserva = cargar(id);
        if (!actor.puedeGestionar(reserva.getClienteId()) && !actor.puedeGestionar(reserva.getProveedorId())) {
            throw new OperacionNoPermitidaException("No tienes acceso a esta reserva");
        }
        return reserva;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listar(Actor actor) {
        if (actor.esAdmin()) {
            return reservas.listarTodas();
        }
        if (actor.esProveedor()) {
            return reservas.listarPorProveedor(actor.usuarioId());
        }
        return reservas.listarPorCliente(actor.usuarioId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaDisponible(Long herramientaId, LocalDate inicio, LocalDate fin) {
        if (fin.isBefore(inicio)) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la de inicio");
        }
        Herramienta herramienta = herramientas.buscarPorId(herramientaId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Herramienta", herramientaId));
        return herramienta.estaDisponibleParaAlquiler()
                && !reservas.existeSolapamiento(herramientaId, inicio, fin, -1L);
    }

    @Override
    public int expirarVencidas() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();
        int vencidas = 0;
        for (Reserva r : reservas.listarPorEstado(EstadoReserva.PENDIENTE)) {
            if (haVencido(r, ahora.minusHours(HORAS_MAX_PENDIENTE), hoy)) {
                r.vencer();
                reservas.guardar(r);
                vencidas++;
            }
        }
        for (Reserva r : reservas.listarPorEstado(EstadoReserva.APROBADA)) {
            if (haVencido(r, ahora.minusHours(HORAS_MAX_SIN_PAGO), hoy)) {
                r.vencer();
                reservas.guardar(r);
                vencidas++;
            }
        }
        return vencidas;
    }

    // ----------------------------------------------------------------- helpers

    private boolean haVencido(Reserva r, LocalDateTime limiteCreacion, LocalDate hoy) {
        boolean creadaHaceMucho = r.getFechaCreacion() != null && r.getFechaCreacion().isBefore(limiteCreacion);
        return creadaHaceMucho || r.getFechaInicio().isBefore(hoy);
    }

    private Reserva cargar(Long id) {
        return reservas.buscarPorId(id).orElseThrow(() -> RecursoNoEncontradoException.de("Reserva", id));
    }

    /** Aprobar, rechazar, entregar y recibir son tareas del proveedor dueño de la herramienta (o del admin). */
    private void exigirProveedorODueno(Reserva reserva, Actor actor) {
        boolean esProveedorDueno = actor.esProveedor() && actor.usuarioId().equals(reserva.getProveedorId());
        if (!actor.esAdmin() && !esProveedorDueno) {
            throw new OperacionNoPermitidaException("Solo el proveedor de la herramienta o un administrador pueden hacer esto");
        }
    }
}
