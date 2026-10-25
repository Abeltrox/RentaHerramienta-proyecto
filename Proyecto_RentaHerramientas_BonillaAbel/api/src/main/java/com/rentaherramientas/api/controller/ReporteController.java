package com.rentaherramientas.api.controller;

import com.rentaherramientas.api.security.ActorResolver;
import com.rentaherramientas.application.port.in.ReporteUseCase;
import com.rentaherramientas.domain.model.Reportes.ClienteFrecuente;
import com.rentaherramientas.domain.model.Reportes.DisponibilidadHerramienta;
import com.rentaherramientas.domain.model.Reportes.HerramientaRanking;
import com.rentaherramientas.domain.model.Reportes.IngresoMensual;
import com.rentaherramientas.domain.model.Reportes.ResumenGeneral;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
@Tag(name = "Reportes", description = "Estadísticas. El ADMIN ve toda la plataforma; el PROVEEDOR solo lo suyo.")
public class ReporteController {

    private final ReporteUseCase reportes;

    @Operation(summary = "Resumen general (tarjetas del panel)")
    @GetMapping("/resumen")
    public ResumenGeneral resumen() {
        return reportes.resumen(ActorResolver.actual());
    }

    @Operation(summary = "Ingresos por alquileres agrupados por mes")
    @GetMapping("/ingresos")
    public List<IngresoMensual> ingresos() {
        return reportes.ingresosMensuales(ActorResolver.actual());
    }

    @Operation(summary = "Herramientas más alquiladas")
    @GetMapping("/herramientas-mas-alquiladas")
    public List<HerramientaRanking> masAlquiladas(@RequestParam(defaultValue = "10") int limite) {
        return reportes.herramientasMasAlquiladas(ActorResolver.actual(), limite);
    }

    @Operation(summary = "Clientes frecuentes")
    @GetMapping("/clientes-frecuentes")
    public List<ClienteFrecuente> clientesFrecuentes(@RequestParam(defaultValue = "10") int limite) {
        return reportes.clientesFrecuentes(ActorResolver.actual(), limite);
    }

    @Operation(summary = "Disponibilidad y estado de las herramientas (hoy)")
    @GetMapping("/disponibilidad")
    public List<DisponibilidadHerramienta> disponibilidad() {
        return reportes.disponibilidad(ActorResolver.actual());
    }
}
