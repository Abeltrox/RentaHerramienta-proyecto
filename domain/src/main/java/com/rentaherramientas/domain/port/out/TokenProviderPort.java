package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.Usuario;

/** Puerto de salida: emisión de tokens de acceso (la implementación usa JWT). */
public interface TokenProviderPort {

    String generar(Usuario usuario);

    long expiracionSegundos();
}
