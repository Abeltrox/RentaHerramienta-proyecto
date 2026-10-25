package com.rentaherramientas.api.controller;

import com.rentaherramientas.api.dto.ReservaDto.CrearRequest;
import com.rentaherramientas.api.dto.ReservaDto.DevolucionRequest;
import com.rentaherramientas.api.dto.ReservaDto.RechazoRequest;
import com.rentaherramientas.api.dto.ReservaDto.Response;
import com.rentaherramientas.api.security.ActorResolver;
import com.rentaherramientas.application.port.in.ReservaUseCase.CrearReservaComando;
import com.rentaherramientas.application.port.in.ReservaUseCase.DevolucionComando;
import com.rentaherramientas.application.port.in.ReservaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Solicitud, aprobación, entrega y devolución de alquileres")
public class ReservaController {

    private final ReservaUseCase reservas;

    @Operation(summary = "Solicitar una reserva (CLIENTE)",
            description = "Valida disponibilidad y fechas. Queda PENDIENTE, o APROBADA si la herramienta tiene confirmación automática.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE')")
    public Response crear(@Valid @RequestBody CrearRequest r) {
        return Response.de(reservas.crear(
                new CrearReservaComando(r.herramientaId(), r.fechaInicio(), r.fechaFin(), r.observaciones()), ActorResolver.actual()));
    }

    @Operation(summary = "Listar reservas según el rol",
            description = "ADMIN: todas · PROVEEDOR: las de sus herramientas · CLIENTE: su historial de alquileres.")
    @GetMapping
    public List<Response> listar() {
        return reservas.listar(ActorResolver.actual()).stream().map(Response::de).toList();
    }

    @Operation(summary = "Detalle de una reserva")
    @GetMapping("/{id}")
    public Response obtener(@PathVariable Long id) {
        return Response.de(reservas.obtener(id, ActorResolver.actual()));
    }

    @Operation(summary = "Aprobar solicitud (PROVEEDOR dueño o ADMIN)")
    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response aprobar(@PathVariable Long id) {
        return Response.de(reservas.aprobar(id, ActorResolver.actual()));
    }

    @Operation(summary = "Rechazar solicitud (PROVEEDOR dueño o ADMIN)")
    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response rechazar(@PathVariable Long id, @Valid @RequestBody RechazoRequest r) {
        return Response.de(reservas.rechazar(id, r.motivo(), ActorResolver.actual()));
    }

    @Operation(summary = "Cancelar reserva pendiente o aprobada (CLIENTE dueño o ADMIN)")
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public Response cancelar(@PathVariable Long id) {
        return Response.de(reservas.cancelar(id, ActorResolver.actual()));
    }

    @Operation(summary = "Registrar entrega de la herramienta (PROVEEDOR dueño o ADMIN)", description = "Requiere reserva PAGADA.")
    @PatchMapping("/{id}/entregar")
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response entregar(@PathVariable Long id) {
        return Response.de(reservas.registrarEntrega(id, ActorResolver.actual()));
    }

    @Operation(summary = "Confirmar devolución y reportar daños (PROVEEDOR dueño o ADMIN)",
            description = "Si `hayDanio` es true se crea la incidencia y la herramienta pasa a DANADA.")
    @PatchMapping("/{id}/devolver")
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response devolver(@PathVariable Long id, @Valid @RequestBody(required = false) DevolucionRequest r) {
        DevolucionComando comando = r == null ? new DevolucionComando(false, null, null)
                : new DevolucionComando(r.hayDanio(), r.descripcionDanio(), r.costoEstimado());
        return Response.de(reservas.registrarDevolucion(id, comando, ActorResolver.actual()));
    }
}
