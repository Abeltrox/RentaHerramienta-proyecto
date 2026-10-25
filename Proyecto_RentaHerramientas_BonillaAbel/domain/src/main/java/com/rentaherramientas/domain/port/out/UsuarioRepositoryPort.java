package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.Usuario;

import java.util.Optional;

/** Puerto de salida: persistencia de usuarios. */
public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorEmail(String email);

    boolean existePorEmailYIdDistinto(String email, Long id);
}
