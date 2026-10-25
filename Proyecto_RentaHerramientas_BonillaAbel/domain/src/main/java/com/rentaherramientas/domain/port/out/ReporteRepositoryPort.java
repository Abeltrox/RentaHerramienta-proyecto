package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.Reportes.ClienteFrecuente;
import com.rentaherramientas.domain.model.Reportes.DisponibilidadHerramienta;
import com.rentaherramientas.domain.model.Reportes.HerramientaRanking;
import com.rentaherramientas.domain.model.Reportes.IngresoMensual;
import com.rentaherramientas.domain.model.Reportes.ResumenGeneral;

import java.util.List;

/** Consultas de agregación para reportes. {@code proveedorId == null} significa "toda la plataforma". */
public interface ReporteRepositoryPort {

    ResumenGeneral resumen(Long proveedorId);

    List<IngresoMensual> ingresosMensuales(Long proveedorId);

    List<HerramientaRanking> herramientasMasAlquiladas(Long proveedorId, int limite);

    List<ClienteFrecuente> clientesFrecuentes(Long proveedorId, int limite);

    List<DisponibilidadHerramienta> disponibilidad(Long proveedorId);
}
