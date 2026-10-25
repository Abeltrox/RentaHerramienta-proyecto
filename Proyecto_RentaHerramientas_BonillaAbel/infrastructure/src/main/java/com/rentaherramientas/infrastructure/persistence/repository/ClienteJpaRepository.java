package com.rentaherramientas.infrastructure.persistence.repository;

import com.rentaherramientas.infrastructure.persistence.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    List<ClienteEntity> findAllByOrderByIdAsc();
}
