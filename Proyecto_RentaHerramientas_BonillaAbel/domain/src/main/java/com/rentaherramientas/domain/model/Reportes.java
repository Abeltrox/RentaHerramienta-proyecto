package com.rentaherramientas.domain.model;

import java.math.BigDecimal;

/** Modelos de solo lectura usados por los reportes y estadísticas. */
public final class Reportes {

    private Reportes() {
    }

    public record ResumenGeneral(
            long totalProveedores,
            long totalClientes,
            long totalHerramientas,
            long reservasPendientes,
            long reservasActivas,
            long devolucionesPendientes,
            long danosPendientes,
            BigDecimal ingresosTotales) {
    }

    /** Ingresos agrupados por mes (formato yyyy-MM). */
    public record IngresoMensual(String periodo, BigDecimal total, long cantidadPagos) {
    }

    public record HerramientaRanking(Long herramientaId, String nombre, long vecesAlquilada, BigDecimal ingresos) {
    }

    public record ClienteFrecuente(Long clienteId, String nombre, long reservas, BigDecimal gastoTotal) {
    }

    public record DisponibilidadHerramienta(
            Long herramientaId,
            String nombre,
            String categoria,
            String proveedor,
            EstadoHerramienta estado,
            boolean libreHoy) {
    }
}
