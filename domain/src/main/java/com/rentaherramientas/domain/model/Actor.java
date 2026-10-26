package com.rentaherramientas.domain.model;

/**
 * Quién ejecuta una operación (usuario autenticado). El dominio no depende de Spring Security:
 * la capa de seguridad traduce el token JWT a este objeto.
 * Nota: el id de un proveedor o cliente coincide con el id de su usuario.
 */
public record Actor(Long usuarioId, Rol rol) {

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    public boolean esProveedor() {
        return rol == Rol.PROVEEDOR;
    }

    public boolean esCliente() {
        return rol == Rol.CLIENTE;
    }

    /** El administrador puede gestionar todo; los demás solo lo que les pertenece. */
    public boolean puedeGestionar(Long propietarioId) {
        return esAdmin() || (usuarioId != null && usuarioId.equals(propietarioId));
    }
}
