package com.rentaherramientas.application.port.in;

import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.EstadoDanio;
import com.rentaherramientas.domain.model.ReporteDanio;

import java.math.BigDecimal;
import java.util.List;

/** Caso de uso: gestión de incidencias por equipos dañados. */
public interface DanioUseCase {

    List<ReporteDanio> listar(Actor actor);

    ReporteDanio crear(CrearDanioComando comando, Actor actor);

    /** Solo el administrador cambia el estado de la incidencia. Al resolver, la herramienta vuelve a estar disponible. */
    ReporteDanio cambiarEstado(Long id, EstadoDanio nuevoEstado, Actor actor);

    record CrearDanioComando(Long reservaId, String descripcion, BigDecimal costoEstimado) {
    }
}
