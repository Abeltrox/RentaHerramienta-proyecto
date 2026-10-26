package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.Proveedor;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepositoryPort {

    Proveedor guardar(Proveedor proveedor);

    Optional<Proveedor> buscarPorId(Long id);

    List<Proveedor> listar();

    boolean existePorNit(String nit);

    boolean existePorNitYIdDistinto(String nit, Long id);
}
