package com.rentaherramientas.infrastructure.persistence.repository;

import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.infrastructure.persistence.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservaJpaRepository extends JpaRepository<ReservaEntity, Long> {

    List<ReservaEntity> findAllByOrderByFechaCreacionDesc();

    List<ReservaEntity> findByCliente_IdOrderByFechaCreacionDesc(Long clienteId);

    List<ReservaEntity> findByHerramienta_Proveedor_IdOrderByFechaCreacionDesc(Long proveedorId);

    List<ReservaEntity> findByEstado(EstadoReserva estado);

    boolean existsByHerramienta_IdAndEstadoIn(Long herramientaId, Collection<EstadoReserva> estados);

    /** Cuenta reservas activas que se cruzan con [inicio, fin] (los extremos cuentan como ocupados). */
    @Query("""
            SELECT COUNT(r) FROM ReservaEntity r
            WHERE r.herramienta.id = :herramientaId
              AND r.estado IN :estados
              AND r.fechaInicio <= :fin
              AND r.fechaFin >= :inicio
              AND r.id <> :excluirId
            """)
    long contarSolapadas(@Param("herramientaId") Long herramientaId,
                         @Param("estados") Collection<EstadoReserva> estados,
                         @Param("inicio") LocalDate inicio,
                         @Param("fin") LocalDate fin,
                         @Param("excluirId") Long excluirId);
}
