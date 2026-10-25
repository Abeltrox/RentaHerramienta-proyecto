package com.rentaherramientas.domain.exception;

/** Conflicto con el estado actual (duplicados, fechas ocupadas...) (HTTP 409). */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
