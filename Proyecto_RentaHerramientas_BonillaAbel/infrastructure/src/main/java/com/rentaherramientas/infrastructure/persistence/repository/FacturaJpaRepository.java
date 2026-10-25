package com.rentaherramientas.infrastructure.persistence.repository;

import com.rentaherramientas.infrastructure.persistence.entity.FacturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacturaJpaRepository extends JpaRepository<FacturaEntity, Long> {

    Optional<FacturaEntity> findByReserva_Id(Long reservaId);

    List<FacturaEntity> findAllByOrderByFechaEmisionDesc();

    List<FacturaEntity> findByReserva_Cliente_IdOrderByFechaEmisionDesc(Long clienteId);

    List<FacturaEntity> findByReserva_Herramienta_Proveedor_IdOrderByFechaEmisionDesc(Long proveedorId);
}
