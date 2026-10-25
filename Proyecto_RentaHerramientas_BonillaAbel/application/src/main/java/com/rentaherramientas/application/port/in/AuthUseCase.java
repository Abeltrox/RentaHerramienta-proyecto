package com.rentaherramientas.application.port.in;

import com.rentaherramientas.domain.model.Usuario;

/** Caso de uso: registro de clientes e inicio de sesión. */
public interface AuthUseCase {

    AuthResultado registrarCliente(RegistroClienteComando comando);

    AuthResultado login(String email, String password);

    Usuario perfil(Long usuarioId);

    record RegistroClienteComando(String nombre, String email, String password, String telefono,
                                  String documento, String direccion, String ciudad) {
    }

    record AuthResultado(String token, long expiraEnSegundos, Usuario usuario) {
    }
}
