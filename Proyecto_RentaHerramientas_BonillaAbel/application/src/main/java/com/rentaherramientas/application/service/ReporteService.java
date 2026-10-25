package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.ReporteUseCase;
import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Reportes.ClienteFrecuente;
import com.rentaherramientas.domain.model.Reportes.DisponibilidadHerramienta;
import com.rentaherramientas.domain.model.Reportes.HerramientaRanking;
import com.rentaherramientas.domain.model.Reportes.IngresoMensual;
import com.rentaherramientas.domain.model.Reportes.ResumenGeneral;
import com.rentaherramientas.domain.port.out.ReporteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService implements ReporteUseCase {

    private final ReporteRepositoryPort reportes;

    @Override
    public ResumenGeneral resumen(Actor actor) {
        return reportes.resumen(alcance(actor));
    }

    @Override
    public List<IngresoMensual> ingresosMensuales(Actor actor) {
        return reportes.ingresosMensuales(alcance(actor));
    }

    @Override
    public List<HerramientaRanking> herramientasMasAlquiladas(Actor actor, int limite) {
        return reportes.herramientasMasAlquiladas(alcance(actor), limitar(limite));
    }

    @Override
    public List<ClienteFrecuente> clientesFrecuentes(Actor actor, int limite) {
        return reportes.clientesFrecuentes(alcance(actor), limitar(limite));
    }

    @Override
    public List<DisponibilidadHerramienta> disponibilidad(Actor actor) {
        return reportes.disponibilidad(alcance(actor));
    }

    /** Admin: toda la plataforma (null). Proveedor: solo lo suyo. Cliente: sin acceso. */
    private Long alcance(Actor actor) {
        if (actor.esAdmin()) {
            return null;
        }
        if (actor.esProveedor()) {
            return actor.usuarioId();
        }
        throw new OperacionNoPermitidaException("Los clientes no tienen acceso a reportes");
    }

    private int limitar(int limite) {
        return Math.max(1, Math.min(limite, 50));
    }
}
