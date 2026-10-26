package com.rentaherramientas.domain.exception;

/** El usuario autenticado no puede ejecutar esta operación sobre este recurso (HTTP 403). */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
