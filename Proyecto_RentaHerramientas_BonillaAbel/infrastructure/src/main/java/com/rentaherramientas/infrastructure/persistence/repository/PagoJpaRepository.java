package com.rentaherramientas.infrastructure.persistence.repository;

import com.rentaherramientas.infrastructure.persistence.entity.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoJpaRepository extends JpaRepository<PagoEntity, Long> {

    Optional<PagoEntity> findByReserva_Id(Long reservaId);

    List<PagoEntity> findAllByOrderByFechaPagoDesc();

    List<PagoEntity> findByReserva_Cliente_IdOrderByFechaPagoDesc(Long clienteId);

    List<PagoEntity> findByReserva_Herramienta_Proveedor_IdOrderByFechaPagoDesc(Long proveedorId);
}
