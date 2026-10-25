package com.rentaherramientas.api.controller;

import com.rentaherramientas.api.dto.ClienteDto.Request;
import com.rentaherramientas.api.dto.ClienteDto.Response;
import com.rentaherramientas.api.security.ActorResolver;
import com.rentaherramientas.application.port.in.ClienteUseCase;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "CRUD de clientes")
public class ClienteController {

    private final ClienteUseCase clientes;

    @Operation(summary = "Listar clientes (ADMIN)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Response> listar() {
        return clientes.listar().stream().map(Response::de).toList();
    }

    @Operation(summary = "Consultar un cliente (ADMIN o el mismo cliente)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public Response obtener(@PathVariable Long id) {
        return Response.de(clientes.obtener(id, ActorResolver.actual()));
    }

    @Operation(summary = "Registrar un cliente (ADMIN)", description = "Los clientes también pueden autorregistrarse en /api/auth/registro.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Response crear(@Valid @RequestBody Request r) {
        return Response.de(clientes.crear(r.aComando()));
    }

    @Operation(summary = "Actualizar un cliente (ADMIN o el mismo cliente)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public Response actualizar(@PathVariable Long id, @Valid @RequestBody Request r) {
        return Response.de(clientes.actualizar(id, r.aComando(), ActorResolver.actual()));
    }

    @Operation(summary = "Eliminar (desactivar) un cliente (ADMIN)",
            description = "Eliminación lógica: se conserva el historial. Falla si tiene reservas activas.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        clientes.eliminar(id);
    }
}
