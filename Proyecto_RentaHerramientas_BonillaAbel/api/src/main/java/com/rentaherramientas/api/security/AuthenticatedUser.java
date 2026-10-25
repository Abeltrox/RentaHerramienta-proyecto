package com.rentaherramientas.api.security;

import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Rol;

/** Principal guardado en el SecurityContext tras validar el JWT. */
public record AuthenticatedUser(Long id, String email, Rol rol) {

    public Actor comoActor() {
        return new Actor(id, rol);
    }
}
