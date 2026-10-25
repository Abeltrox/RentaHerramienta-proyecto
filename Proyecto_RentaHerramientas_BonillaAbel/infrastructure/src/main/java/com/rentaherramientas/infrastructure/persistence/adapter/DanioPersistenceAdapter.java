package com.rentaherramientas.infrastructure.persistence.adapter;

import com.rentaherramientas.domain.model.ReporteDanio;
import com.rentaherramientas.domain.port.out.DanioRepositoryPort;
import com.rentaherramientas.infrastructure.persistence.entity.DanioEntity;
import com.rentaherramientas.infrastructure.persistence.mapper.PersistenceMappers;
import com.rentaherramientas.infrastructure.persistence.repository.DanioJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.HerramientaJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.ReservaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DanioPersistenceAdapter implements DanioRepositoryPort {

    private final DanioJpaRepository jpa;
    private final ReservaJpaRepository reservas;
    private final HerramientaJpaRepository herramientas;

    @Override
    public ReporteDanio guardar(ReporteDanio d) {
        DanioEntity e = d.getId() == null ? new DanioEntity() : jpa.findById(d.getId()).orElseGet(DanioEntity::new);
        e.setReserva(reservas.getReferenceById(d.getReservaId()));
        e.setHerramienta(herramientas.getReferenceById(d.getHerramientaId()));
        e.setDescripcion(d.getDescripcion());
        e.setCostoEstimado(d.getCostoEstimado());
        e.setEstado(d.getEstado());
        e.setFechaResolucion(d.getFechaResolucion());
        return PersistenceMappers.danio(jpa.save(e));
    }

    @Override
    public Optional<ReporteDanio> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMappers::danio);
    }

    @Override
    public List<ReporteDanio> listarTodos() {
        return jpa.findAllByOrderByFechaReporteDesc().stream().map(PersistenceMappers::danio).toList();
    }

    @Override
    public List<ReporteDanio> listarPorProveedor(Long proveedorId) {
        return jpa.findByHerramienta_Proveedor_IdOrderByFechaReporteDesc(proveedorId)
                .stream().map(PersistenceMappers::danio).toList();
    }
}
