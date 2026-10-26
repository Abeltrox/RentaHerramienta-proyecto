package com.rentaherramientas.api.controller;

import com.rentaherramientas.api.dto.DanioDto.CrearRequest;
import com.rentaherramientas.api.dto.DanioDto.EstadoRequest;
import com.rentaherramientas.api.dto.DanioDto.Response;
import com.rentaherramientas.api.security.ActorResolver;
import com.rentaherramientas.application.port.in.DanioUseCase.CrearDanioComando;
import com.rentaherramientas.application.port.in.DanioUseCase;
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
@RequestMapping("/api/danios")
@RequiredArgsConstructor
@Tag(name = "Daños", description = "Incidencias por equipos dañados")
public class DanioController {

    private final DanioUseCase danios;

    @Operation(summary = "Listar reportes de daño (ADMIN: todos · PROVEEDOR: los suyos)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public List<Response> listar() {
        return danios.listar(ActorResolver.actual()).stream().map(Response::de).toList();
    }

    @Operation(summary = "Reportar un daño (PROVEEDOR dueño o ADMIN)", description = "La reserva debe estar ENTREGADA o DEVUELTA.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response crear(@Valid @RequestBody CrearRequest r) {
        return Response.de(danios.crear(new CrearDanioComando(r.reservaId(), r.descripcion(), r.costoEstimado()), ActorResolver.actual()));
    }

    @Operation(summary = "Avanzar el estado de la incidencia (ADMIN)",
            description = "REPORTADO → EN_REVISION → RESUELTO. Al resolver, la herramienta vuelve a estar DISPONIBLE.")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public Response cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest r) {
        return Response.de(danios.cambiarEstado(id, r.estado(), ActorResolver.actual()));
    }
}
