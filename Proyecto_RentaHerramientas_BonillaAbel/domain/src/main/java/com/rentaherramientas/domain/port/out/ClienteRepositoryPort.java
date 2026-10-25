package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryPort {

    Cliente guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long id);

    List<Cliente> listar();

    boolean existePorDocumento(String documento);

    boolean existePorDocumentoYIdDistinto(String documento, Long id);
}
