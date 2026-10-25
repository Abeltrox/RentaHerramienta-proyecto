package com.rentaherramientas.infrastructure.persistence.repository;

import com.rentaherramientas.infrastructure.persistence.entity.ProveedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProveedorJpaRepository extends JpaRepository<ProveedorEntity, Long> {

    boolean existsByNit(String nit);

    boolean existsByNitAndIdNot(String nit, Long id);

    List<ProveedorEntity> findAllByOrderByEmpresaAsc();
}
