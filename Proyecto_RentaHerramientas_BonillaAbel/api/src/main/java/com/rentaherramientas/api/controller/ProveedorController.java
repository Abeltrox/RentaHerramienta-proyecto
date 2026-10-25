package com.rentaherramientas.api.controller;

import com.rentaherramientas.api.dto.ProveedorDto.Request;
import com.rentaherramientas.api.dto.ProveedorDto.Response;
import com.rentaherramientas.api.security.ActorResolver;
import com.rentaherramientas.application.port.in.ProveedorUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "CRUD de proveedores de equipos")
public class ProveedorController {

    private final ProveedorUseCase proveedores;

    @Operation(summary = "Listar proveedores (ADMIN)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Response> listar() {
        return proveedores.listar().stream().map(Response::de).toList();
    }

    @Operation(summary = "Consultar un proveedor (ADMIN o el mismo proveedor)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response obtener(@PathVariable Long id) {
        return Response.de(proveedores.obtener(id, ActorResolver.actual()));
    }

    @Operation(summary = "Registrar un proveedor (ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Response crear(@Valid @RequestBody Request r) {
        return Response.de(proveedores.crear(r.aComando()));
    }

    @Operation(summary = "Actualizar un proveedor (ADMIN o el mismo proveedor)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response actualizar(@PathVariable Long id, @Valid @RequestBody Request r) {
        return Response.de(proveedores.actualizar(id, r.aComando(), ActorResolver.actual()));
    }

    @Operation(summary = "Eliminar (desactivar) un proveedor (ADMIN)",
            description = "Eliminación lógica: se conserva el historial. Falla si tiene reservas activas.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        proveedores.eliminar(id);
    }
}
