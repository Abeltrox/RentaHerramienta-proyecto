package com.rentaherramientas.infrastructure.persistence.adapter;

import com.rentaherramientas.domain.model.Factura;
import com.rentaherramientas.domain.port.out.FacturaRepositoryPort;
import com.rentaherramientas.infrastructure.persistence.entity.FacturaEntity;
import com.rentaherramientas.infrastructure.persistence.mapper.PersistenceMappers;
import com.rentaherramientas.infrastructure.persistence.repository.FacturaJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.PagoJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.ReservaJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FacturaPersistenceAdapter implements FacturaRepositoryPort {

    private final FacturaJpaRepository jpa;
    private final ReservaJpaRepository reservas;
    private final PagoJpaRepository pagos;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Factura guardar(Factura f) {
        FacturaEntity e = f.getId() == null ? new FacturaEntity() : jpa.findById(f.getId()).orElseGet(FacturaEntity::new);
        e.setNumero(f.getNumero());
        e.setReserva(reservas.getReferenceById(f.getReserva().getId()));
        e.setPago(pagos.getReferenceById(f.getPago().getId()));
        e.setSubtotal(f.getSubtotal());
        e.setIva(f.getIva());
        e.setTotal(f.getTotal());
        return PersistenceMappers.factura(jpa.save(e));
    }

    @Override
    public Optional<Factura> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMappers::factura);
    }

    @Override
    public Optional<Factura> buscarPorReservaId(Long reservaId) {
        return jpa.findByReserva_Id(reservaId).map(PersistenceMappers::factura);
    }

    @Override
    public List<Factura> listarTodas() {
        return jpa.findAllByOrderByFechaEmisionDesc().stream().map(PersistenceMappers::factura).toList();
    }

    @Override
    public List<Factura> listarPorCliente(Long clienteId) {
        return jpa.findByReserva_Cliente_IdOrderByFechaEmisionDesc(clienteId).stream().map(PersistenceMappers::factura).toList();
    }

    @Override
    public List<Factura> listarPorProveedor(Long proveedorId) {
        return jpa.findByReserva_Herramienta_Proveedor_IdOrderByFechaEmisionDesc(proveedorId)
                .stream().map(PersistenceMappers::factura).toList();
    }

    @Override
    public long siguienteConsecutivo() {
        return ((Number) em.createNativeQuery("SELECT nextval('seq_factura_numero')").getSingleResult()).longValue();
    }
}
