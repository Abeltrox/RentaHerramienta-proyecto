package com.rentaherramientas.infrastructure.persistence.repository;

import com.rentaherramientas.domain.model.EstadoHerramienta;
import com.rentaherramientas.infrastructure.persistence.entity.HerramientaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HerramientaJpaRepository extends JpaRepository<HerramientaEntity, Long> {

    List<HerramientaEntity> findByProveedor_IdAndEstadoNotOrderByNombreAsc(Long proveedorId, EstadoHerramienta estado);

    boolean existsByCategoria_Id(Long categoriaId);

    /** Bloqueo pesimista de escritura: serializa las reservas concurrentes sobre la misma herramienta. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HerramientaEntity h WHERE h.id = :id")
    Optional<HerramientaEntity> buscarParaActualizar(@Param("id") Long id);
}
