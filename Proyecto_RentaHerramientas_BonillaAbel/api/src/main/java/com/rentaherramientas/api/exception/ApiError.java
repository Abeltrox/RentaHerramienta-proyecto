package com.rentaherramientas.api.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

/** Formato único de error para toda la API. */
public record ApiError(LocalDateTime timestamp, int status, String error, String message, String path, List<String> details) {

    public static ApiError de(HttpStatus status, String mensaje, String ruta, List<String> detalles) {
        return new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, ruta, detalles);
    }
}
