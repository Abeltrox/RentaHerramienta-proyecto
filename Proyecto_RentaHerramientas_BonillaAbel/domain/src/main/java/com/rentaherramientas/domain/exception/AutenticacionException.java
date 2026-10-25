package com.rentaherramientas.domain.exception;

/** Credenciales inválidas o usuario inactivo (HTTP 401). */
public class AutenticacionException extends RuntimeException {

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
