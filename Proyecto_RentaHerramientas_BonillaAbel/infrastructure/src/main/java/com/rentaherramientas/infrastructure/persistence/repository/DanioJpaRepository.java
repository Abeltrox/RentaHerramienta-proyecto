package com.rentaherramientas.infrastructure.persistence.repository;

import com.rentaherramientas.infrastructure.persistence.entity.DanioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DanioJpaRepository extends JpaRepository<DanioEntity, Long> {

    List<DanioEntity> findAllByOrderByFechaReporteDesc();

    List<DanioEntity> findByHerramienta_Proveedor_IdOrderByFechaReporteDesc(Long proveedorId);
}
