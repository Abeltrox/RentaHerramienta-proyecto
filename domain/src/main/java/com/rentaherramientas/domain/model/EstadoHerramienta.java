package com.rentaherramientas.domain.model;

/** Estado físico/administrativo de un equipo. La disponibilidad por fechas se calcula con las reservas. */
public enum EstadoHerramienta {
    DISPONIBLE,
    EN_MANTENIMIENTO,
    DANADA,
    /** Eliminación lógica: se conserva el historial de alquileres. */
    INACTIVA
}
