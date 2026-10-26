package com.rentaherramientas.application.port.in;

import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Cliente;

import java.util.List;

/** Caso de uso: gestión de clientes (CRUD). */
public interface ClienteUseCase {

    Cliente crear(ClienteComando comando);

    Cliente actualizar(Long id, ClienteComando comando, Actor actor);

    void eliminar(Long id);

    List<Cliente> listar();

    Cliente obtener(Long id, Actor actor);

    record ClienteComando(String nombre, String email, String password, String telefono,
                          String documento, String direccion, String ciudad) {
    }
}
