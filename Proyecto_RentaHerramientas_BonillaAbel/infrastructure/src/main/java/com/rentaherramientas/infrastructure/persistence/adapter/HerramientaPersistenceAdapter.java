package com.rentaherramientas.infrastructure.persistence.adapter;

import com.rentaherramientas.domain.model.EstadoHerramienta;
import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.FiltroHerramienta;
import com.rentaherramientas.domain.model.Herramienta;
import com.rentaherramientas.domain.port.out.HerramientaRepositoryPort;
import com.rentaherramientas.infrastructure.persistence.entity.HerramientaEntity;
import com.rentaherramientas.infrastructure.persistence.mapper.PersistenceMappers;
import com.rentaherramientas.infrastructure.persistence.repository.CategoriaJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.HerramientaJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.ProveedorJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HerramientaPersistenceAdapter implements HerramientaRepositoryPort {

    private final HerramientaJpaRepository jpa;
    private final ProveedorJpaRepository proveedores;
    private final CategoriaJpaRepository categorias;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Herramienta guardar(Herramienta h) {
        HerramientaEntity e = h.getId() == null ? new HerramientaEntity() : jpa.findById(h.getId()).orElseGet(HerramientaEntity::new);
        e.setProveedor(proveedores.getReferenceById(h.getProveedorId()));
        e.setCategoria(categorias.getReferenceById(h.getCategoriaId()));
        e.setNombre(h.getNombre());
        e.setDescripcion(h.getDescripcion());
        e.setMarca(h.getMarca());
        e.setPrecioDia(h.getPrecioDia());
        e.setEstado(h.getEstado());
        e.setImagenUrl(h.getImagenUrl());
        e.setReservaAutomatica(h.isReservaAutomatica());
        return PersistenceMappers.herramienta(jpa.save(e));
    }

    @Override
    public Optional<Herramienta> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMappers::herramienta);
    }

    @Override
    public Optional<Herramienta> buscarPorIdParaActualizar(Long id) {
        return jpa.buscarParaActualizar(id).map(PersistenceMappers::herramienta);
    }

    @Override
    public List<Herramienta> buscar(FiltroHerramienta f) {
        // JPQL dinámico: solo se agregan las condiciones que el usuario pidió.
        StringBuilder jpql = new StringBuilder("""
                SELECT h FROM HerramientaEntity h
                JOIN FETCH h.proveedor p
                JOIN FETCH h.categoria c
                WHERE h.estado <> :inactiva
                """);
        if (f.categoriaId() != null) {
            jpql.append(" AND c.id = :categoriaId");
        }
        if (f.proveedorId() != null) {
            jpql.append(" AND p.id = :proveedorId");
        }
        if (f.texto() != null && !f.texto().isBlank()) {
            jpql.append(" AND (LOWER(h.nombre) LIKE :texto OR LOWER(h.marca) LIKE :texto OR LOWER(h.descripcion) LIKE :texto)");
        }
        if (f.soloDisponibles()) {
            jpql.append(" AND h.estado = :disponible");
        }
        boolean porFechas = f.desde() != null && f.hasta() != null;
        if (porFechas) {
            jpql.append("""
                     AND h.estado = :disponible
                     AND NOT EXISTS (
                         SELECT 1 FROM ReservaEntity r
                         WHERE r.herramienta = h
                           AND r.estado IN :bloqueantes
                           AND r.fechaInicio <= :hasta
                           AND r.fechaFin >= :desde)
                    """);
        }
        jpql.append(" ORDER BY h.nombre ASC");

        TypedQuery<HerramientaEntity> q = em.createQuery(jpql.toString(), HerramientaEntity.class);
        q.setParameter("inactiva", EstadoHerramienta.INACTIVA);
        if (f.categoriaId() != null) {
            q.setParameter("categoriaId", f.categoriaId());
        }
        if (f.proveedorId() != null) {
            q.setParameter("proveedorId", f.proveedorId());
        }
        if (f.texto() != null && !f.texto().isBlank()) {
            q.setParameter("texto", "%" + f.texto().trim().toLowerCase() + "%");
        }
        if (f.soloDisponibles() || porFechas) {
            q.setParameter("disponible", EstadoHerramienta.DISPONIBLE);
        }
        if (porFechas) {
            q.setParameter("bloqueantes", EstadoReserva.bloqueantes());
            q.setParameter("desde", f.desde());
            q.setParameter("hasta", f.hasta());
        }
        return q.getResultList().stream().map(PersistenceMappers::herramienta).toList();
    }

    @Override
    public List<Herramienta> listarPorProveedor(Long proveedorId) {
        return jpa.findByProveedor_IdAndEstadoNotOrderByNombreAsc(proveedorId, EstadoHerramienta.INACTIVA)
                .stream().map(PersistenceMappers::herramienta).toList();
    }

    @Override
    public boolean existePorCategoria(Long categoriaId) {
        return jpa.existsByCategoria_Id(categoriaId);
    }
}
