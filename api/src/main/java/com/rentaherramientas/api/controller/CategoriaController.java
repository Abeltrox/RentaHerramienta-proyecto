package com.rentaherramientas.api.controller;

import com.rentaherramientas.api.dto.CategoriaDto.Request;
import com.rentaherramientas.api.dto.CategoriaDto.Response;
import com.rentaherramientas.application.port.in.CategoriaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Clasificación de herramientas")
public class CategoriaController {

    private final CategoriaUseCase categorias;

    @Operation(summary = "Listar categorías (público)")
    @SecurityRequirements
    @GetMapping
    public List<Response> listar() {
        return categorias.listar().stream().map(Response::de).toList();
    }

    @Operation(summary = "Crear categoría (ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Response crear(@Valid @RequestBody Request r) {
        return Response.de(categorias.crear(r.aComando()));
    }

    @Operation(summary = "Actualizar categoría (ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Response actualizar(@PathVariable Long id, @Valid @RequestBody Request r) {
        return Response.de(categorias.actualizar(id, r.aComando()));
    }

    @Operation(summary = "Eliminar categoría (ADMIN)", description = "Falla si hay herramientas que la usan.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        categorias.eliminar(id);
    }
}
