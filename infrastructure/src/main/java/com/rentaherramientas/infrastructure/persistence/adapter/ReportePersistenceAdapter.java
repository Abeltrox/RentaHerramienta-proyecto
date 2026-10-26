package com.rentaherramientas.infrastructure.persistence.adapter;

import com.rentaherramientas.domain.model.EstadoHerramienta;
import com.rentaherramientas.domain.model.Reportes.ClienteFrecuente;
import com.rentaherramientas.domain.model.Reportes.DisponibilidadHerramienta;
import com.rentaherramientas.domain.model.Reportes.HerramientaRanking;
import com.rentaherramientas.domain.model.Reportes.IngresoMensual;
import com.rentaherramientas.domain.model.Reportes.ResumenGeneral;
import com.rentaherramientas.domain.port.out.ReporteRepositoryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reportes con SQL nativo (agregaciones). Cuando {@code proveedorId} es null se consulta toda la plataforma.
 * El filtro por proveedor se agrega al SQL solo si aplica (evita parámetros nulos sin tipo en PostgreSQL).
 */
@Component
public class ReportePersistenceAdapter implements ReporteRepositoryPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public ResumenGeneral resumen(Long proveedorId) {
        String h = filtro(proveedorId, "h.proveedor_id");
        long proveedores = proveedorId == null ? contar("SELECT COUNT(*) FROM proveedores p JOIN usuarios u ON u.id = p.id WHERE u.activo", null) : 1;
        long clientes = proveedorId == null ? contar("SELECT COUNT(*) FROM clientes c JOIN usuarios u ON u.id = c.id WHERE u.activo", null)
                : contar("SELECT COUNT(DISTINCT r.cliente_id) FROM reservas r JOIN herramientas h ON h.id = r.herramienta_id WHERE 1=1" + h, proveedorId);
        long herramientas = contar("SELECT COUNT(*) FROM herramientas h WHERE h.estado <> 'INACTIVA'" + h, proveedorId);
        long pendientes = contarReservas("r.estado = 'PENDIENTE'", proveedorId);
        long activas = contarReservas("r.estado IN ('APROBADA','PAGADA','ENTREGADA')", proveedorId);
        long porDevolver = contarReservas("r.estado = 'ENTREGADA'", proveedorId);
        long danos = contar("SELECT COUNT(*) FROM reportes_danio d JOIN herramientas h ON h.id = d.herramienta_id "
                + "WHERE d.estado <> 'RESUELTO'" + h, proveedorId);
        BigDecimal ingresos = decimal(consulta("SELECT COALESCE(SUM(p.monto),0) FROM pagos p JOIN reservas r ON r.id = p.reserva_id "
                + "JOIN herramientas h ON h.id = r.herramienta_id WHERE p.estado = 'APROBADO'" + h, proveedorId).getSingleResult());
        return new ResumenGeneral(proveedores, clientes, herramientas, pendientes, activas, porDevolver, danos, ingresos);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<IngresoMensual> ingresosMensuales(Long proveedorId) {
        String sql = "SELECT to_char(p.fecha_pago, 'YYYY-MM') AS periodo, SUM(p.monto), COUNT(*) "
                + "FROM pagos p JOIN reservas r ON r.id = p.reserva_id JOIN herramientas h ON h.id = r.herramienta_id "
                + "WHERE p.estado = 'APROBADO'" + filtro(proveedorId, "h.proveedor_id")
                + " GROUP BY periodo ORDER BY periodo";
        List<Object[]> filas = consulta(sql, proveedorId).getResultList();
        return filas.stream().map(f -> new IngresoMensual((String) f[0], decimal(f[1]), ((Number) f[2]).longValue())).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HerramientaRanking> herramientasMasAlquiladas(Long proveedorId, int limite) {
        String sql = "SELECT h.id, h.nombre, COUNT(r.id) AS veces, COALESCE(SUM(r.total),0) "
                + "FROM reservas r JOIN herramientas h ON h.id = r.herramienta_id "
                + "WHERE r.estado IN ('PAGADA','ENTREGADA','DEVUELTA')" + filtro(proveedorId, "h.proveedor_id")
                + " GROUP BY h.id, h.nombre ORDER BY veces DESC, h.nombre LIMIT " + limite;
        List<Object[]> filas = consulta(sql, proveedorId).getResultList();
        return filas.stream().map(f -> new HerramientaRanking(((Number) f[0]).longValue(), (String) f[1],
                ((Number) f[2]).longValue(), decimal(f[3]))).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ClienteFrecuente> clientesFrecuentes(Long proveedorId, int limite) {
        String sql = "SELECT c.id, u.nombre, COUNT(r.id) AS reservas, COALESCE(SUM(r.total),0) "
                + "FROM reservas r JOIN clientes c ON c.id = r.cliente_id JOIN usuarios u ON u.id = c.id "
                + "JOIN herramientas h ON h.id = r.herramienta_id "
                + "WHERE r.estado IN ('PAGADA','ENTREGADA','DEVUELTA')" + filtro(proveedorId, "h.proveedor_id")
                + " GROUP BY c.id, u.nombre ORDER BY reservas DESC, u.nombre LIMIT " + limite;
        List<Object[]> filas = consulta(sql, proveedorId).getResultList();
        return filas.stream().map(f -> new ClienteFrecuente(((Number) f[0]).longValue(), (String) f[1],
                ((Number) f[2]).longValue(), decimal(f[3]))).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DisponibilidadHerramienta> disponibilidad(Long proveedorId) {
        String sql = "SELECT h.id, h.nombre, c.nombre, pr.empresa, h.estado, "
                + "(h.estado = 'DISPONIBLE' AND NOT EXISTS (SELECT 1 FROM reservas r WHERE r.herramienta_id = h.id "
                + "AND r.estado IN ('PENDIENTE','APROBADA','PAGADA','ENTREGADA') "
                + "AND r.fecha_inicio <= CURRENT_DATE AND r.fecha_fin >= CURRENT_DATE)) AS libre "
                + "FROM herramientas h JOIN categorias c ON c.id = h.categoria_id JOIN proveedores pr ON pr.id = h.proveedor_id "
                + "WHERE h.estado <> 'INACTIVA'" + filtro(proveedorId, "h.proveedor_id") + " ORDER BY h.nombre";
        List<Object[]> filas = consulta(sql, proveedorId).getResultList();
        return filas.stream().map(f -> new DisponibilidadHerramienta(((Number) f[0]).longValue(), (String) f[1],
                (String) f[2], (String) f[3], EstadoHerramienta.valueOf((String) f[4]), (Boolean) f[5])).toList();
    }

    // ----------------------------------------------------------------- helpers

    private String filtro(Long proveedorId, String columna) {
        return proveedorId == null ? "" : " AND " + columna + " = :proveedorId";
    }

    private Query consulta(String sql, Long proveedorId) {
        Query q = em.createNativeQuery(sql);
        if (proveedorId != null && sql.contains(":proveedorId")) {
            q.setParameter("proveedorId", proveedorId);
        }
        return q;
    }

    private long contar(String sql, Long proveedorId) {
        return ((Number) consulta(sql, proveedorId).getSingleResult()).longValue();
    }

    private long contarReservas(String condicion, Long proveedorId) {
        return contar("SELECT COUNT(*) FROM reservas r JOIN herramientas h ON h.id = r.herramienta_id WHERE "
                + condicion + filtro(proveedorId, "h.proveedor_id"), proveedorId);
    }

    private BigDecimal decimal(Object valor) {
        return valor == null ? BigDecimal.ZERO : new BigDecimal(valor.toString());
    }
}
