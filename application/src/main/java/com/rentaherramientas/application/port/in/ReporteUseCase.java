package com.rentaherramientas.application.port.in;

import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Reportes.ClienteFrecuente;
import com.rentaherramientas.domain.model.Reportes.DisponibilidadHerramienta;
import com.rentaherramientas.domain.model.Reportes.HerramientaRanking;
import com.rentaherramientas.domain.model.Reportes.IngresoMensual;
import com.rentaherramientas.domain.model.Reportes.ResumenGeneral;

import java.util.List;

/** Caso de uso: reportes. El admin ve toda la plataforma; el proveedor, solo lo suyo. */
public interface ReporteUseCase {

    ResumenGeneral resumen(Actor actor);

    List<IngresoMensual> ingresosMensuales(Actor actor);

    List<HerramientaRanking> herramientasMasAlquiladas(Actor actor, int limite);

    List<ClienteFrecuente> clientesFrecuentes(Actor actor, int limite);

    List<DisponibilidadHerramienta> disponibilidad(Actor actor);
}
