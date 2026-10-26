package com.rentaherramientas.infrastructure.persistence.adapter;

import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.Reserva;
import com.rentaherramientas.domain.port.out.ReservaRepositoryPort;
import com.rentaherramientas.infrastructure.persistence.entity.ReservaEntity;
import com.rentaherramientas.infrastructure.persistence.mapper.PersistenceMappers;
import com.rentaherramientas.infrastructure.persistence.repository.ClienteJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.HerramientaJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.ReservaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReservaPersistenceAdapter implements ReservaRepositoryPort {

    private final ReservaJpaRepository jpa;
    private final ClienteJpaRepository clientes;
    private final HerramientaJpaRepository herramientas;

    @Override
    public Reserva guardar(Reserva r) {
        ReservaEntity e = r.getId() == null ? new ReservaEntity() : jpa.findById(r.getId()).orElseGet(ReservaEntity::new);
        e.setCliente(clientes.getReferenceById(r.getClienteId()));
        e.setHerramienta(herramientas.getReferenceById(r.getHerramientaId()));
        e.setFechaInicio(r.getFechaInicio());
        e.setFechaFin(r.getFechaFin());
        e.setDias(r.getDias());
        e.setPrecioDia(r.getPrecioDia());
        e.setTotal(r.getTotal());
        e.setEstado(r.getEstado());
        e.setObservaciones(r.getObservaciones());
        e.setMotivoRechazo(r.getMotivoRechazo());
        e.setFechaEntrega(r.getFechaEntrega());
        e.setFechaDevolucion(r.getFechaDevolucion());
        return PersistenceMappers.reserva(jpa.save(e));
    }

    @Override
    public Optional<Reserva> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMappers::reserva);
    }

    @Override
    public List<Reserva> listarTodas() {
        return jpa.findAllByOrderByFechaCreacionDesc().stream().map(PersistenceMappers::reserva).toList();
    }

    @Override
    public List<Reserva> listarPorCliente(Long clienteId) {
        return jpa.findByCliente_IdOrderByFechaCreacionDesc(clienteId).stream().map(PersistenceMappers::reserva).toList();
    }

    @Override
    public List<Reserva> listarPorProveedor(Long proveedorId) {
        return jpa.findByHerramienta_Proveedor_IdOrderByFechaCreacionDesc(proveedorId)
                .stream().map(PersistenceMappers::reserva).toList();
    }

    @Override
    public List<Reserva> listarPorEstado(EstadoReserva estado) {
        return jpa.findByEstado(estado).stream().map(PersistenceMappers::reserva).toList();
    }

    @Override
    public boolean existeSolapamiento(Long herramientaId, LocalDate inicio, LocalDate fin, Long excluirReservaId) {
        return jpa.contarSolapadas(herramientaId, EstadoReserva.bloqueantes(), inicio, fin, excluirReservaId) > 0;
    }

    @Override
    public boolean herramientaTieneReservasActivas(Long herramientaId) {
        return jpa.existsByHerramienta_IdAndEstadoIn(herramientaId, EstadoReserva.bloqueantes());
    }
}
