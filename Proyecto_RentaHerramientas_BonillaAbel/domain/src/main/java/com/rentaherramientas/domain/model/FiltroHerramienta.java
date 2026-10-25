package com.rentaherramientas.domain.model;

import java.time.LocalDate;

/**
 * Criterios de búsqueda de herramientas. Todos son opcionales.
 * Si se indican {@code desde} y {@code hasta} solo se devuelven equipos libres en ese rango.
 */
public record FiltroHerramienta(
        Long categoriaId,
        String texto,
        Long proveedorId,
        LocalDate desde,
        LocalDate hasta,
        boolean soloDisponibles) {
}
