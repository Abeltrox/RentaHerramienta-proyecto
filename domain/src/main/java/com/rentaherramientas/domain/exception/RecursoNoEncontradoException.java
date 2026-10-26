package com.rentaherramientas.domain.exception;

/** El recurso solicitado no existe (HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoEncontradoException de(String recurso, Long id) {
        return new RecursoNoEncontradoException(recurso + " con id " + id + " no existe");
    }
}
