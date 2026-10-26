package com.rentaherramientas.infrastructure.persistence.adapter;

import com.rentaherramientas.domain.model.Pago;
import com.rentaherramientas.domain.port.out.PagoRepositoryPort;
import com.rentaherramientas.infrastructure.persistence.entity.PagoEntity;
import com.rentaherramientas.infrastructure.persistence.mapper.PersistenceMappers;
import com.rentaherramientas.infrastructure.persistence.repository.PagoJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.ReservaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PagoPersistenceAdapter implements PagoRepositoryPort {

    private final PagoJpaRepository jpa;
    private final ReservaJpaRepository reservas;

    @Override
    public Pago guardar(Pago p) {
        PagoEntity e = p.getId() == null ? new PagoEntity() : jpa.findById(p.getId()).orElseGet(PagoEntity::new);
        e.setReserva(reservas.getReferenceById(p.getReservaId()));
        e.setMonto(p.getMonto());
        e.setMetodo(p.getMetodo());
        e.setEstado(p.getEstado());
        e.setReferencia(p.getReferencia());
        e.setFechaPago(p.getFechaPago());
        return PersistenceMappers.pago(jpa.save(e));
    }

    @Override
    public Optional<Pago> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMappers::pago);
    }

    @Override
    public Optional<Pago> buscarPorReservaId(Long reservaId) {
        return jpa.findByReserva_Id(reservaId).map(PersistenceMappers::pago);
    }

    @Override
    public List<Pago> listarTodos() {
        return jpa.findAllByOrderByFechaPagoDesc().stream().map(PersistenceMappers::pago).toList();
    }

    @Override
    public List<Pago> listarPorCliente(Long clienteId) {
        return jpa.findByReserva_Cliente_IdOrderByFechaPagoDesc(clienteId).stream().map(PersistenceMappers::pago).toList();
    }

    @Override
    public List<Pago> listarPorProveedor(Long proveedorId) {
        return jpa.findByReserva_Herramienta_Proveedor_IdOrderByFechaPagoDesc(proveedorId)
                .stream().map(PersistenceMappers::pago).toList();
    }
}
