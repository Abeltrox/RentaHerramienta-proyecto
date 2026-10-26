package com.rentaherramientas.api.security;

import com.rentaherramientas.domain.exception.AutenticacionException;
import com.rentaherramientas.domain.model.Actor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Traduce el usuario autenticado (Spring Security) al {@link Actor} que entiende el dominio. */
public final class ActorResolver {

    private ActorResolver() {
    }

    public static Actor actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser usuario) {
            return usuario.comoActor();
        }
        throw new AutenticacionException("Debes iniciar sesión");
    }
}
