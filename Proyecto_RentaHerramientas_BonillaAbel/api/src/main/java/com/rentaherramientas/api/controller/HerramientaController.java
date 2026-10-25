package com.rentaherramientas.api.controller;

import com.rentaherramientas.api.dto.HerramientaDto.Request;
import com.rentaherramientas.api.dto.HerramientaDto.Response;
import com.rentaherramientas.api.dto.ReservaDto.DisponibilidadResponse;
import com.rentaherramientas.api.security.ActorResolver;
import com.rentaherramientas.application.port.in.HerramientaUseCase;
import com.rentaherramientas.application.port.in.ReservaUseCase;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.FiltroHerramienta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/herramientas")
@RequiredArgsConstructor
@Tag(name = "Herramientas", description = "Inventario y catálogo de equipos")
public class HerramientaController {

    private final HerramientaUseCase herramientas;
    private final ReservaUseCase reservas;

    @Operation(summary = "Explorar el catálogo (público)",
            description = "Filtros opcionales: categoría, texto, rango de fechas libre (`desde` y `hasta` juntos) o solo disponibles.")
    @SecurityRequirements
    @GetMapping
    public List<Response> catalogo(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "false") boolean soloDisponibles) {
        if ((desde == null) != (hasta == null)) {
            throw new ReglaNegocioException("Indica ambas fechas (desde y hasta) o ninguna");
        }
        if (desde != null && hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        return herramientas.buscar(new FiltroHerramienta(categoriaId, q, null, desde, hasta, soloDisponibles))
                .stream().map(Response::de).toList();
    }

    @Operation(summary = "Mi inventario (ADMIN: todo; PROVEEDOR: solo lo suyo)")
    @GetMapping("/gestion")
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public List<Response> gestion() {
        return herramientas.listarGestionables(ActorResolver.actual()).stream().map(Response::de).toList();
    }

    @Operation(summary = "Detalle de una herramienta (público)")
    @SecurityRequirements
    @GetMapping("/{id:\\d+}")
    public Response obtener(@PathVariable Long id) {
        return Response.de(herramientas.obtener(id));
    }

    @Operation(summary = "¿Está libre en un rango de fechas? (público)")
    @SecurityRequirements
    @GetMapping("/{id:\\d+}/disponibilidad")
    public DisponibilidadResponse disponibilidad(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return new DisponibilidadResponse(id, desde, hasta, reservas.estaDisponible(id, desde, hasta));
    }

    @Operation(summary = "Registrar una herramienta (ADMIN o PROVEEDOR)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response crear(@Valid @RequestBody Request r) {
        return Response.de(herramientas.crear(r.aComando(), ActorResolver.actual()));
    }

    @Operation(summary = "Editar una herramienta (ADMIN o su proveedor)")
    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response actualizar(@PathVariable Long id, @Valid @RequestBody Request r) {
        return Response.de(herramientas.actualizar(id, r.aComando(), ActorResolver.actual()));
    }

    @Operation(summary = "Eliminar una herramienta (ADMIN o su proveedor)",
            description = "Eliminación lógica. Falla si tiene reservas activas.")
    @DeleteMapping("/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public void eliminar(@PathVariable Long id) {
        herramientas.eliminar(id, ActorResolver.actual());
    }

    @Operation(summary = "Subir imagen (JPG, PNG o WEBP, máx. 5 MB)")
    @PostMapping(value = "/{id:\\d+}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response subirImagen(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) throws IOException {
        return Response.de(herramientas.subirImagen(id, archivo.getContentType(), archivo.getBytes(), ActorResolver.actual()));
    }
}
